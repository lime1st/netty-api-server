package lime1st.netty.server.adapter.in.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lime1st.netty.server.application.port.in.ApiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

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

        if (service == null) {
            sendErrorResponse(ctx, HttpResponseStatus.NOT_FOUND);
            return;
        }

        Mono.from(service.executeService())
                .map(result -> {
                    FullHttpResponse response = new DefaultFullHttpResponse(
                            HttpVersion.HTTP_1_1,
                            HttpResponseStatus.OK,
                            Unpooled.copiedBuffer(result.toString(), CharsetUtil.UTF_8)
                    );
                    response.headers().set(CONTENT_TYPE, "application/json");
                    response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
                    if (HttpUtil.isKeepAlive(req)) {
                        response.headers().set(CONNECTION, KEEP_ALIVE);
                    }
                    return response;
                })
                .defaultIfEmpty(createEmptyResponse())
                .subscribe(
                        response -> {
                            ChannelFuture future = ctx.writeAndFlush(response);
                            future.addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
                                    .addListener(f -> {
                                        if (f.isSuccess()) {
                                            log.info("Response sent");
                                        } else {
                                            log.error("Failed to send response: ", f.cause());
                                        }
                                        if (!HttpUtil.isKeepAlive(req)) {
                                            ctx.close();
                                        }
                                    });
                        },
                        error -> {
                            log.error("Failed to process request", error);
                            sendErrorResponse(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                        },
                        () -> log.info("Request processing complete")
                );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Error processing request", cause);
        ctx.close();
    }

    private FullHttpResponse createEmptyResponse() {
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.copiedBuffer("{}", CharsetUtil.UTF_8)
        );
    }

    private void sendErrorResponse(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, status,
                Unpooled.copiedBuffer("{\"error\": \"" + status.reasonPhrase() + "\"}", CharsetUtil.UTF_8)
        );
        response.headers().set(CONTENT_TYPE, "application/json");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
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
