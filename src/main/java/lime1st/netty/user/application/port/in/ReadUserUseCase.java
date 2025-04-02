package lime1st.netty.user.application.port.in;

import lime1st.netty.user.application.dto.out.FindUserQuery;
import reactor.core.publisher.Mono;

public interface ReadUserUseCase {

    Mono<FindUserQuery> readUserByEmail(String email);

    Mono<FindUserQuery> readUserByPassword(String password);
}
