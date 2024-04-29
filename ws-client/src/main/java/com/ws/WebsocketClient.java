package com.ws;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * websocket客户端
 *
 * @date 2023/05/18
 */
public class WebsocketClient extends AbstractWebsocketClient {

    private static final Logger log = LoggerFactory.getLogger(WebsocketClient.class);

    private static final NioEventLoopGroup NIO_GROUP = new NioEventLoopGroup();

    private final URI uri;

    private final int port;

    private final Boolean wss;

    private Bootstrap bootstrap;

    private WebsocketClientHandler handler;

    private Channel channel;


    public WebsocketClient(String url, Boolean wss) throws URISyntaxException, MyException {
        super();
        this.uri = new URI(url);
        this.wss = wss;
        this.port = getPort();
    }

    /**
     * Extract the specified port
     *
     * @return the specified port or the default port for the specific scheme
     */
    private int getPort() throws MyException {
        int port = uri.getPort();
        if (port == -1) {
            String scheme = uri.getScheme();
            if ("wss".equals(scheme)) {
                return 443;
            } else if ("ws".equals(scheme)) {
                return 80;
            } else {
                throw new MyException("unknown scheme: " + scheme);
            }
        }
        return port;
    }

    @SneakyThrows
    @Override
    protected void doOpen() {
        // websocket客户端握手实现的基类
        WebSocketClientHandshaker webSocketClientHandshaker =
                WebSocketClientHandshakerFactory.newHandshaker(uri,
                        WebSocketVersion.V13, null, false, new DefaultHttpHeaders(),
                        65536 * 10);
        // 业务处理类
        handler = new WebsocketClientHandler(webSocketClientHandshaker);
        SslContext sslContext = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);

        // client端，引导client channel启动
        bootstrap = new Bootstrap();
        // 添加管道 绑定端口 添加作用域等
        bootstrap.group(NIO_GROUP)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        if (wss) {
                            ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), uri.getHost(), port));
                        }
                        ch.pipeline().addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                new WebSocketFrameAggregator(8192),
                                handler
                        );
                    }
                });
    }

    @Override
    public void doConnect() {
        try {
            // 启动连接
            ChannelFuture sync = bootstrap.connect(uri.getHost(), port).sync();
            channel = sync.channel();
            // 等待握手响应
            handler.handshakeFuture().sync();
            log.info("websocket连接完成");
        } catch (InterruptedException e) {
            log.error("websocket连接发生异常", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected Channel getChannel() {
        return channel;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public boolean isOpen() {
        if (channel != null) {
            return channel.isOpen();
        }
        return false;
    }
}
