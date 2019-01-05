package com.wetchat.wetchat;

import com.wetchat.wetchat.entity.Game;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/game")
public class GameController {

    @GetMapping("/getGameList")
    private List<Game> getGameList() {
        return Arrays.asList(new Game(1L, "梦幻西游", "网易"));
    }
}
