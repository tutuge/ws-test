package com.ws;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;

/**
 * 客户端抽象
 *
 * @date 2023/05/18
 */
@Slf4j
public abstract class AbstractWebsocketClient implements Closeable {

    /**
     * 发送消息.<br>
     *
     * @param message 发送文本
     */
    public void send(String message) throws MyException {
        log.info("发送的消息-->{}", message);
        Channel channel = getChannel();
        if (channel != null) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
            return;
        }
        throw new MyException("连接已经关闭");
    }

    /**
     * 连接并发送消息.<br>
     */
    public void connect() throws MyException {
        try {
            doOpen();
            doConnect();
        } catch (Exception e) {
            throw new MyException("连接没有成功打开,原因是:{}" + e.getMessage(), e);
        }
    }

    /**
     * 初始化连接.<br>
     */
    protected abstract void doOpen();

    /**
     * 建立连接.<br>
     */
    protected abstract void doConnect() throws MyException;

    /**
     * 获取本次连接channel.<br>
     *
     * @return {@link Channel}
     */
    protected abstract Channel getChannel();

    /**
     * 关闭连接.<br>
     */
    @Override
    public abstract void close();

}
