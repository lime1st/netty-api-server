package lime1st.netty.auth.application.port.in;

import reactor.core.publisher.Mono;

public interface TokenIssueUseCase {

    Mono<String> issueToken(long userId, String email);
}
