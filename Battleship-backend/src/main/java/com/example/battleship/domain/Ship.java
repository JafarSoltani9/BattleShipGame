package com.example.battleship.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ship {
    private final ShipType type;
    private final List<Coord> cells;
    private final Set<Coord> hits = new HashSet<>();

    public Ship(ShipType type, List<Coord> cells) {
        this.type = type;
        this.cells = List.copyOf(cells);
    }
    public ShipType getType() { return type; }
    public List<Coord> getCells() { return cells; }

    public boolean registerHit(Coord c) {
        if (cells.contains(c)) {
            hits.add(c);
            return true;
        }
        return false;
    }
    public boolean isSunk() { return hits.size() == cells.size(); }
}