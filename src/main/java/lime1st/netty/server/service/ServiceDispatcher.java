package lime1st.netty.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ServiceDispatcher {

    private static final Logger log = LoggerFactory.getLogger(ServiceDispatcher.class);
    private static ApplicationContext springContext;

    // beanName 을 결정하는 로직(if-else 와 switch)을 Map 기반 매핑으로 교체
    private static final Map<String, Map<String, String>> URI_METHOD_TO_BEAN;

    static {
        URI_METHOD_TO_BEAN = Map.of("/tokens", new HashMap<>(Map.of(
                        "GET", "tokenVerify",
                        "POST", "tokenIssue",
                        "DELETE", "tokenExpire"
                )),
                "/users", Map.of("", "users"));
    }

    @Autowired
    public void init(ApplicationContext springContext) {
        ServiceDispatcher.springContext = springContext;
    }

    public static ApiRequest dispatch(Map<String, String> requestMap) {
        if (requestMap == null || requestMap.isEmpty()) {
            return (ApiRequest) springContext.getBean("notFound", Map.of());
        }
        String serviceName = resolveUriAndHttpMethod(requestMap);
//        log.info("Service name: {}", serviceName);
        ApiRequest service;

        try {
            service = (ApiRequest) springContext.getBean(serviceName, requestMap);
        } catch (Exception e) {
            log.error("Failed to get bean: {}, {}", serviceName, e.getMessage(), e);
            service = (ApiRequest) springContext.getBean("notFound", requestMap);
        }
        return service;
    }

    private static String resolveUriAndHttpMethod(Map<String, String> requestMap) {
        String serviceUri = requestMap.get("REQUEST_URI");
        if (serviceUri == null) {
            return "notFound";
        }

        String httpMethod = getOrDefault(requestMap.get("REQUEST_METHOD"));
        return URI_METHOD_TO_BEAN.entrySet().stream()
                .filter(entry -> serviceUri.startsWith(entry.getKey()))
                .findFirst()
                .map(entry -> {
                        Map<String, String> methodMap = entry.getValue();
                        return methodMap.containsKey("") ?
                                methodMap.get("") :
                                methodMap.getOrDefault(httpMethod, "notFound");
                })
                .orElse("notFound");
    }

    private static String getOrDefault(String value) {
        return value != null ? value : "";
    }
}
