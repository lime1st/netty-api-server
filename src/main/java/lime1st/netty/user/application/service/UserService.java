package lime1st.netty.user.application.service;

import lime1st.netty.user.application.dto.command.CreateUserCommand;
import lime1st.netty.user.application.dto.query.FindUserQuery;
import lime1st.netty.user.application.port.in.CreateUserUseCase;
import lime1st.netty.user.application.port.in.ReadUserUseCase;
import lime1st.netty.user.application.port.out.LoadUserPort;
import lime1st.netty.user.application.port.out.SaveUserPort;
import lime1st.netty.user.domain.User;

public class UserService implements CreateUserUseCase, ReadUserUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;

    public UserService(SaveUserPort saveUserPort, LoadUserPort loadUserPort) {
        this.saveUserPort = saveUserPort;
        this.loadUserPort = loadUserPort;
    }

    @Override
    public long createUser(CreateUserCommand command) {
        User user = User.create(
                command.name(),
                command.email(),
                command.password()
        );
        return saveUserPort.saveUser(CreateUserCommand.from(user));
    }

    @Override
    public FindUserQuery readUserByEmail(String email) {
        return loadUserPort.loadUserByEmail(email);
    }
}
