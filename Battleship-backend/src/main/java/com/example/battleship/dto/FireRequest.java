package com.example.battleship.dto;


import com.example.battleship.domain.Player;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FireRequest(
        @NotNull Player player,
        @Min(0) @Max(9) int row,
        @Min(0) @Max(9) int col
) {}
