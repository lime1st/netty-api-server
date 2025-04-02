package lime1st.netty.auth.adapter.in.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.server.domain.ApiRequestTemplate;
import lime1st.netty.auth.application.port.in.TokenIssueUseCase;
import lime1st.netty.user.application.port.in.ReadUserUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TokenIssueEndpoint extends ApiRequestTemplate {

    private static final Logger log = LoggerFactory.getLogger(TokenIssueEndpoint.class);
    private final TokenIssueUseCase tokenIssueUseCase;
    private final ReadUserUseCase readUserUseCase;

    public TokenIssueEndpoint(
            Map<String, String> reqData,
            TokenIssueUseCase tokenIssueUseCase,
            ReadUserUseCase readUserUseCase
    ) {
        super(reqData);
        this.tokenIssueUseCase = tokenIssueUseCase;
        this.readUserUseCase = readUserUseCase;
    }

    @Override
    public Mono<Void> service(ObjectNode apiResult) {

        // TODO: password 검증을 해야..
        String password = reqData.get("password");
        return readUserUseCase.readUserById(reqData.get("userId"))
                .flatMap(findUser ->
                        tokenIssueUseCase.issueToken(findUser.id(), findUser.email()))
                .doOnNext(tokenKey -> {
                    log.info("Token issue: {}", tokenKey);
                    apiResult.put("resultCode", "200");
                    apiResult.put("tokenKey", tokenKey);})
                .then();
    }

    @Override
    protected Mono<Void> validateRequest() {
        return super.validateRequest()
                .then(Mono.defer(() -> {
                    if (!reqData.containsKey("userId") || reqData.get("userId").isEmpty()) {
                        return Mono.error(new IllegalArgumentException("userId is required"));
                    }
                    if (!reqData.containsKey("password") || reqData.get("password").isEmpty()) {
                        return Mono.error(new IllegalArgumentException("password is required"));
                    }
                    return Mono.empty();
                }));
    }
}
