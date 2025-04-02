package lime1st.netty.server.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.server.application.port.in.ApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

public abstract class ApiRequestTemplate implements ApiRequest {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestTemplate.class);
    protected Map<String, String> reqData;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiRequestTemplate(Map<String, String> reqData) {
        this.reqData = reqData;
    }

    public Mono<ObjectNode> executeService() {
        ObjectNode apiResult = objectMapper.createObjectNode();

        return validateRequest()
                .then(service(apiResult))
                .then(Mono.just(apiResult))
                .onErrorResume(error -> {
                    log.warn("Request validation failed: {}", error.getMessage());
                    apiResult.put("resultCode", "400");
                    apiResult.put("message", error.getMessage());
                    return Mono.just(apiResult);
                });
    }

    protected Mono<Void> validateRequest() {
        if (reqData == null || reqData.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Request data is required"));
        }
        return Mono.empty();
    }
    public abstract Mono<Void> service(ObjectNode apiResult);
}
