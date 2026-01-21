package com.kanva.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Nested
    @DisplayName("User 생성")
    class CreateUser {

        @Test
        @DisplayName("Builder로 User를 생성할 수 있다")
        void createWithBuilder() {
            // given
            String email = "test@example.com";
            String password = "password123";
            String name = "테스트유저";
            Role role = Role.USER;

            // when
            User user = User.builder()
                    .email(email)
                    .password(password)
                    .name(name)
                    .role(role)
                    .build();

            // then
            assertThat(user.getEmail()).isEqualTo(email);
            assertThat(user.getPassword()).isEqualTo(password);
            assertThat(user.getName()).isEqualTo(name);
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Role을 지정하지 않으면 기본값 USER로 설정된다")
        void createWithDefaultRole() {
            // when
            User user = User.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("테스트유저")
                    .role(null)
                    .build();

            // then
            assertThat(user.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("ADMIN Role로 User를 생성할 수 있다")
        void createAdminUser() {
            // when
            User user = User.builder()
                    .email("admin@example.com")
                    .password("admin123")
                    .name("관리자")
                    .role(Role.ADMIN)
                    .build();

            // then
            assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        }
    }

    @Nested
    @DisplayName("User 수정")
    class UpdateUser {

        @Test
        @DisplayName("이름을 수정할 수 있다")
        void updateName() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("password123")
                    .name("원래이름")
                    .role(Role.USER)
                    .build();

            // when
            user.updateName("수정된이름");

            // then
            assertThat(user.getName()).isEqualTo("수정된이름");
        }

        @Test
        @DisplayName("비밀번호를 수정할 수 있다")
        void updatePassword() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .password("oldPassword")
                    .name("테스트유저")
                    .role(Role.USER)
                    .build();

            // when
            user.updatePassword("newPassword");

            // then
            assertThat(user.getPassword()).isEqualTo("newPassword");
        }
    }

    @Nested
    @DisplayName("Role enum")
    class RoleEnum {

        @Test
        @DisplayName("USER Role의 설명을 조회할 수 있다")
        void getUserRoleDescription() {
            // when & then
            assertThat(Role.USER.getDescription()).isEqualTo("일반 사용자");
        }

        @Test
        @DisplayName("ADMIN Role의 설명을 조회할 수 있다")
        void getAdminRoleDescription() {
            // when & then
            assertThat(Role.ADMIN.getDescription()).isEqualTo("관리자");
        }
    }
}
