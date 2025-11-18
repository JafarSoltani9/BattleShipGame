package com.example.battleship.dto;


import com.example.battleship.domain.CellState;
import com.example.battleship.domain.GameState;
import com.example.battleship.domain.Player;

public record GameViewDto(
        String gameId,
        GameState state,
        Player winner,
        String p1Name,
        String p2Name,
        CellState[][] yourBoard,
        CellState[][] opponentBoardMasked
) {}
