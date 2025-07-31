package com.kukokuk.advice;

import com.kukokuk.exception.AppException;
import org.springframework.dao.DataAccessException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.kukokuk.controller")
public class ControllerExceptionHandler {

    @ExceptionHandler(AppException.class)
    public String handlerAppException(AppException ex, Model model) {
        model.addAttribute("message", ex.getMessage());

        return "error/app";
    }

    @ExceptionHandler(DataAccessException.class)
    public String handlerDataAccessException(DataAccessException ex, Model model) {
        model.addAttribute("message", ex.getMessage());

        return "error/db";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handlerRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("message", ex.getMessage());

        return "error/server";
    }

}
