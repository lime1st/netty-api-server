package lime1st.netty.service.user;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.domain.User;
import lime1st.netty.domain.UserRepository;
import lime1st.netty.exception.RequestParamException;

import java.util.Map;

public class UserInfo extends ApiRequestTemplate {

    private final UserRepository userRepository;

    public UserInfo(Map<String, String> reqData, UserRepository userRepository) {
        super(reqData);
        this.userRepository = userRepository;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!reqData.containsKey("email") || reqData.get("email").isEmpty()) {
            throw new RequestParamException("email is required");
        }
    }

    @Override
    public void service(ObjectNode apiResult) {
        User user = userRepository.findByEmail(reqData.get("email"));

        if (user != null) {
            apiResult.put("resultCode", "200");
            apiResult.put("message", "Success");
            apiResult.put("userId", user.id());
        } else {
            apiResult.put("resultCode", "404");
            apiResult.put("message", "User not found");
        }
    }
}
