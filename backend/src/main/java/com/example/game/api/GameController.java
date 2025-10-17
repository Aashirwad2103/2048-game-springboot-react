package com.example.game.api;


import com.example.game.dto.GameStateResponse;
import com.example.game.model.Direction;
import com.example.game.service.GameService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService svc;
    public GameController(GameService svc){this.svc = svc;}

    public static class NewGameRequest {
        private int size;
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
    @PostMapping
    public GameStateResponse create(@RequestBody(required=false) NewGameRequest req){
        int size = (req==null || req.size<=0) ? 4 : req.size;
        return svc.newGame(size);
    }

    @GetMapping("/{id}")
    public GameStateResponse get(@PathVariable String id){
        return svc.get(id);
    }

    public static class MoveRequest {
        private String direction;
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
    }

    @PostMapping("/{id}/move")
    public GameStateResponse move(@PathVariable String id, @RequestBody MoveRequest req){
        Direction dir = Direction.valueOf(req.direction.toUpperCase());
        return svc.move(id, dir);
    }

    public static class RestartRequest {
        private int size;
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
    @PostMapping("/{id}/restart")
    public GameStateResponse restart(@PathVariable String id, @RequestBody(required=false) RestartRequest req){
        int size = (req==null || req.size<=0) ? 4 : req.size;
        return svc.restart(id, size);
    }
}
