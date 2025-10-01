package com.example.musicGenie.config.exception;

import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handle all uncaught exceptions (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneralException(
            Exception ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    // 2. Handle invalid JSON, request body parsing issues
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleInvalidJson(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid Request Body");
        problem.setDetail("Malformed JSON or invalid request format.");
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(problem);
    }

    // 3. Handle validation errors (e.g., @Valid, @NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Error");

        // Collect field error messages
        String errors = ex.getBindingResult().getFieldErrors().stream()
                          .map(err -> err.getField() + ": " + err.getDefaultMessage())
                          .collect(Collectors.joining(", "));

        problem.setDetail(errors);
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(problem);
    }

    // 4. Handle missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParams(
            MissingServletRequestParameterException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Missing Request Parameter");
        problem.setDetail("Required parameter '" + ex.getParameterName() + "' is missing.");
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(problem);
    }


    // 6. Handle access denied (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Access Denied");
        problem.setDetail("You do not have permission to access this resource.");
        problem.setProperty("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }
}
