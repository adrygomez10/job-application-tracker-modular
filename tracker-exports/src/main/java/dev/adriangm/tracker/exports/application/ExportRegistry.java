package dev.adriangm.tracker.exports.application;

import dev.adriangm.tracker.exports.api.ExportDescriptor;
import dev.adriangm.tracker.exports.api.ExportPlugin;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExportRegistry {
  private final Map<String, ExportPlugin> plugins;

  public ExportRegistry(List<ExportPlugin> exporters) {
    var registered = new LinkedHashMap<String, ExportPlugin>();
    for (ExportPlugin plugin : exporters) {
      if (registered.putIfAbsent(plugin.descriptor().id(), plugin) != null) {
        throw new IllegalArgumentException("Duplicate export plugin: " + plugin.descriptor().id());
      }
    }
    plugins = Map.copyOf(registered);
  }

  public List<ExportDescriptor> descriptors() {
    return plugins.values().stream()
        .map(ExportPlugin::descriptor)
        .sorted(java.util.Comparator.comparing(ExportDescriptor::id))
        .toList();
  }

  public ExportPlugin require(String id) {
    ExportPlugin plugin = plugins.get(id);
    if (plugin == null) {
      throw new IllegalArgumentException("Unknown export plugin");
    }
    return plugin;
  }
}
