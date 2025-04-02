package lime1st.netty.user.application.dto.out;

import java.time.LocalDateTime;

public record FindUserQuery(
        Long id,
        String name,
        String email,
        String password
) {
}
