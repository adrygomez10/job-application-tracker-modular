package dev.adriangm.tracker.configuration;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class WorkshopErrors {
  @ExceptionHandler(IllegalArgumentException.class)
  String invalid(HttpServletResponse response, Model model) {
    response.setStatus(400);
    model.addAttribute("errorCode", "invalid");
    model.addAttribute("errorStatus", 400);
    return "error";
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  String tooLarge(HttpServletResponse response, Model model) {
    response.setStatus(413);
    model.addAttribute("errorCode", "invalid");
    model.addAttribute("errorStatus", 413);
    return "error";
  }
}
