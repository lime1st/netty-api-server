package lime1st.netty.service.user;

import com.google.gson.JsonObject;
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
    public void service(JsonObject apiResult) {
        User user = userRepository.findByEmail(reqData.get("email"));

        if (user != null) {
            apiResult.addProperty("resultCode", "200");
            apiResult.addProperty("message", "Success");
            apiResult.addProperty("userId", user.id());
        } else {
            apiResult.addProperty("resultCode", "404");
            apiResult.addProperty("message", "User not found");
        }
    }
}
