package com.ws.thread;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ws.domain.MessageTempBo;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static com.ws.WsServerHandler.clients;
import static com.ws.thread.ThreadSend.pendingMessages;

@Slf4j
public class MessageCreate {

    public void messageCreate() {
        //String fileName = "test.json";
        //Resource resource = new ClassPathResource(fileName);
        //String message = IoUtil.read(resource.getStream(), StandardCharsets.UTF_8);
        //System.out.println(message);
        // 启动新线程发送消息
        Thread sendThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(10 * 1000); // 每隔10秒发送300条消息
                    log.info("----开始创建消息----");
                    log.info("----当前通道数为---->{}",clients.size());
                    for (int i = 0; i < 300; i++) {
                        clients.forEach(channel -> {
                            try {
                                String message= RandomUtil.randomString(500000);
                                if (channel.isWritable()) {
                                    log.info("当前可写，写入数据");
                                    channel.writeAndFlush(new TextWebSocketFrame(message));
                                } else {
                                    MessageTempBo bo = new MessageTempBo();
                                    bo.setMessage(message);
                                    bo.setChannel(channel);
                                    bo.setDate(new Date());
                                    pendingMessages.add(bo);
                                    log.error("当前缓冲区已满，消息放入队列  {}条", pendingMessages.size());
                                }
                            } catch (Exception e) {
                                //如果发送失败表明该channel已经掉线
                                log.error("发送失败", e);
                            }
                        });
                    }
                    log.info("----创建消息完成----");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        sendThread.start();
    }
}
