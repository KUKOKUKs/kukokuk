package com.kukokuk.common.advice;

import com.kukokuk.common.exception.AppException;
import com.kukokuk.common.exception.BadRequestException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Log4j2
@ControllerAdvice(basePackages = "com.kukokuk.domain")
public class ControllerExceptionHandler {

    // 공통 예외 처리 구조 생성
    private String buildErrorPage(HttpServletResponse response, Model model,
        int status, String error, String message) {
        response.setStatus(status);
        model.addAttribute("error", error);
        model.addAttribute("status", status);
        model.addAttribute("message", message);
        log.error("[{}] {} - {}", error, status, message);
        return "error/error-page";
    }

    // 애플리케이션에서 발생하는 커스텀 예외 처리용 (직접 정의한 AppException 예외 발생 시)
    @ExceptionHandler(AppException.class)
    public String handlerAppException(AppException ex, HttpServletResponse response, Model model) {
        return buildErrorPage(response, model, 500, "App Error", ex.getMessage());
    }

    // DB 관련 예외 처리용 (DB 작업 중 SQLException, 무결성 제약 위반, 쿼리 오류 등 발생 시)
    @ExceptionHandler(DataAccessException.class)
    public String handlerDataAccessException(DataAccessException ex, HttpServletResponse response,
        Model model) {
        log.error("handlerDataAccessException 예외처리 발생: {}",  ex.getMessage());
        return buildErrorPage(response, model, 500, "Database Error", "데이터베이스 작업 중 오류가 발생했습니다.");
    }

    // 런타임 예외 처리용 (널 포인터, 잘못된 형변환, 배열 인덱스 오류 등)
    // NullPointerException, IllegalArgumentException 등 일반적인 예기치 못한 에러
    @ExceptionHandler(RuntimeException.class)
    public String handlerRuntimeException(RuntimeException ex, HttpServletResponse response,
        Model model) {
        log.error("handlerRuntimeException 예외처리 발생: {}",  ex.getMessage());
        return buildErrorPage(response, model, 500, "Runtime Error", "예기치 못한 오류가 발생했습니다.");
    }

    // @Valid, @Validated 실패 시 처리 (요청 값에 대해 유효성 검사를 통과하지 못한 경우)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleBadRequest(MethodArgumentNotValidException ex, HttpServletResponse response,
        Model model) {
        return buildErrorPage(response, model, 400, "Bad Request", "입력 값이 올바르지 않습니다." + ex.getMessage());
    }

    // 인가 실패
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(AccessDeniedException ex, HttpServletResponse response,
        Model model) {
        return buildErrorPage(response, model, 403, "Access Denied", "접근 권한이 없습니다.: " + ex.getMessage());
    }

    // 범용 요청값 오류 처리
    @ExceptionHandler(BadRequestException.class)
    public String handleBadRequest(BadRequestException ex, HttpServletResponse response,
        Model model) {
        return buildErrorPage(response, model, 400, "Bad Request", "요청값이 올바르지 않습니다." + ex.getMessage());
    }

}