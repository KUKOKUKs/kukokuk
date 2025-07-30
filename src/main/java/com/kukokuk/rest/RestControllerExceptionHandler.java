package com.kukokuk.rest;

import com.kukokuk.exception.AppException;
import com.kukokuk.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.kukokuk.rest")
public class RestControllerExceptionHandler {
  @ExceptionHandler(AppException.class)
  public ApiResponse<Void> handlerAppException(AppException ex) {
    return ApiResponse.fail(500, ex.getMessage());
  }
}
