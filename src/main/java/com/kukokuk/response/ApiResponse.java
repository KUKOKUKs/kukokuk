package com.kukokuk.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private String message;
    private T data;

    public ApiResponse(boolean success, int status, String message, T data) {
        this.success = success;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 성공 응답- status:200, message:메세지, data:null
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<Void>(true, 200, message, null);
    }

    // 성공 응답 - status:200, message:"성공", data:데이터
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, 200, "성공", data);
    }

    // 성공 응답 - status:200, message:메세지, data:데이터
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<T>(true, 200, message, data);
    }

    // 실패 응답 - status:응답코드, message:메세지, data:null
    public static ApiResponse<Void> fail(int status, String message) {
        return new ApiResponse<Void>(false, status, message, null);
    }
}
