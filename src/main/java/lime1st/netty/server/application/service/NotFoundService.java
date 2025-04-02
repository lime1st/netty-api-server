package lime1st.netty.server.application.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.server.domain.ApiRequestTemplate;
import reactor.core.publisher.Mono;

import java.util.Map;

public class NotFoundService extends ApiRequestTemplate {
    public NotFoundService(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public Mono<Void> service(ObjectNode apiResult) {
        apiResult.put("resultCode", "404");
        apiResult.put("message", "Service not found");
        return Mono.empty();
    }
}