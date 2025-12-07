package org.kunievakateryna.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kunievakateryna.dto.AuthorDto;
import org.kunievakateryna.dto.AuthorSaveDto;
import org.kunievakateryna.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing authors.
 * Provides endpoints for creating, reading, updating and deleting authors.
 */
@RestController
@RequestMapping("/api/author")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "API for managing authors")
public class AuthorController {

    private final AuthorService authorService;

    /**
     * Gets all authors from the database.
     *
     * @return list of all authors
     */
    @GetMapping
    @Operation(summary = "Get list of authors", description = "Returns list of authors")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of authors",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AuthorDto.class))))
    })
    public List<AuthorDto> getAuthors() {
        return authorService.getAuthors();
    }

    /**
     * Creates a new author.
     *
     * @param authorAddDto the author data to save
     * @return the created author
     */
    @PostMapping
    @Operation(summary = "Add author", description = "Adds a new author with the provided data")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Author added successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "409", description = "Such author already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @Transactional
    public AuthorDto addAuthor(@Valid @RequestBody AuthorSaveDto authorAddDto) {
        return authorService.addAuthor(authorAddDto);
    }

    /**
     * Updates an existing author by id.
     *
     * @param id            the author id
     * @param authorSaveDto the new author data
     * @return the updated author
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update author", description = "Updates an existing author with new data")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Author updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthorDto.class))),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Author with such name already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class)))
    })
    @Transactional
    public AuthorDto updateAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable("id") UUID id,
            @Valid @Parameter(description = "Updated author data", required = true) @RequestBody AuthorSaveDto authorSaveDto) {
        return authorService.update(id, authorSaveDto);
    }

    /**
     * Deletes an author by id.
     *
     * @param id the author id to delete
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete author", description = "Deletes an author")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProblemDetail.class))),
    })
    @Transactional
    public void deleteAuthor(
            @Parameter(description = "Author ID", required = true) @PathVariable("id") UUID id
    ) {
        authorService.delete(id);
    }
}