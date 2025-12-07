package org.kunievakateryna.controller;

import org.kunievakateryna.dto.AuthorSaveDto;
import org.kunievakateryna.repository.AuthorRepository;
import org.kunievakateryna.service.AuthorService;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void getAuthors_shouldReturnEmptyListInitially() throws Exception {
        mockMvc.perform(get("/api/author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAuthors_shouldReturnListOfAuthors() throws Exception {
        authorService.addAuthor(AuthorSaveDto.builder().name("TEST-01").build());
        authorService.addAuthor(AuthorSaveDto.builder().name("TEST-02").build());

        mockMvc.perform(get("/api/author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("TEST-01"))
                .andExpect(jsonPath("$[1].name").value("TEST-02"));
    }

    @Test
    void addAuthor_shouldReturnCreatedAuthor() throws Exception {
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name("TEST-01")
                .build();

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("TEST-01"));
    }

    @Test
    void addAuthor_shouldReturnDuplicate() throws Exception {
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name("TEST-DUPLICATE")
                .build();
        authorService.addAuthor(request);

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Author with name 'TEST-DUPLICATE' already exists"))
                .andExpect(jsonPath("$.title").value("Duplicate Record"));
    }

    @Test
    void addAuthor_shouldReturnBadRequestWithEmptyName() throws Exception {
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAuthor_shouldReturnBadRequestWithNameTooLong() throws Exception {
        String longName = "A".repeat(101);
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name(longName)
                .build();

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAuthor_shouldReturnBadRequestWithOnlyWhitespace() throws Exception {
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name("   ")
                .build();

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAuthor_shouldSucceedWithMaxNameLength() throws Exception {
        String maxName = "A".repeat(100);
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name(maxName)
                .build();

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(maxName));
    }

    @Test
    void addAuthor_shouldSucceedWithSingleCharName() throws Exception {
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name("A")
                .build();

        mockMvc.perform(post("/api/author")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("A"));
    }

    @Test
    void updateAuthor_shouldReturnUpdatedAuthor() throws Exception {
        AuthorSaveDto requestCreate = AuthorSaveDto.builder()
                .name("TEST-UPDATE")
                .build();
        UUID authorId = authorService.addAuthor(requestCreate).getId();

        AuthorSaveDto requestUpdate = AuthorSaveDto.builder()
                .name("TEST-UPDATED")
                .build();

        mockMvc.perform(put("/api/author/" + authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TEST-UPDATED"))
                .andExpect(jsonPath("$.id").value(authorId.toString()));
    }

    @Test
    void updateAuthor_shouldReturnDuplicate() throws Exception {
        AuthorSaveDto requestCreate = AuthorSaveDto.builder()
                .name("TEST-UPDATE")
                .build();
        AuthorSaveDto requestDuplicate = AuthorSaveDto.builder()
                .name("TEST-DUPLICATE")
                .build();
        authorService.addAuthor(requestDuplicate);
        UUID authorId = authorService.addAuthor(requestCreate).getId();

        mockMvc.perform(put("/api/author/" + authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDuplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Author with name 'TEST-DUPLICATE' already exists"))
                .andExpect(jsonPath("$.title").value("Duplicate Record"));
    }

    @Test
    void updateAuthor_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        AuthorSaveDto request = AuthorSaveDto.builder()
                .name("ANY-NAME")
                .build();

        mockMvc.perform(put("/api/author/" + nonExistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Author with id '%s' not found".formatted(nonExistentId)));
    }

    @Test
    void updateAuthor_shouldReturnBadRequestWithNullName() throws Exception {
        AuthorSaveDto create = AuthorSaveDto.builder()
                .name("ORIGINAL")
                .build();
        UUID id = authorService.addAuthor(create).getId();

        AuthorSaveDto update = AuthorSaveDto.builder()
                .name(null)
                .build();

        mockMvc.perform(put("/api/author/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void updateAuthor_shouldReturnBadRequestWithNameTooLong() throws Exception {
        AuthorSaveDto create = AuthorSaveDto.builder()
                .name("ORIGINAL")
                .build();
        UUID id = authorService.addAuthor(create).getId();

        String longName = "B".repeat(101);
        AuthorSaveDto updateRequest = AuthorSaveDto.builder()
                .name(longName)
                .build();

        mockMvc.perform(put("/api/author/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void deleteAuthor_shouldDeleteAuthor() throws Exception {
        AuthorSaveDto author = AuthorSaveDto.builder()
                .name("TEST-DELETE")
                .build();
        UUID authorId = authorService.addAuthor(author).getId();

        mockMvc.perform(delete("/api/author/" + authorId))
                .andExpect(status().isNoContent());

        assertFalse(authorRepository.existsById(authorId));
    }

    @Test
    void deleteAuthor_shouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(delete("/api/author/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Author with id '%s' not found".formatted(nonExistentId)));
    }
}
