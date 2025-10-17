package com.example.game.service;

import com.example.game.model.*;
import com.example.game.dto.GameStateResponse;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private static class GameData {
        private final Board board;
        private final int score;
        public GameData(Board board, int score) {
            this.board = board;
            this.score = score;
        }
        public Board getBoard() { return board; }
        public int getScore() { return score; }
    }
    private final Map<String, GameData> store = new ConcurrentHashMap<>();

    public GameStateResponse newGame(int size){
        Board b = new Board(size)
                .withPlacedRandomTile()
                .withPlacedRandomTile();
        String id = UUID.randomUUID().toString();
        store.put(id, new GameData(b, 0));
        return toResponse(id, b, 0);
    }

    public GameStateResponse move(String id, Direction dir){
        GameData gd = store.get(id);
        if (gd == null) throw new NoSuchElementException("Game not found");
        Board.Result res = gd.board.move(dir);
        Board nextBoard = res.changed ? res.board.withPlacedRandomTile() : res.board;
        int newScore = gd.score + res.gained;
        store.put(id, new GameData(nextBoard, newScore));
        return toResponse(id, nextBoard, newScore);
    }

    public GameStateResponse restart(String id, int size){
        Board b = new Board(size).withPlacedRandomTile().withPlacedRandomTile();
        store.put(id, new GameData(b, 0));
        return toResponse(id, b, 0);
    }

    public GameStateResponse get(String id){
        GameData gd = store.get(id);
        if (gd == null) throw new NoSuchElementException("Game not found");
        return toResponse(id, gd.board, gd.score);
    }

    private GameStateResponse toResponse(String id, Board b, int score){
        boolean won = b.contains(2048);
        boolean over = !b.canMove();
        return new GameStateResponse(id, b.getSize(), b.getGrid(), score, won, over);
    }
}

