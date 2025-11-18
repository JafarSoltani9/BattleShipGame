package com.example.battleship.dto;

import com.example.battleship.domain.Player;

public record FireResponse(
        boolean hit,
        boolean sunk,
        String sunkShipType,   // null if not sunk
        String nextState,
        Player winner          // null unless game finished
) {}
