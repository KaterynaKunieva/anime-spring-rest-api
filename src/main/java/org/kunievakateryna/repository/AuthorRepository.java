package org.kunievakateryna.repository;

import org.kunievakateryna.data.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing author data.
 * Provides CRUD operations and custom queries for author entities.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {

    /**
     * Checks if an author exists with the given name.
     *
     * @param name the author name
     * @return true if author with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * Finds an author by name, case-insensitive.
     *
     * @param name the author name
     * @return Optional containing the author if found, empty otherwise
     */
    Optional<Author> findByNameIgnoreCase(String name);

}