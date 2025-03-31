package lime1st.netty.service.auth;

import com.google.gson.JsonObject;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.domain.User;
import lime1st.netty.domain.UserRepository;
import lime1st.netty.exception.RequestParamException;
import lime1st.netty.infra.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TokenIssue extends ApiRequestTemplate {

    private static final Logger log = LoggerFactory.getLogger(TokenIssue.class);
    private final UserRepository userRepository;
    private final RedisService redisService;

    public TokenIssue(Map<String, String> reqData, UserRepository userRepository, RedisService redisService) {
        super(reqData);
        this.userRepository = userRepository;
        this.redisService = redisService;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        log.info("data: {}", reqData);
        if (!reqData.containsKey("userId") || reqData.get("userId").isEmpty()) {
            throw new RequestParamException("userId is required");
        }
        if (!reqData.containsKey("password") || reqData.get("password").isEmpty()) {
            throw new RequestParamException("password is required");
        }
        log.info("userId: {}, password: {}", reqData.get("userId"), reqData.get("password"));
    }

    @Override
    public void service(JsonObject apiResult) {
        User user = userRepository.findByPassword(reqData.get("password"));
        log.info("user: {}", user);
        if (user != null) {
            final long threeHour = 60 * 60 * 3;
            long issueDate = System.currentTimeMillis() / 1000;
            String email = user.email();

            JsonObject token = new JsonObject();
            token.addProperty("issueDate", issueDate);
            token.addProperty("expireDate", issueDate + threeHour);
            token.addProperty("email", email);
            token.addProperty("userId", user.id());

            // token 저장
            String tokenKey = "token:" + user.email();
            redisService.save(tokenKey, token.toString());

            // helper.
            apiResult.addProperty("resultCode", "200");
            apiResult.addProperty("message", "Success");
            apiResult.addProperty("token", tokenKey);
        } else {
            apiResult.addProperty("resultCode", "404");
            apiResult.addProperty("message", "User not found");
        }
    }
}
