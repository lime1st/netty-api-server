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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApiServer {

    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);
    private final int port;
    private final int boosThreadCount;
    private final int workerThreadCount;

    public ApiServer(int port, int boosThreadCount, int workerThreadCount) {
        this.port = port;
        this.boosThreadCount = boosThreadCount;
        this.workerThreadCount = workerThreadCount;
    }

    public void start() throws Exception {

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
            b.childHandler(new ApiServerInitializer());
            b.option(ChannelOption.SO_BACKLOG, 128);
            b.childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            log.info("Server started on port {}", port);

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
