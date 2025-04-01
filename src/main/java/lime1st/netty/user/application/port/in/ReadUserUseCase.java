package lime1st.netty.user.application.port.in;

import lime1st.netty.user.application.dto.query.FindUserQuery;

public interface ReadUserUseCase {

    FindUserQuery readUserByEmail(String email);
}
