package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.query.FindUserQuery;

public interface LoadUserPort {

    FindUserQuery loadUserByEmail(String email);
}
