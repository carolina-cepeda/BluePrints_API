package edu.eci.arsw.blueprints.controllers;

import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import edu.eci.arsw.blueprints.api.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * REST API controller for managing Blueprints.
 */
@RestController
@RequestMapping("/blueprints")
public class BlueprintsAPIController {

        private final BlueprintsServices services;

        public BlueprintsAPIController(BlueprintsServices services) {
                this.services = services;
        }

        @Operation(summary = "Get all blueprints", description = "Returns all blueprints stored in the system.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints successfully retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Blueprint.class)))
        })
        @GetMapping
        public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
                Set<Blueprint> blueprints = services.getAllBlueprints();
                ApiResponse<Set<Blueprint>> response = new ApiResponse<>(
                                HttpStatus.OK.value(),
                                "Blueprints retrieved successfully",
                                blueprints);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get blueprints by author", description = "Returns all blueprints created by the specified author.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints found for the author"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No blueprints found for the given author", content = @Content)
        })
        @GetMapping("/{author}")
        public ResponseEntity<?> byAuthor(@PathVariable String author) {
                try {
                        return ResponseEntity.ok(services.getBlueprintsByAuthor(author));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @Operation(summary = "Get a blueprint by author and name", description = "Returns a specific blueprint identified by author and blueprint name.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Blueprint.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content)
        })
        @GetMapping("/{author}/{bpname}")
        public ResponseEntity<?> byAuthorAndName(
                        @PathVariable String author,
                        @PathVariable String bpname) {
                try {
                        return ResponseEntity.ok(services.getBlueprint(author, bpname));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @Operation(summary = "Create a new blueprint", description = "Creates a new blueprint with the provided author, name and points.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint successfully created"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Blueprint could not be created (already exists or persistence error)", content = @Content)
        })
        @PostMapping
        public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
                try {
                        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
                        services.addNewBlueprint(bp);
                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .build();
                } catch (BlueprintPersistenceException e) {
                        return ResponseEntity
                                        .status(HttpStatus.FORBIDDEN)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        @Operation(summary = "Add a point to an existing blueprint", description = "Adds a new point (x, y) to a specific blueprint.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Point successfully added"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content)
        })
        @PutMapping("/{author}/{bpname}/points")
        public ResponseEntity<?> addPoint(
                        @PathVariable String author,
                        @PathVariable String bpname,
                        @RequestBody Point p) {
                try {
                        services.addPoint(author, bpname, p.x(), p.y());
                        return ResponseEntity
                                        .status(HttpStatus.ACCEPTED)
                                        .build();
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(Map.of("error", e.getMessage()));
                }
        }

        /**
         * Request body used to create a new Blueprint.
         */
        public record NewBlueprintRequest(
                        @NotBlank String author,
                        @NotBlank String name,
                        @Valid java.util.List<Point> points) {
        }
}
