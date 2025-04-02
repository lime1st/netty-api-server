package lime1st.netty.user.application.service;

import lime1st.netty.user.application.dto.in.CreateUserCommand;
import lime1st.netty.user.application.dto.out.FindUserQuery;
import lime1st.netty.user.application.port.in.CreateUserUseCase;
import lime1st.netty.user.application.port.in.ReadUserUseCase;
import lime1st.netty.user.application.port.out.LoadUserPort;
import lime1st.netty.user.application.port.out.SaveUserPort;
import lime1st.netty.user.domain.User;
import reactor.core.publisher.Mono;

public class UserService implements CreateUserUseCase, ReadUserUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;

    public UserService(SaveUserPort saveUserPort, LoadUserPort loadUserPort) {
        this.saveUserPort = saveUserPort;
        this.loadUserPort = loadUserPort;
    }

    @Override
    public Mono<Long> createUser(CreateUserCommand command) {
        User user = User.create(
                command.name(),
                command.email(),
                command.password()
        );
        return saveUserPort.saveUser(CreateUserCommand.from(user));
    }

    @Override
    public Mono<FindUserQuery> readUserByEmail(String email) {
        return loadUserPort.loadUserByEmail(email);
    }

    @Override
    public Mono<FindUserQuery> readUserByPassword(String password) {
        return loadUserPort.loadUserByPassword(password);
    }
}
