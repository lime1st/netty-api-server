package lime1st.netty.server.adapter.in.web;

import lime1st.netty.auth.adapter.in.web.TokenExpireEndpoint;
import lime1st.netty.auth.adapter.in.web.TokenIssueEndpoint;
import lime1st.netty.auth.adapter.in.web.TokenVerifyEndpoint;
import lime1st.netty.auth.application.service.TokenService;
import lime1st.netty.server.application.port.in.ApiRequest;
import lime1st.netty.server.application.service.NotFoundService;
import lime1st.netty.user.adapter.in.web.UserEndpoint;
import lime1st.netty.user.application.port.in.ReadUserUseCase;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Router {

    private final Map<String, Function<Map<String, String>, ApiRequest>> routes;

    public Router(TokenService tokenService, ReadUserUseCase readUserUseCase) {
        this.routes = new HashMap<>();
        routes.put("/tokens_POST", reqData -> new TokenIssueEndpoint(reqData, tokenService, readUserUseCase));
        routes.put("/tokens_GET", reqData -> new TokenVerifyEndpoint(reqData, tokenService));
        routes.put("/tokens_DELETE", reqData -> new TokenExpireEndpoint(reqData, tokenService));
        routes.put("/users_GET", reqData -> new UserEndpoint(reqData, readUserUseCase));
    }

    public ApiRequest route(String uri, String method, Map<String, String> reqData) {
        String key = uri + "_" + method;
        return routes.getOrDefault(key, NotFoundService::new).apply(reqData);
    }
}
