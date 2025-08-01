package com.kukokuk.advice;

import com.kukokuk.exception.AppException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice(basePackages = "com.kukokuk.controller")
public class ControllerExceptionHandler {

    private String buildErrorPage(HttpServletResponse response, Model model,
        int status, String error, String message) {
        response.setStatus(status);
        model.addAttribute("error", error);
        model.addAttribute("status", status);
        model.addAttribute("message", message);
        log.error("[{}] {} - {}", error, status, message);
        return "error/error-page";
    }

    @ExceptionHandler(AppException.class)
    public String handlerAppException(AppException ex, HttpServletResponse response, Model model) {
        return buildErrorPage(response, model, 500, "App Error", ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public String handlerDataAccessException(DataAccessException ex, HttpServletResponse response, Model model) {
        return buildErrorPage(response, model, 500, "Database Error", ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public String handlerRuntimeException(RuntimeException ex, HttpServletResponse response, Model model) {
        return buildErrorPage(response, model, 500, "Runtime Error", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleBadRequest(MethodArgumentNotValidException ex, HttpServletResponse response, Model model) {
        return buildErrorPage(response, model, 400, "Bad Request", "입력 값이 올바르지 않습니다.");
    }

}