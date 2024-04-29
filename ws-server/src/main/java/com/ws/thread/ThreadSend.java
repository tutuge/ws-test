package com.ws.thread;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import com.ws.domain.MessageTempBo;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
public class ThreadSend {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 创建单线程的线程池

    static ThreadFactory threadFactory = new CustomThreadFactory("print-queue-size");

    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(threadFactory); // 创建单线程的线程池

    public static Queue<MessageTempBo> pendingMessages = new LinkedList<>(); // 存放待发送的消息

    public void sendMessage() {
        log.warn("进入发送队列消息的方法");
        executorService.execute(() -> {
            while (true) {
                try {
                    MessageTempBo poll = pendingMessages.poll();
                    if (ObjUtil.isNotNull(poll)) {
                        log.warn(" 1 当前消息队列内还有 {} 条数据", pendingMessages.size());
                        //不为空的情况下，先判断对应的channel是否活着
                        Channel channel = poll.getChannel();
                        SocketAddress socketAddress = channel.remoteAddress();
                        log.info(" 2 当前channel的远程地址 {}", socketAddress);
                        Date date = poll.getDate();
                        //判定下消息是否过期了
                        long between = DateUtil.between(date, new Date(), DateUnit.SECOND);
                        if (between < 20) {
                            if (channel.isActive()) {
                                if (channel.isWritable()) {
                                    log.warn("3 当前channel可写，当前消息队列内还有 {} 条数据", pendingMessages.size());
                                    channel.writeAndFlush(new TextWebSocketFrame(poll.getMessage()));
                                } else {
                                    log.warn("4 当前channel还是不可写，当前消息队列内还有 {} 条数据", pendingMessages.size());
                                    pendingMessages.add(poll);
                                    //不可写的情况下暂停200毫秒
                                    Thread.sleep(200);
                                }
                            } else {
                                pendingMessages.add(poll);
                                log.info("5 当前channel不处于active状态");
                            }
                        }else {
                            log.error("当前消息已过期");
                        }
                    }
                    else {
                        Thread.sleep(1000);
                        log.info("7 当前从队列中拿出的数据-->{}", poll);
                    }
                } catch (Exception e) {
                    log.error("8 当前队列重试报错", e);
                }
            }
        });

        // 参数：1、任务体 2、首次执行的延时时间
        //      3、任务执行间隔 4、间隔时间单位
        service.scheduleAtFixedRate(() ->
                log.warn("当前消息队列内还有 {} 条数据", pendingMessages.size()), 0, 5, TimeUnit.SECONDS);
    }

}
