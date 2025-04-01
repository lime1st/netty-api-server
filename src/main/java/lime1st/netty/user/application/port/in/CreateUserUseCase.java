package lime1st.netty.user.application.port.in;

import lime1st.netty.user.application.dto.command.CreateUserCommand;

public interface CreateUserUseCase {

    long createUser(CreateUserCommand command);
}
