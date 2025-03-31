package lime1st.netty.api.model;

import com.google.gson.JsonObject;
import lime1st.netty.exception.RequestParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ApiRequestTemplate 추상 클래스
 * ApiRequest 인터페이스 중 executeService, getApiResult 만 구현했다.
 * 아직 구현하지 않은 requestParamValidation, service 는 이 클래스를 상속받는 클래스에서 구현하고
 * ApiRequest 인터페이스의 executeService 메서드를 호출하면 서비스에 따라서 다른 로직을 수행할 수 있게 된다.
 * -> 템플릿 메서드 패턴으로 요청 검증과 서비스 로직을 분리
 */
public abstract class ApiRequestTemplate implements ApiRequest {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestTemplate.class);
    protected Map<String, String> reqData;
    private JsonObject apiResult;

    // Http 요청에서 추출한 필드의 이름과 값을 API 서비스 클래스의 생성자로 전달한다.
    public ApiRequestTemplate(Map<String, String> reqData) {
        this.reqData = reqData;

        log.info("Request data: {}", reqData);
    }

    public void executeService() {
        apiResult = new JsonObject();
        try {
            // API 서비스 클래스의 인수로 입력된 Http 요청 맵의 정합성을 검증한다.
            // 이 메서드는 ApiRequestTemplate 추상 클래스를 상속받은 클래스에서 구현해야 한다.
            requestParamValidation();

            // service 메서드는 각 API 서비스 클래스가 제공할 기능을 구현한다.
            service(apiResult);
        } catch (RequestParamException e) {
            log.error(e.getMessage(), e);
            apiResult.addProperty("resultCode", "405");
        } catch (Exception e) {
            log.error(e.getMessage(), e) ;
            apiResult.addProperty("resultCode", "501");
        }
    }

    @Override
    public abstract void service(JsonObject apiResult);

    @Override
    public JsonObject getApiResult() {
        return apiResult;
    }
}
