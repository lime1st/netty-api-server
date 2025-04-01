package lime1st.netty.api.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lime1st.netty.api.model.ApiRequest;
import lime1st.netty.service.common.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class ApiRequestParser extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Router router;

    public ApiRequestParser(Router router) {
        this.router = router;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) {
        // FullHttpRequest 에서 uri, method(GET/POST...), body 분리
        Map<String, String> reqData = parseRequest(req);
        log.info("Received request: {}", reqData);
        ApiRequest service = router.route(reqData.get("uri"), reqData.get("method"), reqData);

        service.executeService();
        ObjectNode result = service.getApiResult();

        FullHttpResponse response = null;
        try {
             response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    Unpooled.copiedBuffer(OBJECT_MAPPER.writeValueAsString(result), CharsetUtil.UTF_8)
            );
            response.headers().set(CONTENT_TYPE, "application/json");
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.err);
        }

        if (HttpUtil.isKeepAlive(req)) {
            assert response != null;
            response.headers().set(CONNECTION, KEEP_ALIVE);
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error processing request", cause);
        ctx.close();
    }

    private Map<String, String> parseRequest(FullHttpRequest req) {
        Map<String, String> reqData = new HashMap<>();
        reqData.put("uri", req.uri());
        reqData.put("method", req.method().name());

        // 쿼리 파라미터 파싱
        QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
        queryDecoder.parameters().forEach((key, values) -> reqData.put(key, values.get(0)));

        // 헤더 추가
        HttpHeaders headers = req.headers();
        headers.forEach(entry -> reqData.put(entry.getKey(), entry.getValue()));

        // 본문 파싱
        String contentType = headers.get(CONTENT_TYPE);
        String body = req.content().toString(CharsetUtil.UTF_8);
        if (!body.isEmpty() && contentType != null) {
            if (contentType.contains("application/json")) {
                try {
                    Map<String, String> jsonData = OBJECT_MAPPER.readValue(body, new TypeReference<Map<String, String>>() {});
                    reqData.putAll(jsonData);
                } catch (Exception e) {
                    log.error("Failed to parse JSON body: {}", body, e);
                }
            } else if (contentType.contains("application/x-www-form-urlencoded")) {
                QueryStringDecoder bodyDecoder = new QueryStringDecoder(body, false);
                bodyDecoder.parameters().forEach((key, values) ->
                        reqData.put(key, values.get(0)));
            } else {
                reqData.put("body", body);  // 기타 형식은 그대로 저장
            }
        }

        return reqData;
    }
}
