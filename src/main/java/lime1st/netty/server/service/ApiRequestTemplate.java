package lime1st.netty.server.service;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * ApiRequestTemplate 추상 클래스
 * ApiRequest 인터페이스 중 executeService, getApiResult 만 구현했다.
 * 아직 구현하지 않은 requestParamValidation, service 는 이 클래스를 상속받는 클래스에서 구현하고
 * ApiRequest 인터페이스의 executeService 메서드를 호출하면 서비스에 따라서 다른 로직을 수행할 수 있게 된다.
 */
public abstract class ApiRequestTemplate implements ApiRequest {

    private static Logger log = null;
    protected Map<String, String> reqData;

    protected JsonObject apiResult;

    // Http 요청에서 추출한 필드의 이름과 값을 API 서비스 클래스의 생성자로 전달한다.
    public ApiRequestTemplate(Map<String, String> reqData) {
        log = LoggerFactory.getLogger(this.getClass());
        this.apiResult = new JsonObject();
        this.reqData = reqData;

        log.info("request data: {}", this.reqData);
    }

    public void executeService() {
        try {
            // API 서비스 클래스의 인수로 입력된 Http 요청 맵의 정합성을 검증한다.
            // 이 메서드는 ApiRequestTemplate 추상 클래스를 상속받은 클래스에서 구현해야 한다.
            this.requestParamValidation();

            // service 메서드는 각 API 서비스 클래스가 제공할 기능을 구현한다.
            this.service();
        } catch (RequestParamException e) {
            log.error(e.getMessage(), e);
            this.apiResult.addProperty("resultCode", "405");
        } catch (ServiceException e) {
            log.error(e.getMessage(), e);
            this.apiResult.addProperty("resultCode", "501");
        }
    }

    public JsonObject getApiResult() {
        return this.apiResult;
    }
}
