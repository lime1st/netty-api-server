package lime1st.netty.auth.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lime1st.netty.auth.application.port.out.DelTokenPort;
import lime1st.netty.auth.application.port.out.GetTokenPort;
import lime1st.netty.auth.application.port.out.SetTokenPort;
import lime1st.netty.auth.domain.Token;
import lime1st.netty.server.framework.RedisService;
import reactor.core.publisher.Mono;

public class TokenRedisAdapter implements SetTokenPort, GetTokenPort, DelTokenPort {

    private final RedisService redisService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TokenRedisAdapter(RedisService redisService) {
        this.redisService = redisService;
    }

    @Override
    public Mono<String> setToken(Token token) {
        String tokenValue = "";
        try {
            tokenValue = objectMapper.writeValueAsString(token);
        } catch (JsonProcessingException e) {
            e.printStackTrace(System.err);
        }
        String finalTokenValue = tokenValue;
        return Mono.fromCallable(() -> redisService.set("token:" + token.email(), finalTokenValue));
    }

    @Override
    public Mono<String> getToken(String token) {
        return Mono.fromCallable(() -> redisService.get(token));
    }

    @Override
    public Mono<Boolean> delToken(String token) {
        return Mono.fromCallable(() ->
                redisService.del(token));
    }
}
