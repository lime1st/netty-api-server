package lime1st.netty.user.adapter.out.persistence;

import lime1st.netty.user.application.dto.command.CreateUserCommand;
import lime1st.netty.user.application.dto.query.FindUserQuery;

public class UserMapper {

    public UserJpaEntity mapToJpaEntityFrom(CreateUserCommand command) {
        return UserJpaEntity.builder()
                .name(command.name())
                .email(command.email())
                .password(command.password())
                .build();
    }

    public FindUserQuery mapToQueryFrom(UserJpaEntity jpaEntity) {
        return new FindUserQuery(
                jpaEntity.getId(),
                jpaEntity.getName(),
                jpaEntity.getEmail(),
                jpaEntity.getPassword(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }


}
