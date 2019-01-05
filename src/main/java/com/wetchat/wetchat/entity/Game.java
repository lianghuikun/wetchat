package com.wetchat.wetchat.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class Game implements Serializable {
    private Long id;
    private String gameName;
    private String descr;

    public Game(Long id, String gameName, String descr) {
        this.id = id;
        this.gameName = gameName;
        this.descr = descr;
    }
}
