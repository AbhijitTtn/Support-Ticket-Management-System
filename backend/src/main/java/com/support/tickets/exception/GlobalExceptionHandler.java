package com.support.tickets.exception;

import com.support.tickets.dto.ErrorResponse;
import com.support.tickets.dto.FieldErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                .toList();

        log.warn("Validation failed for {}: {}", request.getRequestURI(), fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Malformed request body for {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request", request, List.of()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        log.warn("{}: {}", request.getRequestURI(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(errorResponse(HttpStatus.BAD_REQUEST, message, request, List.of()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found for {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, List.of()));
    }

    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid status transition for {}: {}", request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorResponse(HttpStatus.CONFLICT, ex.getMessage(), request, List.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error for {}", request.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred",
                        request,
                        List.of()
                ));
    }

    private ErrorResponse errorResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            List<FieldErrorDto> fieldErrors
    ) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                fieldErrors
        );
    }
}
