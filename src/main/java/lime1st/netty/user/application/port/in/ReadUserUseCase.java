package lime1st.netty.user.application.port.in;

import lime1st.netty.user.application.dto.out.FindUserQuery;

public interface ReadUserUseCase {

    FindUserQuery readUserByEmail(String email);

    FindUserQuery readUserByPassword(String password);
}
