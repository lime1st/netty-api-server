package lime1st.netty.service.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.exception.RequestParamException;
import lime1st.netty.infra.redis.RedisService;

import java.util.Map;

public class TokenExpire extends ApiRequestTemplate {

    private final RedisService redisService;

    public TokenExpire(Map<String, String> reqData, RedisService redisService) {
        super(reqData);
        this.redisService = redisService;
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!reqData.containsKey("token") || reqData.get("token").isEmpty()) {
            throw new RequestParamException("token is required");
        }
    }

    @Override
    public void service(ObjectNode apiResult) {
        if (redisService.del(reqData.get("token"))) {
            // helper.
            apiResult.put("resultCode", "200");
            apiResult.put("message", "Success");
            apiResult.put("token", reqData.get("token"));
        } else {
            apiResult.put("resultCode", "404");
            apiResult.put("message", "token not exist or expired!");
        }
    }
}
