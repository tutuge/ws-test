package com.ws;

import com.ws.thread.MessageCreate;
import com.ws.thread.ThreadSend;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebsocketServer {

    //创建netty
    public void netty() {
        // 一个主线程组(用于监听新连接并初始化通道)，一个分发线程组(用于IO事件的处理)
        EventLoopGroup mainGroup = new NioEventLoopGroup(1);
        EventLoopGroup subGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(mainGroup, subGroup)
                    .channel(NioServerSocketChannel.class)
                    // 这里是一个自定义的通道初始化器，用来添加编解码器和处理器
                    .childHandler(new WsChannelInitializer());
            //设置Socket参数，禁用Nagle算法，启用快速ACK机制
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            //设置Socket参数，启用心跳机制
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            //最大的排队等待处理的最大客户端连接数，现在设置300，当等待连接的客户端超过300则拒绝后面的连接
            bootstrap.option(ChannelOption.SO_BACKLOG, 300);
            //设置发送缓冲区大小为1M--很重要，消息发送判断缓冲区是否已满，满了就本次不再发送，直到缓冲区释放
            bootstrap.childOption(ChannelOption.SO_SNDBUF, 1024 * 1024);
            // 绑定88端口，Websocket服务器的端口就是这个
            ChannelFuture future = bootstrap.bind(88).sync();

            //创建消息，模拟通道缓存已满的情况
            MessageCreate messageCreate = new MessageCreate();
            messageCreate.messageCreate();

            //模拟内存队列，数据发送
            ThreadSend threadSend = new ThreadSend();
            threadSend.sendMessage();

            // 一直阻塞直到服务器关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放资源
            mainGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }

    }
}
