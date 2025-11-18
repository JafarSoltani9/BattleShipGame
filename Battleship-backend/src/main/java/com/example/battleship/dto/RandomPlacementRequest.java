package com.example.battleship.dto;

import com.example.battleship.domain.Player;
import jakarta.validation.constraints.NotNull;

public record RandomPlacementRequest(@NotNull Player player) {

}
