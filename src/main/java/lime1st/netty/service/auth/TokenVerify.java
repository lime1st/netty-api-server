package lime1st.netty.service.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lime1st.netty.api.model.ApiRequestTemplate;
import lime1st.netty.exception.RequestParamException;
import lime1st.netty.infra.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TokenVerify extends ApiRequestTemplate {

    private static final Logger log = LoggerFactory.getLogger(TokenVerify.class);
    private final RedisService redisService;

    public TokenVerify(Map<String, String> reqData, RedisService redisService) {
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
    public void service(JsonObject apiResult){
        String tokenString = redisService.get(this.reqData.get("token"));

        if (tokenString == null) {
            apiResult.addProperty("resultCode", "404");
            apiResult.addProperty("message", "Token not found");
        } else {
            Gson gson = new Gson();
            JsonObject token = gson.fromJson(tokenString, JsonObject.class);

            // helper.
            apiResult.addProperty("resultCode", "200");
            apiResult.addProperty("message", "Success");
            apiResult.addProperty("issueDate", token.get("issueDate").getAsString());
            apiResult.addProperty("email", token.get("email").getAsString());
            apiResult.addProperty("userId", token.get("userId").getAsString());
        }
    }
}
