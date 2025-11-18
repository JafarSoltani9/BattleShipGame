package com.example.battleship.service;

import com.example.battleship.domain.Game;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameStore implements IGameStore {
    private final ConcurrentHashMap<String, Game> store = new ConcurrentHashMap<>();

    @Override
    public void put(Game g) {
        store.put(g.getId(), g);
    }

    @Override
    public Game get(String id) {
        return store.get(id);
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }

    @Override
    public boolean exists(String id) {
        return store.containsKey(id);
    }

    @Override
    public void clear() {
        store.clear();
    }


}
