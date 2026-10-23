package dev.adriangm.tracker.imports.adapter.csv;

import dev.adriangm.tracker.imports.application.CsvSource;
import dev.adriangm.tracker.imports.application.ImportRow;
import dev.adriangm.tracker.imports.application.InvalidCsv;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

public final class CsvOfferSource implements CsvSource {
  public static final int MAX_BYTES = 1024 * 1024;
  private static final Set<String> REQUIRED = Set.of("source", "external_id", "company", "role");
  private static final Set<String> ALLOWED =
      Set.of(
          "source",
          "external_id",
          "company",
          "role",
          "url",
          "location",
          "work_mode",
          "technologies");

  @Override
  public List<ImportRow> read(byte[] contents) {
    if (contents.length > MAX_BYTES) {
      throw new InvalidCsv("File exceeds 1 MiB");
    }
    String text = decode(contents);
    var format = CSVFormat.RFC4180.builder().setHeader().setSkipHeaderRecord(true).get();
    try (CSVParser parser = format.parse(new StringReader(text))) {
      validateHeader(parser.getHeaderNames());
      return readRecords(parser);
    } catch (IOException | java.io.UncheckedIOException invalid) {
      throw new InvalidCsv("Malformed CSV", invalid);
    }
  }

  private static List<ImportRow> readRecords(CSVParser parser) {
    List<ImportRow> rows = new ArrayList<>();
    for (var record : parser) {
      if (rows.size() >= 1000 || !record.isConsistent()) {
        throw new InvalidCsv("Too many records or inconsistent columns");
      }
      var fields = new LinkedHashMap<String, String>();
      parser.getHeaderNames().forEach(name -> fields.put(name, record.get(name)));
      rows.add(new ImportRow((int) record.getRecordNumber(), fields));
    }
    return List.copyOf(rows);
  }

  private static void validateHeader(List<String> names) {
    if (!names.containsAll(REQUIRED)
        || !ALLOWED.containsAll(names)
        || names.size() != Set.copyOf(names).size()) {
      throw new InvalidCsv("Invalid CSV header");
    }
  }

  private static String decode(byte[] contents) {
    try {
      String text =
          StandardCharsets.UTF_8
              .newDecoder()
              .onMalformedInput(CodingErrorAction.REPORT)
              .onUnmappableCharacter(CodingErrorAction.REPORT)
              .decode(ByteBuffer.wrap(contents))
              .toString();
      return text.startsWith("\uFEFF") ? text.substring(1) : text;
    } catch (CharacterCodingException invalid) {
      throw new InvalidCsv("Expected UTF-8", invalid);
    }
  }
}
