package com.example.battleship.api;

import com.example.battleship.domain.Game;
import com.example.battleship.domain.Player;
import com.example.battleship.dto.*;
import com.example.battleship.service.GameService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/game")
public class GameController {

    @Autowired
    private GameService svc;


    // POST /api/game players
    @PostMapping
    public CreateGameResponse create(@RequestParam String p1,
                                     @RequestParam String p2) {
        Game g = svc.createGame(p1, p2);
        return new CreateGameResponse(g.getId());
    }
    // GET /api/game/ player 1
    @GetMapping("/{id}")
    public GameViewDto view(@PathVariable String id, @RequestParam Player viewer) {
        var g = svc.getGameOr404(id);
        return svc.view(g, viewer);
    }

    // POST /api/game/ place ship
    @PostMapping("/{id}/place-ship")
    public ResponseEntity<?> place(@PathVariable String id, @RequestBody @Valid PlaceShipRequest req) {
        var g = svc.getGameOr404(id);
        svc.placeShip(g, req);
        return ResponseEntity.ok().build();
    }

    // POST /api/game/ random placement
    @PostMapping("/{id}/random-placement")
    public ResponseEntity<?> random(@PathVariable String id, @RequestBody @Valid RandomPlacementRequest req) {
        var g = svc.getGameOr404(id);
        svc.randomPlacement(g, req.player());
        return ResponseEntity.ok().build();
    }

    // POST /api/game/try to fire
    @PostMapping("/{id}/fire")
    public FireResponse fire(@PathVariable String id, @RequestBody @Valid FireRequest req) {
        var g = svc.getGameOr404(id);
        return svc.fire(g, req);
    }
}
