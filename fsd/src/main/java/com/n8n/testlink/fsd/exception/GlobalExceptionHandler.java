package com.n8n.testlink.fsd.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Validation errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {

        log.warn("⚠️ Exception: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", ex.getMessage()
        ));
    }

    // TestLink API specific errors
    @ExceptionHandler(br.eti.kinoshita.testlinkjavaapi.util.TestLinkAPIException.class)
    public ResponseEntity<Map<String, String>> handleTestLinkError(Exception ex) {

        log.error("❌ TestLink API Error", ex);

        return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "TestLink API error: " + ex.getMessage()
        ));
    }

    // All other unexpected errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {

        log.error("🔥 Unexpected Exception", ex);

        return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Internal server error"
        ));
    }
}