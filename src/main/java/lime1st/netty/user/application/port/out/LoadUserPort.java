package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.out.FindUserQuery;
import reactor.core.publisher.Mono;

public interface LoadUserPort {

    Mono<FindUserQuery> loadUserByEmail(String email);

    Mono<FindUserQuery> loadUserByPassword(String password);
}
