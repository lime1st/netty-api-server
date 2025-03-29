package lime1st.netty.server.service;

import lime1st.netty.user.UserService;
import lime1st.netty.user.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("users")
@Scope("prototype")
public class UserInfo extends ApiRequestTemplate {

    @Autowired
    private UserService userService;

    public UserInfo(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!StringUtils.hasText(reqData.get("email"))) {
            throw new RequestParamException("email is required");
        }
    }

    @Override
    public void service() throws ServiceException {
        Users user = userService.findByEmail(reqData.get("email"));

        if (user != null) {
            this.apiResult.addProperty("resultCode", "200");
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("userId", user.getId());
        } else {
            this.apiResult.addProperty("resultCode", "404");
            this.apiResult.addProperty("message", "User not found");
        }
    }
}
