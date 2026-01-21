package com.kanva.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password123")
                .name("테스트유저")
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("findByEmail 메서드")
    class FindByEmail {

        @Test
        @DisplayName("이메일로 User를 조회할 수 있다")
        void success() {
            // given
            em.persist(testUser);
            em.flush();
            em.clear();

            // when
            Optional<User> found = userRepository.findByEmail("test@example.com");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
            assertThat(found.get().getName()).isEqualTo("테스트유저");
            assertThat(found.get().getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회 시 빈 Optional을 반환한다")
        void notFound() {
            // when
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail 메서드")
    class ExistsByEmail {

        @Test
        @DisplayName("이메일이 존재하면 true를 반환한다")
        void exists() {
            // given
            em.persist(testUser);
            em.flush();
            em.clear();

            // when
            boolean exists = userRepository.existsByEmail("test@example.com");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("이메일이 존재하지 않으면 false를 반환한다")
        void notExists() {
            // when
            boolean exists = userRepository.existsByEmail("nonexistent@example.com");

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("save 메서드")
    class Save {

        @Test
        @DisplayName("User를 저장하면 ID가 생성된다")
        void saveGeneratesId() {
            // when
            User saved = userRepository.save(testUser);

            // then
            assertThat(saved.getId()).isNotNull();
        }

        @Test
        @DisplayName("User를 저장하면 createdAt과 updatedAt이 자동 설정된다")
        void saveGeneratesAuditFields() {
            // when
            User saved = userRepository.save(testUser);
            em.flush();

            // then
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById 메서드")
    class FindById {

        @Test
        @DisplayName("ID로 User를 조회할 수 있다")
        void success() {
            // given
            em.persist(testUser);
            em.flush();
            em.clear();

            Long savedId = testUser.getId();

            // when
            Optional<User> found = userRepository.findById(savedId);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(savedId);
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
        void notFound() {
            // when
            Optional<User> found = userRepository.findById(999L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete 메서드")
    class Delete {

        @Test
        @DisplayName("User를 삭제할 수 있다")
        void deleteSuccess() {
            // given
            em.persist(testUser);
            em.flush();
            em.clear();

            Long savedId = testUser.getId();

            // when
            User toDelete = userRepository.findById(savedId).orElseThrow();
            userRepository.delete(toDelete);
            em.flush();
            em.clear();

            // then
            Optional<User> found = userRepository.findById(savedId);
            assertThat(found).isEmpty();
        }
    }
}
