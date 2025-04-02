package lime1st.netty.service.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.exception.RequestParamException;
import lime1st.netty.infra.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

public class TokenVerify extends ApiRequestTemplate {

    private static final Logger log = LoggerFactory.getLogger(TokenVerify.class);
    private final RedisService redisService;

    public TokenVerify(Map<String, String> reqData, RedisService redisService) {
        super(reqData);
        this.redisService = redisService;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!reqData.containsKey("token") || reqData.get("token").isEmpty()) {
            throw new RequestParamException("token is required");
        }
    }

    @Override
    public Mono<Void> service(ObjectNode apiResult) {
        String tokenString = redisService.get(reqData.get("token"));

        if (tokenString == null) {
            apiResult.put("resultCode", "404");
            apiResult.put("message", "Token not found");
        } else {
            try {
                JsonNode token = OBJECT_MAPPER.readTree(tokenString);

                // helper.
                apiResult.put("resultCode", "200");
                apiResult.put("message", "Success");
                apiResult.put("issueDate", token.get("issueDate").asText());
                apiResult.put("email", token.get("email").asText());
                apiResult.put("userId", token.get("userId").asText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return Mono.empty();
    }
}
