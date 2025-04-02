package lime1st.netty.user.adapter.in.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.server.domain.ApiRequestTemplate;
import lime1st.netty.user.adapter.in.web.dto.response.FindUserResponse;
import lime1st.netty.user.application.port.in.ReadUserUseCase;
import reactor.core.publisher.Mono;

import java.util.Map;

public class UserEndpoint extends ApiRequestTemplate {

    private final ReadUserUseCase readUserUseCase;

    public UserEndpoint(Map<String, String> reqData, ReadUserUseCase readUserUseCase) {
        super(reqData);
        this.readUserUseCase = readUserUseCase;
    }

    @Override
    public Mono<Void> service(ObjectNode apiResult) {
        return readUserUseCase.readUserByEmail(reqData.get("email"))
                .map(FindUserResponse::fromQuery)
                .doOnNext(findUser -> {
                    apiResult.put("resultCode", "200");
                    apiResult.put("message", "Success");
                    apiResult.put("userId", findUser.id());
                })
                .switchIfEmpty(Mono.fromRunnable(() -> {
                    apiResult.put("resultCode", "404");
                    apiResult.put("message", "User Not Found");
                }))
                .then();
    }

    @Override
    protected Mono<Void> validateRequest() {
        return super.validateRequest()
                .then(Mono.defer(() -> {
                    if (!reqData.containsKey("email") || reqData.get("email").isEmpty()) {
                        return Mono.error(new IllegalArgumentException("email is required"));
                    }
                    return Mono.empty();
                }));
    }
}
