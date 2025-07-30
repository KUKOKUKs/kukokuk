package com.kukokuk.rest;

import com.kukokuk.exception.AppException;
import com.kukokuk.response.ApiResponse;
import com.kukokuk.response.ResponseEntityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.kukokuk.rest")
public class RestControllerExceptionHandler {
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiResponse<Void>> handlerAppException(AppException ex) {
    return ResponseEntityUtils.fail(500, ex.getMessage());
  }
}
