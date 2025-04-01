package lime1st.netty.user.adapter.in.web.dto.response;

import lime1st.netty.user.application.dto.out.FindUserQuery;

import java.time.LocalDateTime;

public record FindUserResponse(
        Long id,
        String name,
        String email,
        String password,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FindUserResponse fromQuery(FindUserQuery query) {
        return new FindUserResponse(
                query.id(),
                query.name(),
                query.email(),
                query.password(),
                query.createdAt(),
                query.updatedAt()
        );
    }
}
