package com.example.battleship.domain;

import java.util.ArrayList;
import java.util.List;

public class Board {
    public static final int SIZE = 10;

    private final CellState[][] grid = new CellState[SIZE][SIZE];
    private final List<Ship> ships = new ArrayList<>();

    public Board() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = CellState.EMPTY;
    }

    public CellState[][] getGrid() { return grid; }
    public List<Ship> getShips() { return ships; }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }
}
