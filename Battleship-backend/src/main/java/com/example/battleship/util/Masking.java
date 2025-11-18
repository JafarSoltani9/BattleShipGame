package com.example.battleship.util;

import com.example.battleship.domain.CellState;

public class Masking {
    public static CellState[][] maskOpponent(CellState[][] g) {
        int n = g.length;
        var out = new CellState[n][n];
        for (int r=0;r<n;r++) {
            for (int c=0;c<n;c++) {
                var s = g[r][c];
                if (s == CellState.SHIP) out[r][c] = CellState.EMPTY;
                else out[r][c] = s;
            }
        }
        return out;
    }
}
