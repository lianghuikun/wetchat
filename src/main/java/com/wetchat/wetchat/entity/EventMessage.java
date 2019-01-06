package com.wetchat.wetchat.entity;

import lombok.Data;

/**
 * 事件， 取消订阅和关注
 */
@Data
public class EventMessage extends BaseMessage {
    /**
     * 事件类型，subscribe(订阅)、unsubscribe(取消订阅)
     */
    private String Event;
    /**
     * 扫描带参数的二维码会含有这个参数
     */
    private String EventKey;
}
