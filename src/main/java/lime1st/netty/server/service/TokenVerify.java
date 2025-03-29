package lime1st.netty.server.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lime1st.netty.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("tokenVerify")
@Scope("prototype")
public class TokenVerify extends ApiRequestTemplate {

    @Autowired
    private RedisService redisService;

    public TokenVerify(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public void requestParamValidation() throws RequestParamException {
        if (!StringUtils.hasText(reqData.get("token"))) {
            throw new RequestParamException("token is required");
        }
    }

    @Override
    public void service() throws ServiceException {
        String tokenString = redisService.get(this.reqData.get("token"));

        if (tokenString == null) {
            this.apiResult.addProperty("resultCode", "404");
            this.apiResult.addProperty("message", "Token not found");
        } else {
            Gson gson = new Gson();
            JsonObject token = gson.fromJson(tokenString, JsonObject.class);

            // helper.
            this.apiResult.addProperty("resultCode", "200");
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("issueDate", token.get("issueDate").getAsString());
            this.apiResult.addProperty("email", token.get("email").getAsString());
            this.apiResult.addProperty("userId", token.get("userId").getAsString());
        }
    }
}
