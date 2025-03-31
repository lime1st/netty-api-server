package lime1st.netty.service.common;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lime1st.netty.api.model.ApiRequestTemplate;

import java.util.Map;

public class NotFoundService extends ApiRequestTemplate {
    public NotFoundService(Map<String, String> reqData) {
        super(reqData);
    }

    @Override
    public void requestParamValidation() {
        // No validation needed
    }

    @Override
    public void service(ObjectNode apiResult) {
        apiResult.put("resultCode", "404");
        apiResult.put("message", "Service not found");
    }
}