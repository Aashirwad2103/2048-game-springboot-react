package com.example.game.dto;

public class GameStateResponse {
    private String id;
    private int size;
    private int[][] board;
    private int score;
    private boolean won;
    private boolean over;

    public GameStateResponse(String id, int size, int[][] board, int score, boolean won, boolean over) {
        this.id = id;
        this.size = size;
        this.board = board;
        this.score = score;
        this.won = won;
        this.over = over;
    }

    public String getId() { return id; }
    public int getSize() { return size; }
    public int[][] getBoard() { return board; }
    public int getScore() { return score; }
    public boolean isWon() { return won; }
    public boolean isOver() { return over; }
}
