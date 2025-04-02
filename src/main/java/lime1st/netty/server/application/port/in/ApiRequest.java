package lime1st.netty.server.application.port.in;

import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

public interface ApiRequest {

    Mono<ObjectNode> executeService();
}
