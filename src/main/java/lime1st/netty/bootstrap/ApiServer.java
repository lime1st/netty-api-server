package lime1st.netty.bootstrap;

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
import lime1st.netty.infra.redis.RedisService;
import lime1st.netty.service.common.Router;
import lime1st.netty.user.adapter.out.persistence.UserPersistenceAdapter;
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
    private final UserPersistenceAdapter userPersistenceAdapter;

    public ApiServer(int port, int boosThreadCount, int workerThreadCount) {
        this.port = port;
        this.boosThreadCount = boosThreadCount;
        this.workerThreadCount = workerThreadCount;
        this.redisService = new RedisService();
        this.userPersistenceAdapter = new UserPersistenceAdapter();
        UserService userService = new UserService(userPersistenceAdapter, userPersistenceAdapter);
        this.router = new Router(redisService, userService);
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
        userPersistenceAdapter.close();
        redisService.close();
        log.info("Shutting down...");
    }

    private void initializeData() {
        CreateUserCommand createUserCommand = new CreateUserCommand("alex", "alex@mail.com", "password");
        userPersistenceAdapter.saveUser(createUserCommand);
    }
}
