package lime1st.netty.server.core;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import lime1st.netty.server.service.ApiRequest;
import lime1st.netty.server.service.ServiceDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;

public class ApiRequestParser extends SimpleChannelInboundHandler<FullHttpMessage> {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestParser.class);
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static final Set<String> usingHeader = new HashSet<>();
    static {
        usingHeader.add("token");
        usingHeader.add("email");
    }

    private HttpRequest request;
    private JsonObject apiResult;
    private HttpPostRequestDecoder decoder;
    private Map<String, String> reqData = new HashMap<>();

    // 클라이언트가 전송한 데이터가 채널 파이프라인의 모든 디코더를 거치고 난 뒤에 channelRead0 메서드가 호출된다.
    // 메서드 호출에 입력되는 객체는 클래스의 확장에 선언한 제네릭 타입의 구현체가 된다.
    // FullHttpMessage 에는 HTTP 프로토콜의 모든 데이터가 포함되어 있다.
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpMessage msg) throws Exception {
        // Request header 처리
        // HttpRequestDecoder 는 HTTP 프로토콜의 데이터를 HttpRequest, HttpContent, LastHttpContent 의 순서로
        // 디코딩하여 FullHttpMessage 객체로 만들고 인바운드 이벤트를 발생시킨다.
        if (msg instanceof HttpRequest) {
            this.request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(this.request)) send100Continue(ctx);

            // 헤더 정보 추출
            HttpHeaders headers = request.headers();
            if (!headers.isEmpty()) {
                for (Map.Entry<String, String> header : headers) {
                    String key = header.getKey();
                    // 추출한 헤더 정보 중에서 usingHeader 에 지정된 값만 추출하여 저장한다.
                    if (usingHeader.contains(key)) {
                        reqData.put(key, header.getValue());
                    }
                }
            }

            // 클라이언트의 요청 URI, METHOD 를 추출하여 저장한다.
            reqData.put("REQUEST_URI", request.uri());
            reqData.put("REQUEST_METHOD", request.method().name());
        }

        if (msg != null) {
            // HttpContent 인터페이스로부터 HTTP 본문 데이터를 추출한다.
            ByteBuf content = ((HttpContent) msg).content();

            log.debug("LastHttpContent message received!!{}", request.uri());

            // Http 본문에서 Post 데이터를 추출한다.
            readPostData();

            // Http 프로토콜에서 필요한 데이터의 추출이 완료되면 reqData 맵을 ServiceDispatcher 클래스의
            // dispatch 메서드를 호출하여 Http 요청에 맞는 api 서비스 클래스를 생성한다.
            ApiRequest service = ServiceDispatcher.dispatch(reqData);

            try {
                service.executeService();
                apiResult = service.getApiResult();
            } finally {
                reqData.clear();
            }

            // apiResult 멤버 변수에 저장된 api 처리 결과를 클라이언트 채널의 송신 버퍼에 기록한다.
            if (!writeResponse((LastHttpContent) msg, ctx)) {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                        .addListener(ChannelFutureListener.CLOSE);
            }
            reset();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("요청 처리 완료");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace(System.err );
        ctx.close();
    }

    private void readPostData() {
        try {
            decoder = new HttpPostRequestDecoder(factory, request);
            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (HttpDataType.Attribute == data.getHttpDataType()) {
                    try {
                        Attribute attribute = (Attribute) data;
                        // Attribute 의 이름과 값을 reqData 맵에 저장한다. 즉 클라이언트가 HTML 의 FORM 엘리먼트를 사용하여
                        // 전송한 데이터를 추출한다. JSP 의 request.queryString 메서드와 동일한 동작을 수행한다.
                        reqData.put(attribute.getName(), attribute.getValue());
                    } catch (IOException e) {
                        log.error("BODY Attribute: {}", data.getHttpDataType().name(), e);
                        return;
                    }
                } else {
                    log.info("BODY data: {} : {}", data.getHttpDataType().name(), data);
                }
            }
        } catch (ErrorDataDecoderException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (decoder != null) {
                decoder.destroy();
            }
        }
    }

    private void reset() {
        request = null;
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                currentObj.decoderResult().isSuccess() ? OK : BAD_REQUEST, Unpooled.copiedBuffer(
                apiResult.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // -
            // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }
}
