package lime1st.netty.auth.application.port.out;

import reactor.core.publisher.Mono;

public interface GetTokenPort {

    Mono<String> getToken(String token);
}
