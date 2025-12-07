package org.kunievakateryna.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kunievakateryna.data.Anime;
import org.kunievakateryna.data.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AnimeRepositoryTest {

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Author author;
    private Anime anime;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setName("Test Author");
        authorRepository.save(author);

        anime = new Anime();
        anime.setTitle("Test Anime");
        anime.setReleaseYear(2023);
        anime.setAuthor(author);
        animeRepository.save(anime);
    }

    @Test
    void testExistsByTitleAndReleaseYearAndAuthor_WhenExists() {
        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(
                "Test Anime", 2023, author
        );

        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByTitleAndReleaseYearAndAuthor_WhenNotExists() {
        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(
                "Non-existent", 2020, author
        );

        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByTitleAndReleaseYearAndAuthor_DifferentYear() {
        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(
                "Test Anime", 2022, author
        );

        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByTitleAndReleaseYearAndAuthor_DifferentAuthor() {
        Author anotherAuthor = new Author();
        anotherAuthor.setName("Another Author");
        authorRepository.save(anotherAuthor);

        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(
                "Test Anime", 2023, anotherAuthor
        );

        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByTitleAndReleaseYearAndAuthor_WithNullValues() {
        boolean exists = animeRepository.existsByTitleAndReleaseYearAndAuthor(
                null, null, null
        );

        assertThat(exists).isFalse();
    }
}