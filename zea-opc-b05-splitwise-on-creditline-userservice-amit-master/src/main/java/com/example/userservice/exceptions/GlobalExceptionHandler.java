package com.example.userservice.exceptions;

import com.example.userservice.dto.response.ErrorResponse;
import in.zeta.spectra.capture.SpectraLogger;
import in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.AuthorizationServerException;
import in.zeta.springframework.boot.commons.clients.exception.InvalidAuthTokenException;
import olympus.trace.OlympusSpectra;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    private static final String ATTR_STATUS = "status";
    private static final String ATTR_MESSAGE = "message";
    private static final String ATTR_METHOD = "method";
    private static final String ATTR_PATH = "path";
    private static final SpectraLogger logger = OlympusSpectra.getLogger(GlobalExceptionHandler.class);

    // ==================== Authentication & Authorization Exceptions ====================

    @ExceptionHandler(in.zeta.springframework.boot.commons.authentication.models.UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationUnauthorizedException(
            in.zeta.springframework.boot.commons.authentication.models.UnauthorizedException ex, 
            HttpServletRequest request) {
        logger.warn("Authentication failed - no valid auth token provided")
                .attr(ATTR_STATUS, ErrorCode.UNAUTHORIZED.getHttpStatus().value())
                .attr(ATTR_MESSAGE, ex.getMessage())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED, 
                "Authentication required. Please provide a valid auth token in the request header.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ErrorCode.UNAUTHORIZED.getHttpStatus());
    }

    @ExceptionHandler(in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleSandboxUnauthorizedException(
            in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.UnauthorizedException ex, 
            HttpServletRequest request) {
        logger.warn("Authorization failed - access denied")
                .attr(ATTR_STATUS, ErrorCode.FORBIDDEN.getHttpStatus().value())
                .attr(ATTR_MESSAGE, ex.getMessage())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.FORBIDDEN, 
                "Access denied. You do not have permission to perform this action.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ErrorCode.FORBIDDEN.getHttpStatus());
    }

    @ExceptionHandler(AuthorizationServerException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationServerException(
            AuthorizationServerException ex, 
            HttpServletRequest request) {
        logger.warn("Authorization server error")
                .attr(ATTR_STATUS, ErrorCode.UNAUTHORIZED.getHttpStatus().value())
                .attr(ATTR_MESSAGE, ex.getMessage())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED, 
                "Authorization failed. Please ensure you have a valid auth token and required permissions.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ErrorCode.UNAUTHORIZED.getHttpStatus());
    }

    @ExceptionHandler(InvalidAuthTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthTokenException(
            InvalidAuthTokenException ex, 
            HttpServletRequest request) {
        logger.warn("Invalid auth token provided")
                .attr(ATTR_STATUS, ErrorCode.INVALID_AUTH_TOKEN.getHttpStatus().value())
                .attr(ATTR_MESSAGE, ex.getMessage())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_AUTH_TOKEN, 
                "The provided authentication token is invalid or expired. Please provide a valid token.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ErrorCode.INVALID_AUTH_TOKEN.getHttpStatus());
    }

    // ==================== CompletionException (unwrap async exceptions) ====================

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ErrorResponse> handleCompletionException(CompletionException ex, HttpServletRequest request) {
        Throwable cause = ex.getCause();
        
        // Handle wrapped authentication/authorization exceptions
        if (cause instanceof in.zeta.springframework.boot.commons.authentication.models.UnauthorizedException) {
            return handleAuthenticationUnauthorizedException(
                    (in.zeta.springframework.boot.commons.authentication.models.UnauthorizedException) cause, request);
        }
        if (cause instanceof in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.UnauthorizedException) {
            return handleSandboxUnauthorizedException(
                    (in.zeta.springframework.boot.commons.authorization.sandboxAccessControl.UnauthorizedException) cause, request);
        }
        if (cause instanceof AuthorizationServerException) {
            return handleAuthorizationServerException((AuthorizationServerException) cause, request);
        }
        if (cause instanceof InvalidAuthTokenException) {
            return handleInvalidAuthTokenException((InvalidAuthTokenException) cause, request);
        }
        if (cause instanceof UserServiceException) {
            return handleUserServiceException((UserServiceException) cause, request);
        }
        
        logger.error("CompletionException occurred")
                .attr("causeClass", cause != null ? cause.getClass().getSimpleName() : "null")
                .attr(ATTR_MESSAGE, cause != null ? cause.getMessage() : ex.getMessage())
                .attr(ATTR_PATH, request.getRequestURI())
                .log();
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_ERROR,
                cause != null ? cause.getMessage() : "An unexpected error occurred",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }

    // ==================== User Service Exceptions ====================

    @ExceptionHandler(UserServiceException.class)
    public ResponseEntity<ErrorResponse> handleUserServiceException(UserServiceException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        
        logger.error("UserServiceException occurred")
                .attr(ATTR_STATUS, errorCode.getHttpStatus().value())
                .attr(ATTR_MESSAGE, errorCode.getMessage())
                .attr("details", ex.getDetails())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .attr("exceptionClass", ex.getClass().getSimpleName())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(errorCode, ex.getDetails(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        logger.warn("Validation error")
                .attr(ATTR_STATUS, ErrorCode.INVALID_REQUEST.getHttpStatus().value())
                .attr("validationErrors", validationErrors)
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_REQUEST, validationErrors, request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ErrorCode.INVALID_REQUEST.getHttpStatus());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        logger.error("ResponseStatusException occurred")
                .attr(ATTR_STATUS, ex.getStatusCode().value())
                .attr("reason", ex.getReason())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ex.getReason(), request.getRequestURI());
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error occurred")
                .attr(ATTR_STATUS, ErrorCode.INTERNAL_ERROR.getHttpStatus().value())
                .attr(ATTR_MESSAGE, ex.getMessage())
                .attr(ATTR_PATH, request.getRequestURI())
                .attr(ATTR_METHOD, request.getMethod())
                .attr("exceptionClass", ex.getClass().getSimpleName())
                .attr("stackTrace", getStackTraceAsString(ex))
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_ERROR, 
                "An unexpected error occurred. Please try again later.", 
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, ErrorCode.INTERNAL_ERROR.getHttpStatus());
    }

    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        logger.error("ResponseStatusException occurred")
                .attr(ATTR_STATUS, ex.getStatusCode().value())
                .attr("reason", ex.getReason())
                .log();

        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_ERROR, ex.getReason());
        return new ResponseEntity<>(errorResponse, ex.getStatusCode());
    }

    private String getStackTraceAsString(Exception ex) {
        java.io.StringWriter sw = new java.io.StringWriter();
        ex.printStackTrace(new java.io.PrintWriter(sw));
        String fullStackTrace = sw.toString();
        return fullStackTrace.length() > 500 ? fullStackTrace.substring(0, 500) + "..." : fullStackTrace;
    }
}

