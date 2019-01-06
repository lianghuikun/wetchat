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
}
