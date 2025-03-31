package lime1st.netty.api.model;

import com.google.gson.JsonObject;
import lime1st.netty.exception.RequestParamException;

public interface ApiRequest {

    /**
     * API 를 호출하는 HTTP 요청의 파라미터 값이 입력되었는지 검증하는 메서드
     */
    void requestParamValidation() throws RequestParamException;

    /**
     * 각 API 서비스에 따른 개별 구현 메서드
     */
    void service(JsonObject apiResult);

    /**
     * 서비스의 API 호출 시작 메서드
     */
    void executeService();

    /**
     * API 서비스의 처리 결과를 조회하는 메서드
     */
    JsonObject getApiResult();
}
