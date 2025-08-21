package com.kukokuk.common.exception;

import java.io.Serial;

public class AppException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7550881696969175849L;

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

}
