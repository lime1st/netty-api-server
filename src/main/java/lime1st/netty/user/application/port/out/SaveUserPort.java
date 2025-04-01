package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.command.CreateUserCommand;

public interface SaveUserPort {

    long saveUser(CreateUserCommand command);
}
