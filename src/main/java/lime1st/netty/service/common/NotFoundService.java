package lime1st.netty.service.common;

import com.google.gson.JsonObject;
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
    public void service(JsonObject apiResult) {
        apiResult.addProperty("resultCode", "404");
        apiResult.addProperty("message", "Service not found");
    }
}