package com.example.game.model;

public class Tile {
    private int value;

    public Tile(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == 0;
    }

    public static Tile empty() {
        return new Tile(0);
    }

    public static Tile of(int v) {
        return new Tile(v);
    }
}
