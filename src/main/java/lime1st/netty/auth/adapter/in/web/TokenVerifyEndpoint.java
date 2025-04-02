package lime1st.netty.auth.adapter.in.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.auth.application.port.in.TokenVerifyUseCase;
import lime1st.netty.server.domain.ApiRequestTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TokenVerifyEndpoint extends ApiRequestTemplate {

    private final TokenVerifyUseCase tokenVerifyUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenVerifyEndpoint(Map<String, String> reqData, TokenVerifyUseCase tokenVerifyUseCase) {
        super(reqData);
        this.tokenVerifyUseCase = tokenVerifyUseCase;
    }

    @Override
    public Mono<Void> service(ObjectNode apiResult) {
        return tokenVerifyUseCase.verifyToken(reqData.get("token"))
//                .switchIfEmpty(Mono.error(new IllegalArgumentException("token is expired or ...")))
                .flatMap(tokenValue -> {
                    try {
                        JsonNode token = objectMapper.readTree(tokenValue);
                        apiResult.put("resultCode", "200");
                        apiResult.put("message", "Success");
                        apiResult.put("issuedDate", token.get("issuedDate").asText());
                        apiResult.put("email", token.get("email").asText());
                        apiResult.put("userId", token.get("userId").asText());
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                    return Mono.empty();
                }).then();
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
