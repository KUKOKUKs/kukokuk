package com.kukokuk.common.advice;

import com.kukokuk.common.dto.ApiResponse;
import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.exception.BadRequestException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.kukokuk.domain")
public class RestControllerExceptionHandler {

    // 공통 예외 처리 구조 생성
    private ResponseEntity<ApiResponse<Void>> buildResponse(int status, String message) {
        ApiResponse<Void> apiResponse = ApiResponse.fail(status, message);
        return ResponseEntity.status(status).body(apiResponse);
    }

    // 애플리케이션에서 발생하는 커스텀 예외 처리용 (직접 정의한 AppException 예외 발생 시)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handlerAppException(AppException ex) {
        return buildResponse(500, ex.getMessage());
    }

    // DB 관련 예외 처리용 (DB 작업 중 SQLException, 무결성 제약 위반, 쿼리 오류 등 발생 시)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handlerDataAccessException(DataAccessException ex) {
        return buildResponse(500, "Database Error: " + ex.getMessage());
    }

    // 런타임 예외 처리용 (널 포인터, 잘못된 형변환, 배열 인덱스 오류 등)
    // NullPointerException, IllegalArgumentException 등 일반적인 예기치 못한 에러
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handlerRuntimeException(RuntimeException ex) {
        return buildResponse(500, "예기치 못한 오류가 발생했습니다: " + ex.getMessage());
    }

    // @Valid, @Validated 실패 시 처리 (요청 값에 대해 유효성 검사를 통과하지 못한 경우)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(MethodArgumentNotValidException ex) {
        return buildResponse(400, "입력 값이 올바르지 않습니다: " + ex.getMessage());
    }

    // 인가 실패
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(403, "접근 권한이 없습니다: " + ex.getMessage());
    }

    // 범용 요청값 오류 처리
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return buildResponse(400, "요청값이 올바르지 않습니다: " + ex.getMessage());
    }

}
