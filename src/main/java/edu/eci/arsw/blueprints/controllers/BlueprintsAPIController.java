package edu.eci.arsw.blueprints.controllers;

import java.util.Set;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@CrossOrigin(origins = "*")
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
        public ResponseEntity<ApiResponse<Object>> byAuthor(@PathVariable String author) {
                try {
                        Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
                        return ResponseEntity.ok(new ApiResponse<>(
                                        HttpStatus.OK.value(),
                                        "Blueprints found for author: " + author,
                                        blueprints));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
                }
        }

        @Operation(summary = "Get a blueprint by author and name", description = "Returns a specific blueprint identified by author and blueprint name.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Blueprint.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content)
        })
        @GetMapping("/{author}/{bpname}")
        public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
                        @PathVariable String author,
                        @PathVariable String bpname) {
                try {
                        Blueprint bp = services.getBlueprint(author, bpname);
                        return ResponseEntity.ok(new ApiResponse<>(
                                        HttpStatus.OK.value(),
                                        "Blueprint found",
                                        bp));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
                }
        }

        @Operation(summary = "Create a new blueprint", description = "Creates a new blueprint with the provided author, name and points.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint successfully created"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Blueprint could not be created (already exists or persistence error)", content = @Content)
        })
        @PostMapping
        public ResponseEntity<ApiResponse<Void>> add(@Valid @RequestBody NewBlueprintRequest req) {
                try {
                        Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
                        services.addNewBlueprint(bp);
                        return ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(new ApiResponse<>(HttpStatus.CREATED.value(),
                                                        "Blueprint created successfully", null));
                } catch (BlueprintPersistenceException e) {
                        return ResponseEntity
                                        .status(HttpStatus.FORBIDDEN)
                                        .body(new ApiResponse<>(HttpStatus.FORBIDDEN.value(), e.getMessage(), null));
                }
        }

        @Operation(summary = "Add a point to an existing blueprint", description = "Adds a new point (x, y) to a specific blueprint.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Point successfully added"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found", content = @Content)
        })
        @PutMapping("/{author}/{bpname}/points")
        public ResponseEntity<ApiResponse<Void>> addPoint(
                        @PathVariable String author,
                        @PathVariable String bpname,
                        @RequestBody Point p) {
                try {
                        services.addPoint(author, bpname, p.x(), p.y());
                        return ResponseEntity
                                        .status(HttpStatus.ACCEPTED)
                                        .body(new ApiResponse<>(HttpStatus.ACCEPTED.value(), "Point added successfully",
                                                        null));
                } catch (BlueprintNotFoundException e) {
                        return ResponseEntity
                                        .status(HttpStatus.NOT_FOUND)
                                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
                }
        }

        @DeleteMapping("/{author}/{bpname}")
        public ResponseEntity<ApiResponse<Void>> delete(
                @PathVariable String author,
                @PathVariable String bpname) {
            try {
                services.deleteBlueprint(author, bpname);
                return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Blueprint deleted", null));
            } catch (BlueprintNotFoundException e) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), e.getMessage(), null));
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
