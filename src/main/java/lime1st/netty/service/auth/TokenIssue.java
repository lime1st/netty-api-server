package lime1st.netty.service.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.exception.RequestParamException;
import lime1st.netty.infra.redis.RedisService;
import lime1st.netty.user.application.dto.out.FindUserQuery;
import lime1st.netty.user.application.port.in.ReadUserUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TokenIssue extends ApiRequestTemplate {

    private static final Logger log = LoggerFactory.getLogger(TokenIssue.class);
    private final RedisService redisService;
    private final ReadUserUseCase readUserUseCase;

    public TokenIssue(Map<String, String> reqData,RedisService redisService, ReadUserUseCase readUserUseCase) {
        super(reqData);
        this.redisService = redisService;
        this.readUserUseCase = readUserUseCase;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!reqData.containsKey("userId") || reqData.get("userId").isEmpty()) {
            throw new RequestParamException("userId is required");
        }
        if (!reqData.containsKey("password") || reqData.get("password").isEmpty()) {
            throw new RequestParamException("password is required");
        }
    }

    @Override
    public void service(ObjectNode apiResult) {
        FindUserQuery findUser = readUserUseCase.readUserByPassword(reqData.get("password"));

        if (findUser != null) {
            final long threeHour = 60 * 60 * 3;
            long issueDate = System.currentTimeMillis() / 1000;
            String email = findUser.email();

            ObjectNode token = OBJECT_MAPPER.createObjectNode();
            token.put("issueDate", issueDate);
            token.put("expireDate", issueDate + threeHour);
            token.put("email", email);
            token.put("userId", findUser.id());

            // token 저장
            String tokenKey = "token:" + findUser.email();
            redisService.save(tokenKey, token.toString());

            // helper.
            apiResult.put("resultCode", "200");
            apiResult.put("message", "Success");
            apiResult.put("token", tokenKey);
        } else {
            apiResult.put("resultCode", "404");
            apiResult.put("message", "User not found");
        }
    }
}
