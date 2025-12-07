package org.kunievakateryna.repository;

import org.kunievakateryna.data.Anime;
import org.kunievakateryna.data.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for managing anime data.
 * Provides CRUD operations and custom queries for anime entities.
 */
@Repository
public interface AnimeRepository extends JpaRepository<Anime, UUID>, JpaSpecificationExecutor<Anime> {

    /**
     * Checks if an anime exists with the given title, release year and author.
     *
     * @param title the anime title
     * @param releaseYear the release year
     * @param author the author
     * @return true if anime exists, false otherwise
     */
    boolean existsByTitleAndReleaseYearAndAuthor(String title, Integer releaseYear, Author author);
}