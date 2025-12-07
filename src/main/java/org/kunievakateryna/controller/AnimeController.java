package org.kunievakateryna.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kunievakateryna.dto.*;
import org.kunievakateryna.service.AnimeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing anime.
 * Provides endpoints for creating, reading, updating, deleting anime and generating reports.
 */
@RestController
@RequestMapping("/api/anime")
@Tag(name = "Anime", description = "API for managing anime")
@RequiredArgsConstructor
public class AnimeController {
    private final AnimeService animeService;

    /**
     * Creates a new anime.
     *
     * @param animeSaveDto the anime data to create
     * @return the created anime
     */
    @PostMapping
    @Operation(summary = "Add anime", description = "Adds an anime with the provided data")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Anime added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnimeDto.class))),
            @ApiResponse(responseCode = "409", description = "Such anime already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public AnimeDto addAnime(
            @Parameter(description = "Anime data to create", required = true)
            @Valid @RequestBody AnimeSaveDto animeSaveDto
    ) {
        return animeService.addAnime(animeSaveDto);
    }

    /**
     * Gets a single anime by id with all its details.
     *
     * @param id the anime id
     * @return the anime with author information
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get anime", description = "Returns anime by id, including all dependencies")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnimeAuthorDto.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public AnimeAuthorDto getAnime(
            @Parameter(description = "ID of anime", required = true)
            @PathVariable UUID id
    ) {
        return animeService.getAnime(id);
    }

    /**
     * Updates an existing anime by id.
     *
     * @param id           the anime id
     * @param animeSaveDto the new anime data
     * @return the updated anime
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update anime", description = "Update anime data")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Anime updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AnimeDto.class))),
            @ApiResponse(responseCode = "409", description = "Such anime already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @Transactional
    public AnimeDto updateAnime(
            @Parameter(description = "ID of anime", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Anime data to update", required = true)
            @Valid @RequestBody AnimeSaveDto animeSaveDto
    ) {
        return animeService.updateAnime(id, animeSaveDto);
    }

    /**
     * Deletes an anime by id.
     *
     * @param id the anime id to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete anime", description = "Deletes anime by id")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Anime deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
    })
    public void deleteAnime(
            @Parameter(description = "ID of anime", required = true)
            @PathVariable UUID id
    ) {
        animeService.deleteAnime(id);
    }

    /**
     * Gets a paginated list of anime with optional filters.
     *
     * @param request the pagination and filter request
     * @return map with anime list and total pages
     */
    @PostMapping("/_list")
    @Operation(summary = "List pagination", description = "Returns a page with anime list and total amount of pages")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Page returned successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid filters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public Map<String, Object> getAnimeList(
            @Valid
            @Parameter(description = "Pagination + filter request", required = true)
            @RequestBody AnimePageRequestDto request
    ) {
        Page<AnimeDto> result = animeService.getAnimeList(request);

        return Map.of(
                "list", result.getContent(),
                "totalPages", result.getTotalPages()
        );
    }

    /**
     * Generates a CSV report of anime based on filters.
     *
     * @param request  the filter criteria for the report
     * @param response the HTTP response to write the CSV to
     * @throws IOException if there is an error writing to the response
     */
    @PostMapping("/_report")
    @Operation(summary = "Download CSV report", description = "Generates CSV file based on filters")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSV generated and returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void getReport(
            @Valid
            @Parameter(description = "Filters for report generation", required = true)
            @RequestBody AnimeFilterDto request,
            @Parameter(hidden = true)
            HttpServletResponse response
    ) throws IOException {
        animeService.generateReport(request, response);
    }

    /**
     * Imports anime from a JSON file.
     *
     * @param file the JSON file containing anime data
     * @return the result of the import operation
     * @throws IOException if there is an error reading the file
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import anime from JSON", description = "Uploads a JSON file with anime list")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Import completed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ImportResultDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ImportResultDto uploadAnime(
            @Parameter(description = "JSON file with anime list", required = true)
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        return animeService.importAnimeFromFile(file);
    }
}