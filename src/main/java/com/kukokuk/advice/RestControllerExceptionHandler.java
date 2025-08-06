package com.kukokuk.advice;

import com.kukokuk.exception.AppException;
import com.kukokuk.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.kukokuk.rest")
public class RestControllerExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handlerAppException(AppException ex) {
        ApiResponse<Void> apiResponse = ApiResponse.fail(500, ex.getMessage());

        return ResponseEntity
            .status(500)
            .body(apiResponse);
    }
}
