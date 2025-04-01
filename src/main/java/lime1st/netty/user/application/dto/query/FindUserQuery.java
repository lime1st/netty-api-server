package lime1st.netty.user.application.dto.query;

import java.time.LocalDateTime;

public record FindUserQuery(
        Long id,
        String name,
        String email,
        String password,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
