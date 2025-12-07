package org.kunievakateryna.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kunievakateryna.data.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    private Author author;

    @BeforeEach
    void setUp() {
        author = new Author();
        author.setName("John Doe");
        authorRepository.save(author);
    }

    @Test
    void testExistsByName_WhenExists() {
        boolean exists = authorRepository.existsByName("John Doe");
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByName_WhenNotExists() {
        boolean exists = authorRepository.existsByName("Non-existent Author");
        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByName_WithNull() {
        boolean exists = authorRepository.existsByName(null);
        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByName_WithEmptyString() {
        boolean exists = authorRepository.existsByName("");
        assertThat(exists).isFalse();
    }

    @Test
    void testFindByNameIgnoreCase_LowercaseSearch() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase("john doe");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByNameIgnoreCase_UppercaseSearch() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase("JOHN DOE");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByNameIgnoreCase_MixedCaseSearch() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase("JoHn DoE");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void testFindByNameIgnoreCase_ExactMatch() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase("John Doe");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(author.getId());
    }

    @Test
    void testFindByNameIgnoreCase_NotFound() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase("Non-existent Author");
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByNameIgnoreCase_WithNull() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase(null);
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByNameIgnoreCase_WithEmptyString() {
        Optional<Author> found = authorRepository.findByNameIgnoreCase("");
        assertThat(found).isEmpty();
    }

    @Test
    void testFindByNameIgnoreCase_MultipleAuthors() {
        Author secondAuthor = new Author();
        secondAuthor.setName("test testing");
        authorRepository.save(secondAuthor);

        Optional<Author> found = authorRepository.findByNameIgnoreCase("TEST TESTING");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("test testing");
    }
}