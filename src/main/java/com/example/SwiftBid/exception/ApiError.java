package com.example.SwiftBid.exception;

import java.time.Instant;

/**
 * Uniform error body returned by {@link GlobalExceptionHandler}.
 */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {
}
