package org.kunievakateryna.service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kunievakateryna.config.RabbitConfig;
import org.kunievakateryna.data.Anime;
import org.kunievakateryna.data.Author;
import org.kunievakateryna.dto.*;
import org.kunievakateryna.exception.DuplicateRecordException;
import org.kunievakateryna.repository.AnimeRepository;
import org.kunievakateryna.repository.AuthorRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Service for managing anime.
 * Handles business logic for anime operations including CRUD, filtering, reporting and imports.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnimeService {
    private final AnimeRepository animeRepository;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.email-queue}")
    private String emailQueue;

    @Value("${app.mail.admin-email}")
    private String adminEmail;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Batch size for importing anime from JSON file
     */
    private static final int BATCH_SIZE = 200;

    /**
     * Creates a new anime.
     *
     * @param animeSaveDto the anime data
     * @return the created anime
     * @throws NoSuchElementException   if author not found
     * @throws DuplicateRecordException if anime with the same title, year and author already exists
     */
    public AnimeDto addAnime(AnimeSaveDto animeSaveDto) {
        Author author = getAuthorById(animeSaveDto.getAuthorId());
        checkAnimeDuplicate(animeSaveDto.getTitle(), animeSaveDto.getReleaseYear(), author);

        Anime anime = createAnime(animeSaveDto, author);
        Anime saved = animeRepository.save(anime);
        sendNewAnimeEmail(saved);
        return convertToAnimeDto(saved);
    }

    /**
     * Gets a single anime by id with author information.
     *
     * @param id the anime id
     * @return the anime with author details
     * @throws NoSuchElementException if anime not found
     */
    public AnimeAuthorDto getAnime(UUID id) {
        Anime anime = getAnimeById(id);
        return convertToAnimeAuthorDto(anime, anime.getAuthor());
    }

    /**
     * Updates an existing anime by id.
     *
     * @param id           the anime id
     * @param animeSaveDto the new anime data
     * @return the updated anime
     * @throws NoSuchElementException   if anime or author not found
     * @throws DuplicateRecordException if anime with the same title, year and author already exists
     */
    public AnimeDto updateAnime(UUID id, AnimeSaveDto animeSaveDto) {
        Anime anime = getAnimeById(id);
        Author newAuthor = getAuthorById(animeSaveDto.getAuthorId());

        if (isDifferentFromCurrent(anime, animeSaveDto, newAuthor)) {
            checkAnimeDuplicate(animeSaveDto.getTitle(), animeSaveDto.getReleaseYear(), newAuthor);
        }

        anime.setAuthor(newAuthor);
        anime.setTitle(animeSaveDto.getTitle());
        anime.setReleaseYear(animeSaveDto.getReleaseYear());
        anime.setScore(animeSaveDto.getScore());

        try {
            Anime saved = animeRepository.save(anime);
            entityManager.flush();
            return convertToAnimeDto(saved);
        } catch (DataIntegrityViolationException ex) {
            throw ex;
        }
    }

    /**
     * Deletes an anime by id.
     *
     * @param id the anime id
     * @throws NoSuchElementException if anime not found
     */
    public void deleteAnime(UUID id) {
        if (!animeRepository.existsById(id)) {
            throw new NoSuchElementException("Anime with id '%s' not found".formatted(id));
        }
        animeRepository.deleteById(id);
    }

    /**
     * Gets a paginated list of anime with optional filters.
     *
     * @param request the pagination and filter request
     * @return page of anime
     */
    public Page<AnimeDto> getAnimeList(AnimePageRequestDto request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Specification<Anime> specification = createSpecification(request.getAuthorId(), request.getReleaseYear());
        return animeRepository.findAll(specification, pageable).map(AnimeService::convertToAnimeDto);
    }

    /**
     * Generates a CSV report of anime based on filters.
     *
     * @param request  the filter criteria
     * @param response the HTTP response to write the CSV to
     * @throws IOException if there is an error writing to the response
     */
    public void generateReport(AnimeFilterDto request, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"anime_report.csv\"");

        Specification<Anime> spec = createSpecification(request.getAuthorId(), request.getReleaseYear());
        List<Anime> animeList = animeRepository.findAll(spec);

        try (PrintWriter writer = response.getWriter()) {
            writer.println("ID,Title,Score,Year,Author");

            for (Anime anime : animeList) {
                writer.printf("%s,\"%s\",%s,%d,\"%s\"%n",
                              anime.getId(),
                              escapeCsv(anime.getTitle()),
                              anime.getScore(),
                              anime.getReleaseYear(),
                              escapeCsv(anime.getAuthor().getName())
                );
            }
        }
    }

    /**
     * Imports anime from a JSON file.
     * Processes file in batches and caches authors for efficiency.
     *
     * @param file the JSON file containing anime data
     * @return the result of the import operation
     * @throws IOException              if there is an error reading the file
     * @throws IllegalArgumentException if file is invalid
     */
    public ImportResultDto importAnimeFromFile(MultipartFile file) throws IOException {
        validateFile(file);

        JsonParser parser = createJsonParser(file);
        validateJsonArray(parser);

        int success = 0;
        int fail = 0;
        Map<String, Author> authorCache = new HashMap<>();
        List<Anime> batch = new ArrayList<>(BATCH_SIZE);

        while (parser.nextToken() == JsonToken.START_OBJECT) {
            AnimeImportDto dto = parseAnimeDto(parser);

            if (dto == null) {
                fail++;
                continue;
            }

            if (!validateAnimeData(dto)) {
                fail++;
                continue;
            }

            Author author = getAuthorFromCache(dto, authorCache);
            if (author == null) {
                fail++;
                continue;
            }

            if (animeExists(dto, author)) {
                fail++;
                continue;
            }

            Anime anime = createAnime(dto, author);
            batch.add(anime);

            if (batch.size() >= BATCH_SIZE) {
                int savedCount = saveBatch(batch);
                success += savedCount;
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            int savedCount = saveBatch(batch);
            success += savedCount;
        }

        return ImportResultDto.builder()
                .successfulImportCount(success)
                .failedImportCount(fail)
                .build();
    }

    /**
     * Validates the uploaded file.
     *
     * @param file the file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!MediaType.APPLICATION_JSON_VALUE.equals(file.getContentType())) {
            throw new IllegalArgumentException("File content type must be application/json");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".json")) {
            throw new IllegalArgumentException("File extension must be .json");
        }
    }

    /**
     * Creates a JSON parser from the file.
     *
     * @param file the file to parse
     * @return the JSON parser
     * @throws IOException if there is an error reading the file
     */
    private JsonParser createJsonParser(MultipartFile file) throws IOException {
        JsonFactory factory = objectMapper.getFactory();
        return factory.createParser(file.getInputStream());
    }

    /**
     * Validates that the JSON is an array.
     *
     * @param parser the JSON parser
     * @throws IllegalArgumentException if JSON is not an array
     */
    private void validateJsonArray(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalArgumentException("JSON must be an array");
        }
    }

    /**
     * Parses a single anime DTO from the JSON.
     *
     * @param parser the JSON parser
     * @return the parsed DTO or null if parsing failed
     */
    private AnimeImportDto parseAnimeDto(JsonParser parser) {
        try {
            return objectMapper.readValue(parser, AnimeImportDto.class);
        } catch (Exception ex) {
            System.out.println("Error import: Failed to parse JSON");
            return null;
        }
    }

    /**
     * Validates anime data (title, author, year).
     *
     * @param dto the anime DTO
     * @return true if valid, false otherwise
     */
    private boolean validateAnimeData(AnimeImportDto dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank() ||
                dto.getAuthor() == null || dto.getAuthor().isBlank() ||
                dto.getYear() == null) {
            System.out.println("Error import: Missing required fields (Title, Author or Year): " + dto);
            return false;
        }
        return true;
    }

    /**
     * Gets an author from cache or database.
     *
     * @param dto         the anime DTO
     * @param authorCache the author cache
     * @return the author or null if not found
     */
    private Author getAuthorFromCache(AnimeImportDto dto, Map<String, Author> authorCache) {
        Author author = authorCache.get(dto.getAuthor());

        if (author == null) {
            author = authorRepository.findByNameIgnoreCase(dto.getAuthor()).orElse(null);
            if (author == null) {
                System.out.println("Error import: Author not found: " + dto);
                return null;
            }
            authorCache.put(dto.getAuthor(), author);
        }

        return author;
    }

    /**
     * Checks if anime already exists.
     *
     * @param dto    the anime DTO
     * @param author the author
     * @return true if exists, false otherwise
     */
    private boolean animeExists(AnimeImportDto dto, Author author) {
        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(
                dto.getTitle(),
                dto.getYear(),
                author
        );

        if (exists) {
            System.out.println("Error import: Such anime already exists " + dto);
        }

        return exists;
    }

    /**
     * Saves a batch of anime.
     *
     * @param batch the batch to save
     * @return the number of successfully saved anime
     */
    private int saveBatch(List<Anime> batch) {
        try {
            animeRepository.saveAll(batch);
            return batch.size();
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * Gets an author by id.
     *
     * @param authorId the author id
     * @return the author
     * @throws NoSuchElementException if author not found
     */
    private Author getAuthorById(UUID authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new NoSuchElementException("Author not found"));
    }

    /**
     * Gets an anime by id.
     *
     * @param id the anime id
     * @return the anime
     * @throws NoSuchElementException if anime not found
     */
    private Anime getAnimeById(UUID id) {
        return animeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Anime with id '%s' not found".formatted(id)));
    }

    /**
     * Checks if anime with the same title, year and author already exists.
     *
     * @param title  the anime title
     * @param year   the release year
     * @param author the author
     * @throws DuplicateRecordException if anime already exists
     */
    private void checkAnimeDuplicate(String title, Integer year, Author author) {
        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(title, year, author);
        if (exists) {
            throw new DuplicateRecordException(
                    "Anime with title '%s' and year %d by author '%s' already exists"
                            .formatted(title, year, author.getName())
            );
        }
    }

    /**
     * Checks if anime data differs from the current values.
     *
     * @param anime        the current anime
     * @param animeSaveDto the new anime data
     * @param newAuthor    the new author
     * @return true if data differs, false otherwise
     */
    private boolean isDifferentFromCurrent(Anime anime, AnimeSaveDto animeSaveDto, Author newAuthor) {
        return !anime.getTitle().equals(animeSaveDto.getTitle()) ||
                !anime.getReleaseYear().equals(animeSaveDto.getReleaseYear()) ||
                !anime.getAuthor().getId().equals(newAuthor.getId());
    }

    /**
     * Creates an Anime entity from AnimeSaveDto.
     *
     * @param dto    the anime save DTO
     * @param author the author
     * @return the anime entity
     */
    private Anime createAnime(AnimeSaveDto dto, Author author) {
        Anime anime = new Anime();
        anime.setTitle(dto.getTitle());
        anime.setReleaseYear(dto.getReleaseYear());
        anime.setScore(dto.getScore());
        anime.setAuthor(author);
        return anime;
    }

    /**
     * Creates an Anime entity from AnimeImportDto.
     *
     * @param dto    the anime import DTO
     * @param author the author
     * @return the anime entity
     */
    private Anime createAnime(AnimeImportDto dto, Author author) {
        Anime anime = new Anime();
        anime.setTitle(dto.getTitle());
        anime.setScore(dto.getScore());
        anime.setReleaseYear(dto.getYear());
        anime.setAuthor(author);
        return anime;
    }

    /**
     * Converts an Anime entity to AnimeDto.
     *
     * @param anime the anime entity
     * @return the anime DTO
     */
    private static AnimeDto convertToAnimeDto(Anime anime) {
        return AnimeDto.builder()
                .id(anime.getId())
                .title(anime.getTitle())
                .score(anime.getScore())
                .releaseYear(anime.getReleaseYear())
                .authorId(anime.getAuthor().getId())
                .build();
    }

    /**
     * Converts an Anime entity and Author to AnimeAuthorDto.
     *
     * @param anime  the anime entity
     * @param author the author entity
     * @return the anime with author DTO
     */
    private static AnimeAuthorDto convertToAnimeAuthorDto(Anime anime, Author author) {
        AuthorDto authorDto = AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .build();

        return AnimeAuthorDto.builder()
                .id(anime.getId())
                .title(anime.getTitle())
                .score(anime.getScore())
                .releaseYear(anime.getReleaseYear())
                .author(authorDto)
                .build();
    }

    /**
     * Creates a specification for filtering anime.
     *
     * @param authorId the author id filter (optional)
     * @param year     the release year filter (optional)
     * @return the specification for filtering
     */
    private Specification<Anime> createSpecification(UUID authorId, Integer year) {
        List<Specification<Anime>> specs = new ArrayList<>();

        if (authorId != null) {
            specs.add((root, query, cb) ->
                              cb.equal(root.get("author").get("id"), authorId));
        }

        if (year != null) {
            specs.add((root, query, cb) ->
                              cb.equal(root.get("releaseYear"), year));
        }

        if (specs.isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        return Specification.allOf(specs);
    }

    /**
     * Escapes special characters in CSV values.
     *
     * @param input the input string
     * @return the escaped string
     */
    private String escapeCsv(String input) {
        if (input == null) return "";

        if (input.contains(",") || input.contains("\"") || input.contains("\n")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    /**
     * Sends email notification to admin about adding new anime.
     *
     * @param anime added anime.
     */
    private void sendNewAnimeEmail(Anime anime) {
        String emailBody = String.format(
                "New anime:%n" +
                "Title:        %s%n" +
                "Release Year: %d%n" +
                "Score:        %s%n" +
                "Author:       %s%n",
                anime.getTitle(),
                anime.getReleaseYear(),
                anime.getScore(),
                anime.getAuthor().getName()
        );

        EmailMessage message = EmailMessage.builder()
                .recipient(adminEmail)
                .subject("New Anime Notification")
                .body(String.format(emailBody))
                .build();
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_EMAIL_NOTIFICATIONS, "", message);
        } catch (Exception e) {
            log.error("Failed to send RabbitMQ message: {}", e.getMessage());
        }
    }

}