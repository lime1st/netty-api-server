package lime1st.netty.server.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;

@Component
public final class ApiServer {

    private static final Logger log = LoggerFactory.getLogger(ApiServer.class);

    private final InetSocketAddress address;
    private final int workerThreadCount;
    private final int bossThreadCount;

    public ApiServer(
            @Qualifier("tcpSocketAddress") InetSocketAddress address,
            @Qualifier("workerThreadCount") int workerThreadCount,
            @Qualifier("bossThreadCount") int bossThreadCount
    ) {
        this.address = address;
        this.workerThreadCount = workerThreadCount;
        this.bossThreadCount = bossThreadCount;
    }

    public void start() {
        final EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadCount);
        final EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadCount);

        // This latch will become zero one `...Handler` receives the ... command.
        final CountDownLatch shutdownLatch = new CountDownLatch(1);
        ChannelFuture channelFuture = null;

        try {
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.childHandler(new ApiServerInitializer(null));

            final Channel ch = b.bind(8080).sync().channel();

            channelFuture = ch.closeFuture();

            /*
            다음과 같은 방법으로 두 개의 서버 부트스트랩을 설정하여 두 개의 포트를 사용할 수 있다..
            final SslContext sslCtx;
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());

            ServerBootstrap b2 = new ServerBootstrap();
            b2.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ApiServerInitializer(sslCtx));

            Channel ch2 = b2.bind(8443).sync().channel();

            channelFuture = ch2.closeFuture();
             */

            channelFuture.sync();

            // wait until the latch becomes zero.
            shutdownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
