package dev.adriangm.tracker.applications.adapter.web;

import dev.adriangm.tracker.applications.application.EditConflict;
import dev.adriangm.tracker.applications.application.RecordMissing;
import dev.adriangm.tracker.applications.application.query.InvalidCriteria;
import dev.adriangm.tracker.applications.domain.RuleViolation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates the meaning of a failure into an HTTP response. Unexpected failures, including other
 * database integrity errors, keep their nature and end as a server error.
 */
@ControllerAdvice
public class WebErrors {
  @ExceptionHandler(RecordMissing.class)
  String missing(HttpServletResponse response, Model model) {
    return error(HttpServletResponse.SC_NOT_FOUND, "missing", response, model);
  }

  @ExceptionHandler({EditConflict.class, OptimisticLockingFailureException.class})
  String conflict(HttpServletResponse response, Model model) {
    return error(HttpServletResponse.SC_CONFLICT, "conflict", response, model);
  }

  @ExceptionHandler(RuleViolation.class)
  String rule(RuleViolation invalid, HttpServletResponse response, Model model) {
    return error(HttpServletResponse.SC_BAD_REQUEST, invalid.code(), response, model);
  }

  @ExceptionHandler(InvalidInput.class)
  String input(InvalidInput invalid, HttpServletResponse response, Model model) {
    return error(HttpServletResponse.SC_BAD_REQUEST, invalid.code(), response, model);
  }

  @ExceptionHandler(InvalidCriteria.class)
  String criteria(HttpServletResponse response, Model model) {
    return error(HttpServletResponse.SC_BAD_REQUEST, "filters", response, model);
  }

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  String invalid(HttpServletResponse response, Model model) {
    return error(HttpServletResponse.SC_BAD_REQUEST, "invalid", response, model);
  }

  private String error(int status, String code, HttpServletResponse response, Model model) {
    response.setStatus(status);
    model.addAttribute("errorCode", code);
    model.addAttribute("errorStatus", status);
    return "error";
  }
}
