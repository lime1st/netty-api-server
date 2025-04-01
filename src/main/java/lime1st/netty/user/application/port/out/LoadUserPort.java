package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.out.FindUserQuery;

public interface LoadUserPort {

    FindUserQuery loadUserByEmail(String email);

    FindUserQuery loadUserByPassword(String password);
}
