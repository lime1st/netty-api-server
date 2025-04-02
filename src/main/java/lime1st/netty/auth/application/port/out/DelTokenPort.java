package lime1st.netty.auth.application.port.out;

import reactor.core.publisher.Mono;

public interface DelTokenPort {

    Mono<Boolean> delToken(String token);
}
