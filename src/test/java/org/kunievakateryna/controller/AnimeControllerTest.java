package org.kunievakateryna.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kunievakateryna.dto.*;
import org.kunievakateryna.repository.AnimeRepository;
import org.kunievakateryna.repository.AuthorRepository;
import org.kunievakateryna.service.AnimeService;
import org.kunievakateryna.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AnimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnimeService animeService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private UUID authorId;

    @BeforeEach
    void setUp() {
        AuthorSaveDto author = AuthorSaveDto.builder().name("Test Author").build();
        authorId = authorService.addAuthor(author).getId();
    }

    @Test
    void addAnime_shouldReturnCreatedAnime() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Naruto"))
                .andExpect(jsonPath("$.score").value(9.5))
                .andExpect(jsonPath("$.releaseYear").value(2002))
                .andExpect(jsonPath("$.authorId").value(authorId.toString()));
    }

    @Test
    void addAnime_shouldReturnDuplicate() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        animeService.addAnime(request);

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void addAnime_shouldReturnBadRequestWithEmptyTitle() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldReturnBadRequestWithTitleTooLong() throws Exception {
        String longTitle = "A".repeat(256);
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title(longTitle)
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldReturnBadRequestWithNullReleaseYear() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(null)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldReturnBadRequestWithInvalidReleaseYear() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(1899)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldReturnBadRequestWithScoreTooHigh() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(10.1)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldReturnBadRequestWithScoreTooLow() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(-0.1)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldReturnBadRequestWithNullAuthorId() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(null)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void addAnime_shouldAllowNullScore() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(null)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(post("/api/anime")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").doesNotExist());
    }


    @Test
    void getAnime_shouldReturnAnimeWithAuthor() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();
        AnimeDto created = animeService.addAnime(request);

        mockMvc.perform(get("/api/anime/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.title").value("Naruto"))
                .andExpect(jsonPath("$.score").value(9.5))
                .andExpect(jsonPath("$.releaseYear").value(2002))
                .andExpect(jsonPath("$.author.id").value(authorId.toString()))
                .andExpect(jsonPath("$.author.name").value("Test Author"));
    }

    @Test
    void getAnime_shouldReturnNotFoundForNonExistentId() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get("/api/anime/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("not found")));
    }


    @Test
    void updateAnime_shouldReturnUpdatedAnime() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();
        AnimeDto created = animeService.addAnime(request);

        AnimeSaveDto updateRequest = AnimeSaveDto.builder()
                .title("Naruto New")
                .score(9.8)
                .releaseYear(2007)
                .authorId(authorId)
                .build();

        mockMvc.perform(put("/api/anime/" + created.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId().toString()))
                .andExpect(jsonPath("$.title").value("Naruto New"))
                .andExpect(jsonPath("$.score").value(9.8))
                .andExpect(jsonPath("$.releaseYear").value(2007));
    }

    @Test
    void updateAnime_shouldReturnNotFoundForNonExistentId() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(put("/api/anime/" + nonExistentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("not found")));
    }

    @Test
    void updateAnime_shouldReturnBadRequestWithEmptyTitle() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();
        AnimeDto created = animeService.addAnime(request);

        AnimeSaveDto updateRequest = AnimeSaveDto.builder()
                .title("")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();

        mockMvc.perform(put("/api/anime/" + created.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    void updateAnime_shouldReturnDuplicate() throws Exception {
        AnimeSaveDto anime1 = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();
        AnimeSaveDto anime2 = AnimeSaveDto.builder()
                .title("One Piece")
                .score(9.0)
                .releaseYear(1999)
                .authorId(authorId)
                .build();

        AnimeDto created1 = animeService.addAnime(anime1);
        animeService.addAnime(anime2);

        AnimeSaveDto updateRequest = AnimeSaveDto.builder()
                .title("One Piece")
                .score(9.0)
                .releaseYear(1999)
                .authorId(authorId)
                .build();

        mockMvc.perform(put("/api/anime/" + created1.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());
    }


    @Test
    void deleteAnime_shouldDeleteAnime() throws Exception {
        AnimeSaveDto request = AnimeSaveDto.builder()
                .title("Naruto")
                .score(9.5)
                .releaseYear(2002)
                .authorId(authorId)
                .build();
        AnimeDto created = animeService.addAnime(request);

        mockMvc.perform(delete("/api/anime/" + created.getId()))
                .andExpect(status().isNoContent());

        assertFalse(animeRepository.existsById(created.getId()));
    }

    @Test
    void deleteAnime_shouldReturnNotFoundForNonExistentId() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/anime/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(containsString("not found")));
    }


    @Test
    void getAnimeList_shouldReturnEmptyPageInitially() throws Exception {
        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(20);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(0)))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void getAnimeList_shouldReturnListOfAnime() throws Exception {
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("One Piece").releaseYear(1999).authorId(authorId).build());

        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(20);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(2)))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.list[0].title").value("Naruto"))
                .andExpect(jsonPath("$.list[1].title").value("One Piece"));
    }

    @Test
    void getAnimeList_shouldReturnPaginatedResults() throws Exception {
        for (int i = 0; i < 5; i++) {
            animeService.addAnime(AnimeSaveDto.builder()
                                          .title("Anime " + i).releaseYear(2000 + i).authorId(authorId).build());
        }

        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(2);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(2)))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void getAnimeList_shouldFilterByAuthorId() throws Exception {
        AuthorSaveDto author2 = AuthorSaveDto.builder().name("Another Author").build();
        UUID author2Id = authorService.addAuthor(author2).getId();

        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("One Punch Man").releaseYear(2015).authorId(author2Id).build());

        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(20);
        request.setAuthorId(authorId);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(1)))
                .andExpect(jsonPath("$.list[0].title").value("Naruto"));
    }

    @Test
    void getAnimeList_shouldFilterByReleaseYear() throws Exception {
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Bleach").releaseYear(2004).authorId(authorId).build());

        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(20);
        request.setReleaseYear(2002);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(1)))
                .andExpect(jsonPath("$.list[0].title").value("Naruto"));
    }

    @Test
    void getAnimeList_shouldFilterByAuthorAndYear() throws Exception {
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Bleach").releaseYear(2004).authorId(authorId).build());

        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(20);
        request.setAuthorId(authorId);
        request.setReleaseYear(2004);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list", hasSize(1)))
                .andExpect(jsonPath("$.list[0].title").value("Bleach"));
    }

    @Test
    void getAnimeList_shouldReturnBadRequestWithInvalidYear() throws Exception {
        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(20);
        request.setReleaseYear(1899);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAnimeList_shouldReturnBadRequestWithInvalidPage() throws Exception {
        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(-1);
        request.setSize(20);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAnimeList_shouldReturnBadRequestWithInvalidSize() throws Exception {
        AnimePageRequestDto request = new AnimePageRequestDto();
        request.setPage(0);
        request.setSize(0);

        mockMvc.perform(post("/api/anime/_list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void getReport_shouldGenerateCSVReport() throws Exception {
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").score(9.5).releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("One Piece").score(9.0).releaseYear(1999).authorId(authorId).build());

        AnimeFilterDto request = new AnimeFilterDto();

        mockMvc.perform(post("/api/anime/_report")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(containsString("ID,Title,Score,Year,Author")))
                .andExpect(content().string(containsString("Naruto")))
                .andExpect(content().string(containsString("One Piece")));
    }

    @Test
    void getReport_shouldFilterReportByAuthorId() throws Exception {
        AuthorSaveDto author2 = AuthorSaveDto.builder().name("Another Author").build();
        UUID author2Id = authorService.addAuthor(author2).getId();

        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").score(9.5).releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("One Punch Man").score(8.8).releaseYear(2015).authorId(author2Id).build());

        AnimeFilterDto request = new AnimeFilterDto();
        request.setAuthorId(authorId);

        mockMvc.perform(post("/api/anime/_report")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Naruto")))
                .andExpect(content().string(not(containsString("One Punch Man"))));
    }

    @Test
    void getReport_shouldFilterReportByYear() throws Exception {
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").score(9.5).releaseYear(2002).authorId(authorId).build());
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Bleach").score(8.5).releaseYear(2004).authorId(authorId).build());

        AnimeFilterDto request = new AnimeFilterDto();
        request.setReleaseYear(2004);

        mockMvc.perform(post("/api/anime/_report")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bleach")))
                .andExpect(content().string(not(containsString("Naruto"))));
    }

    @Test
    void getReport_shouldReturnBadRequestWithInvalidYear() throws Exception {
        AnimeFilterDto request = new AnimeFilterDto();
        request.setReleaseYear(1899);

        mockMvc.perform(post("/api/anime/_report")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void uploadAnime_shouldImportValidJsonFile() throws Exception {
        String jsonContent = "[" +
                "{\"title\":\"Naruto\",\"author\":\"Test Author\",\"score\":9.5,\"year\":2002}," +
                "{\"title\":\"Bleach\",\"author\":\"Test Author\",\"score\":8.5,\"year\":2004}" +
                "]";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulImportCount").value(2))
                .andExpect(jsonPath("$.failedImportCount").value(0));

        assertEquals(2, animeRepository.count());
    }

    @Test
    void uploadAnime_shouldHandlePartialFailures() throws Exception {
        animeService.addAnime(AnimeSaveDto.builder()
                                      .title("Naruto").releaseYear(2002).authorId(authorId).build());

        String jsonContent = "[" +
                "{\"title\":\"Naruto\",\"author\":\"Test Author\",\"score\":9.5,\"year\":2002}," +
                "{\"title\":\"Bleach\",\"author\":\"Test Author\",\"score\":8.5,\"year\":2004}" +
                "]";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulImportCount").value(1))
                .andExpect(jsonPath("$.failedImportCount").value(1));
    }

    @Test
    void uploadAnime_shouldReturnBadRequestForEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.json",
                MediaType.APPLICATION_JSON_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAnime_shouldReturnBadRequestForNonJsonFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.txt",
                "text/plain",
                "some content".getBytes()
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAnime_shouldReturnBadRequestForInvalidJsonStructure() throws Exception {
        String jsonContent = "{\"invalid\":\"structure\"}";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadAnime_shouldHandleMissingAuthor() throws Exception {
        String jsonContent = "[" +
                "{\"title\":\"Naruto\",\"author\":\"Non Existent Author\",\"score\":9.5,\"year\":2002}" +
                "]";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulImportCount").value(0))
                .andExpect(jsonPath("$.failedImportCount").value(1));
    }

    @Test
    void uploadAnime_shouldHandleMissingRequiredFields() throws Exception {
        String jsonContent = "[" +
                "{\"title\":\"Naruto\",\"score\":9.5,\"year\":2002}," +
                "{\"author\":\"Test Author\",\"score\":9.5,\"year\":2002}" +
                "]";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "anime.json",
                MediaType.APPLICATION_JSON_VALUE,
                jsonContent.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/anime/upload")
                                .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.successfulImportCount").value(0))
                .andExpect(jsonPath("$.failedImportCount").value(2));
    }
}
