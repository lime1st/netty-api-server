package lime1st.netty.service.common;

import lime1st.netty.api.model.ApiRequest;
import lime1st.netty.infra.redis.RedisService;
import lime1st.netty.service.auth.TokenExpire;
import lime1st.netty.service.auth.TokenIssue;
import lime1st.netty.service.auth.TokenVerify;
import lime1st.netty.user.adapter.in.web.UserEndpoint;
import lime1st.netty.user.application.port.in.ReadUserUseCase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Router {

    private final Map<String, Function<Map<String, String>, ApiRequest>> routes;
    private final RedisService redisService;
    private final ReadUserUseCase readUserUseCase;

    public Router(RedisService redisService, ReadUserUseCase readUserUseCase) {
        this.redisService = redisService;
        this.readUserUseCase = readUserUseCase;
        this.routes = new HashMap<>();
        initializeRoutes();
    }

    private void initializeRoutes() {
        routes.put("/tokens_GET", reqData -> new TokenVerify(reqData, redisService));
        routes.put("/tokens_POST", reqData -> new TokenIssue(reqData, redisService, readUserUseCase));
        routes.put("/tokens_DELETE", reqData -> new TokenExpire(reqData, redisService));
        routes.put("/users_GET", reqData -> new UserEndpoint(reqData, readUserUseCase));
    }

    public ApiRequest route(String uri, String method, Map<String, String> reqData) {
        String key = uri + "_" + method;
        return routes.getOrDefault(key, NotFoundService::new).apply(reqData);
    }
}
