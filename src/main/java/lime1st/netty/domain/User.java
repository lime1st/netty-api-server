package lime1st.netty.domain;

public record User(
        Long id, String email, String username, String password
) {
}
