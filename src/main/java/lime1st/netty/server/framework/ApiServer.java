package lime1st.netty.server.framework;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import lime1st.netty.auth.adapter.out.persistence.TokenRedisAdapter;
import lime1st.netty.auth.application.service.TokenService;
import lime1st.netty.server.adapter.in.web.Router;
import lime1st.netty.user.adapter.out.persistence.UserR2dbcAdapter;
import lime1st.netty.user.application.dto.in.CreateUserCommand;
import lime1st.netty.user.application.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiServer {

    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);
    private final int port;
    private final int boosThreadCount;
    private final int workerThreadCount;
    private final Router router;
    private final RedisService redisService;
    private final UserService userService;

    public ApiServer(int port, int boosThreadCount, int workerThreadCount) {
        this.port = port;
        this.boosThreadCount = boosThreadCount;
        this.workerThreadCount = workerThreadCount;
        this.redisService = new RedisService();
        ConnectionFactory connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///testdb");
        UserR2dbcAdapter userR2dbcAdapter = new UserR2dbcAdapter(connectionFactory);
        TokenRedisAdapter tokenRedisAdapter = new TokenRedisAdapter(redisService);
        TokenService tokenService = new TokenService(tokenRedisAdapter);
        this.userService = new UserService(userR2dbcAdapter, userR2dbcAdapter);
        this.router = new Router(tokenService, userService);
    }

    public void start() throws Exception {
         initializeData();

        final EventLoopGroup bossGroup;
        final EventLoopGroup workerGroup;
        final ServerChannel serverChannel;

        // mac 에서 epoll 지원은 안 되지만 KQueue 를 사용할 수 있다.
        if (KQueue.isAvailable()) {
            bossGroup = new KQueueEventLoopGroup(boosThreadCount);
            workerGroup = new KQueueEventLoopGroup(workerThreadCount);
            serverChannel = new KQueueServerSocketChannel();
        } else {
            bossGroup = new NioEventLoopGroup(boosThreadCount);
            workerGroup = new NioEventLoopGroup(workerThreadCount);
            serverChannel = new NioServerSocketChannel();
        }

        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(serverChannel.getClass());
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new ApiServerInitializer(router));
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            log.info("Server started on port {}", port);

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            shutdown();
        }
    }

    private void shutdown() {
        redisService.close();
        log.info("Shutting down...");
    }

    private void initializeData() {
        CreateUserCommand createUserCommand = new CreateUserCommand("alex", "alex@mail.com", "password");
        userService.createUser(createUserCommand)
                .subscribe(
                        id -> log.info("Initialized user with id: {}", id),
                        err -> log.error("Failed to create user", err)
                );
    }
}
