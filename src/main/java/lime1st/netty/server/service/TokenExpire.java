package lime1st.netty.server.service;

import lime1st.netty.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service("tokenExpire")
@Scope("prototype")
public class TokenExpire extends ApiRequestTemplate {

    @Autowired
    private  RedisService redisService;

    public TokenExpire(Map<String, String> reqData) {
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
        if (redisService.del(this.reqData.get("token"))) {
            // helper.
            this.apiResult.addProperty("resultCode", "200");
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("token", this.reqData.get("token"));
        } else {
            this.apiResult.addProperty("resultCode", "404");
            this.apiResult.addProperty("message", "token not exist or expired!");
        }
    }
}
