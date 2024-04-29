package com.ws.thread;

import cn.hutool.core.util.ObjUtil;
import com.ws.WebsocketClient;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class ThreadSend {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 创建单线程的线程池

    static ThreadFactory threadFactory = new CustomThreadFactory("websocket-status");

    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(threadFactory); // 创建单线程的线程池

    private static WebsocketClient websocketClient = null;

    public void start() {

        connect();

        // 参数：1、任务体 2、首次执行的延时时间
        //      3、任务执行间隔 4、间隔时间单位
        service.scheduleAtFixedRate(() -> {
                    log.warn("当前ws通道是否连接正常 {}", websocketClient.isOpen());
                    if (ObjUtil.isNull(websocketClient) || !websocketClient.isOpen()) {
                        connect();
                    }
                }
                , 0, 5, TimeUnit.SECONDS);
    }

    private void connect() {
        try {
            websocketClient = new WebsocketClient("ws://127.0.0.1:88/ws", false);
            websocketClient.connect();
        } catch (Exception e) {
            log.error("连接ws服务端失败", e);
        }
    }

}
