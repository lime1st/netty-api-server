package lime1st.netty.auth.application.port.in;

import reactor.core.publisher.Mono;

public interface TokenVerifyUseCase {

    Mono<String> verifyToken(String token);
}
