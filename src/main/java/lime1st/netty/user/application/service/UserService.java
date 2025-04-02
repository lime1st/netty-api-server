package lime1st.netty.user.application.service;

import lime1st.netty.user.application.dto.in.CreateUserCommand;
import lime1st.netty.user.application.dto.out.FindUserQuery;
import lime1st.netty.user.application.port.in.CreateUserUseCase;
import lime1st.netty.user.application.port.in.ReadUserUseCase;
import lime1st.netty.user.application.port.out.CreateUserPort;
import lime1st.netty.user.application.port.out.ReadUserPort;
import lime1st.netty.user.domain.User;
import reactor.core.publisher.Mono;

public class UserService implements CreateUserUseCase, ReadUserUseCase {

    private final CreateUserPort createUserPort;
    private final ReadUserPort readUserPort;

    public UserService(CreateUserPort createUserPort, ReadUserPort readUserPort) {
        this.createUserPort = createUserPort;
        this.readUserPort = readUserPort;
    }

    @Override
    public Mono<Long> createUser(CreateUserCommand command) {
        User user = User.create(
                command.name(),
                command.email(),
                command.password()
        );
        return createUserPort.createUser(CreateUserCommand.from(user));
    }

    @Override
    public Mono<FindUserQuery> readUserById(String userId) {
        return readUserPort.readUserById(userId);
    }

    @Override
    public Mono<FindUserQuery> readUserByEmail(String email) {
        return readUserPort.readUserByEmail(email);
    }
}
