package lime1st.netty.server.service;

import com.google.gson.JsonObject;
import lime1st.netty.redis.RedisService;
import lime1st.netty.user.UserService;
import lime1st.netty.user.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("tokenIssue")
@Scope("prototype")
public class TokenIssue extends ApiRequestTemplate {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisService redisService;

    public TokenIssue(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!StringUtils.hasText(reqData.get("userId"))) {
            throw new RequestParamException("userId is required");
        }

        if (!StringUtils.hasText(reqData.get("password"))) {
            throw new RequestParamException("password is required");
        }
    }

    @Override
    public void service() throws ServiceException {
        Users user = userService.findByPassword(reqData.get("password"));

        if (user != null) {
            final long threeHour = 60 * 60 * 3;
            long issueDate = System.currentTimeMillis() / 1000;
            String email = user.getEmail();

            JsonObject token = new JsonObject();
            token.addProperty("issueDate", issueDate);
            token.addProperty("expireDate", issueDate + threeHour);
            token.addProperty("email", email);
            token.addProperty("userId", user.getId());

            // token 저장
            String tokenKey = "token:" + user.getEmail();
            redisService.save(tokenKey, token.toString());

            // helper.
            this.apiResult.addProperty("resultCode", "200");
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("token", tokenKey);
        } else {
            this.apiResult.addProperty("resultCode", "404");
            this.apiResult.addProperty("message", "User not found");
        }
    }
}
