package lime1st.netty.auth.domain;

public record Token(
        long userId,
        String email,
        long issuedDate,
        long expireDate
) {

    public boolean isExpired() {
        return System.currentTimeMillis() > expireDate;
    }
}
