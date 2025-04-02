package lime1st.netty.user.adapter.in.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.exception.RequestParamException;
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
    public void requestParamValidation() throws RequestParamException {
        if (!reqData.containsKey("email") || reqData.get("email").isEmpty()) {
            throw new RequestParamException("email is required");
        }
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
}
