package org.kunievakateryna.service;

import lombok.RequiredArgsConstructor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.kunievakateryna.dto.AuthorDto;
import org.kunievakateryna.exception.DuplicateRecordException;
import org.springframework.dao.DataIntegrityViolationException;
import org.kunievakateryna.data.Author;
import org.kunievakateryna.dto.AuthorSaveDto;
import org.kunievakateryna.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service for managing authors.
 * Handles business logic for author operations.
 */
@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Gets all authors from the database.
     *
     * @return list of all authors
     */
    public List<AuthorDto> getAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(AuthorService::convertToAuthorDto)
                .toList();
    }

    /**
     * Creates a new author.
     *
     * @param authorSaveDto the author data
     * @return the created author
     * @throws DuplicateRecordException if author with this name already exists
     */
    public AuthorDto addAuthor(AuthorSaveDto authorSaveDto) {
        if (authorRepository.existsByName(authorSaveDto.getName())) {
            throw new DuplicateRecordException(
                    "Author with name '%s' already exists".formatted(
                            authorSaveDto.getName()));
        }

        Author author = new Author();
        author.setName(authorSaveDto.getName());
        Author saved = authorRepository.save(author);
        return convertToAuthorDto(saved);
    }

    /**
     * Updates an existing author by id.
     *
     * @param id the author id
     * @param authorSaveDto the new author data
     * @return the updated author
     * @throws NoSuchElementException if author not found
     * @throws DuplicateRecordException if author with this name already exists
     */
    public AuthorDto update(UUID id, AuthorSaveDto authorSaveDto) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Author with id '%s' not found".formatted(id)));

        if (authorSaveDto.getName() != null) {
            author.setName(authorSaveDto.getName());
        }

        try {
            Author saved = authorRepository.save(author);
            entityManager.flush();
            return convertToAuthorDto(saved);
        } catch (PersistenceException | DataIntegrityViolationException ex) {
            handleDuplicateException(ex, author.getName());
            throw ex;
        }
    }

    /**
     * Deletes an author by id.
     *
     * @param id the author id
     * @throws NoSuchElementException if author not found
     */
    public void delete(UUID id) {
        if (!authorRepository.existsById(id)) {
            throw new NoSuchElementException("Author with id '%s' not found".formatted(id));
        }
        authorRepository.deleteById(id);
    }

    /**
     * Converts an Author entity to AuthorDto.
     *
     * @param author the author entity
     * @return the author DTO
     */
    private static AuthorDto convertToAuthorDto(Author author) {
        return AuthorDto.builder()
                .id(author.getId())
                .name(author.getName())
                .build();
    }

    /**
     * Handles duplicate name exceptions.
     *
     * @param ex the exception
     * @param name the author name
     * @throws DuplicateRecordException if duplicate key error is detected
     */
    private void handleDuplicateException(Exception ex, String name) {
        String message = ex.getMessage();
        if (message != null && message.toLowerCase().contains("author_name_key")) {
            throw new DuplicateRecordException(
                    "Author with name '%s' already exists".formatted(name), ex);
        }
    }
}