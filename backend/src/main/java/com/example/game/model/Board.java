package com.example.game.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

public final class Board {
    private final int size;
    private final int[][] grid; // use primitives for serialization
    private final Random random = new Random();

    public Board(int size) {
        this.size = size;
        this.grid = new int[size][size];
    }

    // copy constructor
    private Board(int size, int[][] grid) {
        this.size = size;
        this.grid = grid;
    }

    public int getSize() { return size; }
    public int[][] getGrid() {
        // return deep copy
        int[][] copy = new int[size][size];
        for (int i=0;i<size;i++) System.arraycopy(grid[i],0,copy[i],0,size);
        return copy;
    }

    public Board withPlacedRandomTile() {
        List<int[]> empties = new ArrayList<>();
        for (int r=0;r<size;r++) for (int c=0;c<size;c++) if (grid[r][c]==0) empties.add(new int[]{r,c});
        if (empties.isEmpty()) return this;
        int[] pos = empties.get(ThreadLocalRandom.current().nextInt(empties.size()));
        int r = pos[0], c = pos[1];
        int val = ThreadLocalRandom.current().nextDouble() < 0.9 ? 2 : 4;
        int[][] copy = getGrid();
        copy[r][c] = val;
        return new Board(size, copy);
    }

    // helper to slide & merge one line (left) returns [newLine, gainedScore]
    private static class SlideResult { int[] line; int gained; SlideResult(int[] l,int g){line=l;gained=g;} }

    private SlideResult slideAndMergeLeft(int[] line) {
        int n = line.length;
        List<Integer> nonZero = Arrays.stream(line).filter(x -> x!=0).boxed().collect(Collectors.toList());
        List<Integer> result = new ArrayList<>();
        int gained = 0;
        for (int i=0;i<nonZero.size();i++){
            int cur = nonZero.get(i);
            if (i+1<nonZero.size() && nonZero.get(i+1)==cur){
                result.add(cur*2);
                gained += cur*2;
                i++; // skip next
            } else {
                result.add(cur);
            }
        }
        while(result.size() < n) result.add(0);
        int[] arr = result.stream().mapToInt(Integer::intValue).toArray();
        return new SlideResult(arr, gained);
    }


    private static int[][] transpose(int[][] matrix) {
        int n = matrix.length;
        int[][] transposed = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                transposed[i][j] = matrix[j][i];
            }
        }
        return transposed;
    }

    private static int[] reverse(int[] row) {
        int[] r = new int[row.length];
        for (int i = 0; i < row.length; i++) {
            r[i] = row[row.length - 1 - i];
        }
        return r;
    }

    private static int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[grid.length][grid.length];
        for (int i = 0; i < grid.length; i++) {
            copy[i] = Arrays.copyOf(grid[i], grid[i].length);
        }
        return copy;
    }

    // Helper class to return row update details
    private static class MoveResult {
        int[] newRow;
        int gained;
        boolean changed;
        MoveResult(int[] newRow, int gained, boolean changed) {
            this.newRow = newRow;
            this.gained = gained;
            this.changed = changed;
        }
    }

    private static MoveResult compressAndMerge(int[] row) {
        int[] filtered = Arrays.stream(row).filter(v -> v != 0).toArray();
        int[] newRow = new int[row.length];
        int gained = 0;
        int j = 0;
        for (int i = 0; i < filtered.length; i++) {
            if (i < filtered.length - 1 && filtered[i] == filtered[i + 1]) {
                newRow[j++] = filtered[i] * 2;
                gained += filtered[i] * 2;
                i++; // skip next
            } else {
                newRow[j++] = filtered[i];
            }
        }
        boolean changed = !Arrays.equals(row, newRow);
        return new MoveResult(newRow, gained, changed);
    }


    // returns pair {newBoard, gainedScore}
    public Result move(Direction dir) {
        int[][] grid = copyGrid(this.grid);
        int gained = 0;
        boolean changed = false;

        switch (dir) {
            case LEFT:
                for (int i = 0; i < size; i++) {
                    MoveResult r = compressAndMerge(grid[i]);
                    grid[i] = r.newRow;
                    gained += r.gained;
                    if (r.changed) changed = true;
                }
                break;

            case RIGHT:
                for (int i = 0; i < size; i++) {
                    int[] reversed = reverse(grid[i]);
                    MoveResult r = compressAndMerge(reversed);
                    grid[i] = reverse(r.newRow);
                    gained += r.gained;
                    if (r.changed) changed = true;
                }
                break;

            case UP:
                grid = transpose(grid);
                for (int i = 0; i < size; i++) {
                    MoveResult r = compressAndMerge(grid[i]);
                    grid[i] = r.newRow;
                    gained += r.gained;
                    if (r.changed) changed = true;
                }
                grid = transpose(grid);
                break;

            case DOWN:
                grid = transpose(grid);
                for (int i = 0; i < size; i++) {
                    int[] reversed = reverse(grid[i]);
                    MoveResult r = compressAndMerge(reversed);
                    grid[i] = reverse(r.newRow);
                    gained += r.gained;
                    if (r.changed) changed = true;
                }
                grid = transpose(grid);
                break;
        }

        Board newBoard = new Board(size, grid);
        return new Result(newBoard, gained,changed);
    }


    public boolean hasEmpty() {
        for (int r=0;r<size;r++) for (int c=0;c<size;c++) if (grid[r][c]==0) return true;
        return false;
    }

    public boolean canMove() {
        if (hasEmpty()) return true;
        // check neighbors for same values
        for (int r=0;r<size;r++){
            for (int c=0;c<size;c++){
                int v = grid[r][c];
                if (r+1<size && grid[r+1][c]==v) return true;
                if (c+1<size && grid[r][c+1]==v) return true;
            }
        }
        return false;
    }

    public boolean contains(int value){
        for (int r=0;r<size;r++) for (int c=0;c<size;c++) if (grid[r][c] == value) return true;
        return false;
    }

    public static class Result {
        public final Board board;
        public final int gained;
        public final boolean changed;
        public Result(Board b,int g,boolean ch){this.board=b;this.gained=g;this.changed=ch;}
    }
}
