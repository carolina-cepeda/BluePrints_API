package edu.eci.arsw.blueprints.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standard API response wrapper for Blueprint-related operations.
 *
 * @param <T> Blueprint or Blueprint-related data
 */
@Schema(description = "Standard response wrapper for Blueprint API operations")
public record ApiResponse<T>(

        @Schema(
                description = "HTTP status code returned by a Blueprint operation",
                example = "200"
        )
        int code,

        @Schema(
                description = "Result message describing the outcome of the Blueprint operation",
                example = "Blueprint retrieved successfully"
        )
        String message,

        @Schema(
                description = "Blueprint data returned by the operation (single Blueprint, list of Blueprints, or null)"
        )
        T data
) {}
