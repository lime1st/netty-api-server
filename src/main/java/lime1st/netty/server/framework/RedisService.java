package lime1st.netty.server.framework;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisService.class);
    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;
    private final RedisCommands<String, String> commands;

    public RedisService() {
        this.client = RedisClient.create("redis://localhost:6379");
        this.connection = client.connect();
        this.commands = client.connect().sync();
        log.info("Connected to redis");
    }

    public boolean del(String key) {
        return commands.del(key) > 0;
    }

    public String get(String key) {
        return commands.get(key);
    }

    public String set(String key, String value) {
        commands.set(key, value);
        return key;
    }

    public void close() {
        if (connection != null && connection.isOpen()) {
            connection.close();
            log.info("Close connection redis");
        }
        if (client != null) {
            client.shutdown();
            log.info("Close redis client");
        }
    }
}
