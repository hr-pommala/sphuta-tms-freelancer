package net.sphuta.tms.freelancer.exception;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@ControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------- CUSTOM EXCEPTION HANDLERS -----------------------

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<TmsApiResponse<?>> notFound(NotFoundException ex) {
        log.error("NotFoundException handled: {}", ex.getMessage());
        return wrap(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<TmsApiResponse<?>> conflict(ConflictException ex) {
        log.error("ConflictException handled: {}", ex.getMessage());
        return wrap(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(TmsException.class)
    public ResponseEntity<TmsApiResponse<Void>> handleTms(TmsException ex, HttpServletRequest req) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;
        log.error("TmsException {} on {}: {}", status.value(), req.getRequestURI(), ex.getMessage());

        TmsApiResponse<Void> body = new TmsApiResponse<>(
                false,
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(body);
    }

    // ----------------------- VALIDATION HANDLERS -----------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<TmsApiResponse<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, String> details = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> (fe.getDefaultMessage() == null || fe.getDefaultMessage().isBlank())
                                ? "Invalid or required field"
                                : fe.getDefaultMessage(),
                        (a, b) -> a + ", " + b,
                        LinkedHashMap::new
                ));

        log.warn("400 Validation failed on {}: {}", req.getRequestURI(), details);

        TmsApiResponse<Map<String, String>> body = new TmsApiResponse<>(
                false,
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed: required/invalid fields present",
                details,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<TmsApiResponse<?>> pathQueryValidation(ConstraintViolationException ex) {
        log.error("ConstraintViolationException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, "Validation failed: " + ex.getMessage(), null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<TmsApiResponse<?>> dbConflicts(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException handled: {}", ex.getMostSpecificCause().getMessage());
        return wrap(HttpStatus.CONFLICT, "Unique or FK constraint violated", null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<TmsApiResponse<?>> badRequest(IllegalArgumentException ex) {
        log.error("IllegalArgumentException handled: {}", ex.getMessage());
        return wrap(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<TmsApiResponse<Void>> handleOther(Exception ex, HttpServletRequest req) {
        log.error("500 Internal error on {}: {}", req.getRequestURI(), ex.getMessage(), ex);

        TmsApiResponse<Void> body = new TmsApiResponse<>(
                false,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected error",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // ----------------------- UTILITY -----------------------
    private ResponseEntity<TmsApiResponse<?>> wrap(HttpStatus status, String message, Object details) {
        var body = TmsApiResponse.failure(status, message, details);
        return ResponseEntity.status(status).body(body);
    }
}
