package lime1st.netty.auth.adapter.in.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.auth.application.port.in.TokenExpireUseCase;
import lime1st.netty.server.domain.ApiRequestTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TokenExpireEndpoint extends ApiRequestTemplate {

    private final TokenExpireUseCase tokenExpireUseCase;

    public TokenExpireEndpoint(Map<String, String> reqData, TokenExpireUseCase tokenExpireUseCase) {
        super(reqData);
        this.tokenExpireUseCase = tokenExpireUseCase;
    }

    @Override
    public Mono<Void> service(ObjectNode apiResult) {
        return tokenExpireUseCase.tokenExpire(reqData.get("token"))
                .doOnSuccess(v -> {
                    apiResult.put("resultCode", "200");
                    apiResult.put("message", "token expired");
                    apiResult.put("token", reqData.get("token"));
                })
                .onErrorResume(e -> {
                    apiResult.put("resultCode", "404");
                    apiResult.put("message", "token not exist or expired!");
                    return Mono.empty();
                });
    }

    @Override
    protected Mono<Void> validateRequest() {
        return super.validateRequest()
                .then(Mono.defer(() -> {
                    if (!reqData.containsKey("token") || reqData.get("token").isEmpty()) {
                        return Mono.error(new IllegalArgumentException("token is required"));
                    }
                    return Mono.empty();
                }));
    }
}
