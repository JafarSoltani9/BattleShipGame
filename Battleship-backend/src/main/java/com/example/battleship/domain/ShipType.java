package com.example.battleship.domain;

public enum ShipType {
    CARRIER(5),
    BATTLESHIP(4),
    CRUISER(3),
    SUBMARINE(3),
    DESTROYER(2);

    public final int length;
    ShipType(int length) { this.length = length; }
}
