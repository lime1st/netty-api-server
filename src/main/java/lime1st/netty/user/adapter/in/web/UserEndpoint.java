package lime1st.netty.user.adapter.in.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.exception.RequestParamException;
import lime1st.netty.user.adapter.in.web.dto.response.FindUserResponse;
import lime1st.netty.user.application.port.in.ReadUserUseCase;

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
    public void service(ObjectNode apiResult) {
        FindUserResponse findUser = FindUserResponse.fromQuery(readUserUseCase
                .readUserByEmail(reqData.get("email")));

        apiResult.put("resultCode", "200");
        apiResult.put("message", "Success");
        apiResult.put("userId", findUser.id());
    }
}
