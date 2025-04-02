package lime1st.netty.auth.application.port.in;

import reactor.core.publisher.Mono;

public interface TokenExpireUseCase {

    Mono<Void> tokenExpire(String token);
}
