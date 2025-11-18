package com.example.battleship.dto;

import com.example.battleship.domain.Orientation;
import com.example.battleship.domain.Player;
import com.example.battleship.domain.ShipType;
import jakarta.validation.constraints.*;

public record PlaceShipRequest(
        @NotNull Player player,
        @NotNull ShipType shipType,
        @Min(0) @Max(9) int row,
        @Min(0) @Max(9) int col,
        @NotNull Orientation orientation
) {}
