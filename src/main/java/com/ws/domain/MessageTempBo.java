package com.ws.domain;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.Date;

/**
 * 将要发送的ws消息暂存的队列
 */
@Data
public class MessageTempBo {

    /**
     * 数据放进来的时间
     */
    private Date date;

    /**
     * 数据时间类型 timeType:1-1min，2-5min， 3-15min，4-30min，5-1h，6-1day，7-1week，8-1month
     */
    private String timeType;

    /**
     * 消息要发送给的通道
     */
    private Channel channel;

    /**
     * 要发送的消息
     */
    private String message;
}
