package com.wetchat.wetchat.entity;

import lombok.Data;

/**
 * 文本消息工具类
 */
@Data
public class TextMessage {
    // 开发者微信号
    private String ToUserName;
    // 发送方帐号（一个OpenID）
    private String FromUserName;
    // 消息创建时间 （整型）
    private long CreateTime;
    // 消息类型（text/image/location/link）
    private String MsgType;
    //文本内容
    private String Content;
    // 消息id，64位整型
    private long MsgId;
}
