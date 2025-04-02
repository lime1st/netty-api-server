package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.out.FindUserQuery;
import reactor.core.publisher.Mono;

public interface ReadUserPort {

    Mono<FindUserQuery> readUserById(String userId);

    Mono<FindUserQuery> readUserByEmail(String email);
}
