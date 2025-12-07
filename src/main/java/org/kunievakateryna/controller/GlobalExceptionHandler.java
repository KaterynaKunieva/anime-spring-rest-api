package org.kunievakateryna.controller;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.kunievakateryna.exception.DuplicateRecordException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Global exception handler for REST controllers.
 */
@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    /**
     * Handles DuplicateRecordException and returns 409 Conflict.
     *
     * @param ex the exception
     * @return problem detail with error information
     */
    @ExceptionHandler(DuplicateRecordException.class)
    public ProblemDetail handleDuplicateRecordException(DuplicateRecordException ex) {
        log.warn("Duplicate record: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problemDetail.setTitle("Duplicate Record");
        return problemDetail;
    }

    /**
     * Handles NoSuchElementException and returns 404 Not Found.
     *
     * @param ex the exception
     * @return problem detail with error information
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("Resource Not Found");
        return problemDetail;
    }

    /**
     * Handles validation errors and returns 400 Bad Request.
     *
     * @param ex the exception
     * @return problem detail with error information
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                errors
        );
        problemDetail.setTitle("Validation Error");
        return problemDetail;
    }

    /**
     * Handles IllegalArgumentException and returns 400 Bad Request.
     *
     * @param ex the IllegalArgumentException
     * @return response entity with error information
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problem.setTitle("Invalid Request");
        problem.setType(URI.create("about:blank"));

        return ResponseEntity.badRequest().body(problem);
    }
}