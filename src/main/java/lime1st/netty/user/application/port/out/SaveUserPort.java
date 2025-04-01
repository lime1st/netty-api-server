package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.in.CreateUserCommand;

public interface SaveUserPort {

    long saveUser(CreateUserCommand command);
}
