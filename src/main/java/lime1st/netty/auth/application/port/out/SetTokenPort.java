package lime1st.netty.auth.application.port.out;

import lime1st.netty.auth.domain.Token;
import reactor.core.publisher.Mono;

public interface SetTokenPort {

    Mono<String> setToken(Token token);
}
