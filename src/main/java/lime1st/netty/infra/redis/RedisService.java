package lime1st.netty.infra.redis;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisService {

    private final RedisCommands<String, String> commands;

    public RedisService() {
        RedisClient client = RedisClient.create("redis://localhost:6379");
        this.commands = client.connect().sync();
    }

    public boolean del(String key) {
        return commands.del(key) > 0;
    }

    public String get(String key) {
        return commands.get(key);
    }

    public void save(String key, String value) {
        commands.set(key, value);
    }
}
