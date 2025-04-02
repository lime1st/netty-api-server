package lime1st.netty.auth.application.service;

import lime1st.netty.auth.adapter.out.persistence.TokenRedisAdapter;
import lime1st.netty.auth.application.port.in.TokenExpireUseCase;
import lime1st.netty.auth.application.port.in.TokenIssueUseCase;
import lime1st.netty.auth.application.port.in.TokenVerifyUseCase;
import lime1st.netty.auth.application.port.out.DelTokenPort;
import lime1st.netty.auth.application.port.out.GetTokenPort;
import lime1st.netty.auth.application.port.out.SetTokenPort;
import lime1st.netty.auth.domain.Token;
import reactor.core.publisher.Mono;

public class TokenService implements TokenIssueUseCase, TokenVerifyUseCase, TokenExpireUseCase {

    private final SetTokenPort setTokenPort;
    private final GetTokenPort getTokenPort;
    private final DelTokenPort delTokenPort;

    public TokenService(TokenRedisAdapter tokenRedisAdapter) {
        this.setTokenPort = (SetTokenPort) tokenRedisAdapter;
        this.getTokenPort = (GetTokenPort) tokenRedisAdapter;
        this.delTokenPort = (DelTokenPort) tokenRedisAdapter;
    }

    @Override
    public Mono<String> issueToken(long userId, String email) {
        Token token = new Token(userId, email, System.currentTimeMillis(), System.currentTimeMillis() + 3600000);
        return setTokenPort.setToken(token)
                .onErrorMap(e -> new RuntimeException("Failed to set token: " + e));

    }

    @Override
    public Mono<String> verifyToken(String token) {
        return getTokenPort.getToken(token)
                .onErrorMap(e -> new RuntimeException("Failed to get token: " + e));
    }

    @Override
    public Mono<Void> tokenExpire(String token) {
        return delTokenPort.delToken(token)
                .onErrorMap(e -> new RuntimeException("Failed to del token: " + e))
                .then(Mono.empty());
    }
}
