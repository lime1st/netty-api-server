package lime1st.netty.service.common;

import lime1st.netty.api.model.ApiRequest;
import lime1st.netty.infra.redis.RedisService;
import lime1st.netty.service.auth.TokenExpire;
import lime1st.netty.service.auth.TokenIssue;
import lime1st.netty.service.auth.TokenVerify;
import lime1st.netty.service.user.UserInfo;
import lime1st.netty.domain.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Router {

    private final Map<String, Function<Map<String, String>, ApiRequest>> routes;
    private final RedisService redisService;
    private final UserRepository userRepository;

    public Router() {
        this.redisService = new RedisService();
        this.userRepository = new UserRepository();
        this.routes = new HashMap<>();
        routes.put("/tokens_GET", reqData -> new TokenVerify(reqData, redisService));
        routes.put("/tokens_POST", reqData -> new TokenIssue(reqData, userRepository, redisService));
        routes.put("/tokens_DELETE", reqData -> new TokenExpire(reqData, redisService));
        routes.put("/users_GET", reqData -> new UserInfo(reqData, userRepository));
    }

    public ApiRequest route(String uri, String method, Map<String, String> reqData) {
        String key = uri + "_" + method;
        return routes.getOrDefault(key, NotFoundService::new).apply(reqData);
    }
}
