package lime1st.netty.user.application.dto.in;

import lime1st.netty.user.domain.User;

public record CreateUserCommand(
        String name,
        String email,
        String password
) {

    public static CreateUserCommand from(User user) {
        return CreateUserCommand.builder()
                .name(user.name())
                .email(user.email())
                .password(user.password())
                .build();
    }

    // 정적 내부 빌더 클래스
    private static class Builder {
        private String name;
        private String email;
        private String password;

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder email(String email) {
            this.email = email;
            return this;
        }

        Builder password(String password) {
            this.password = password;
            return this;
        }

        CreateUserCommand build() {
            return new CreateUserCommand(name, email, password);
        }
    }

    // 빌더 객체를 반환하는 정적 메서드
    private static Builder builder() {
        return new Builder();
    }
}
