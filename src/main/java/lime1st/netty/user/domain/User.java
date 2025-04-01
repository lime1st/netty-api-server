package lime1st.netty.user.domain;

public record User(
        long id,
        String name,
        String email,
        String password
) {

    public static User create(String name, String email, String password) {
        return new User(
                0L,
                name,
                email,
                password
        );
    }

    // 비즈니스 로직이랄게 딱히 없다.
    public User updateWithPassword(String password) {
        return new User(
                id,
                name,
                email,
                password
        );
    }
}
