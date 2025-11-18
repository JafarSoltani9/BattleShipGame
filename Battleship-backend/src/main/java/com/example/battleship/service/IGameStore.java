package com.example.battleship.service;


import com.example.battleship.domain.Game;

public interface IGameStore {
    void put(Game game);
    Game get(String id);
    void delete(String id);
    boolean exists(String id);
    void clear();
}

