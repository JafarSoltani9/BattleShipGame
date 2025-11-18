package com.example.battleship.service;

import com.example.battleship.domain.*;
import com.example.battleship.dto.*;
import com.example.battleship.util.Masking;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.*;

@Service
public class GameService {
    private final GameStore store;
    private final SecureRandom rnd = new SecureRandom();

    public GameService(GameStore store) { this.store = store; }

    // Create a new game
    public Game createGame(String p1Name, String p2Name) {
        var id = UUID.randomUUID().toString();
        var g = new Game(id);
        if (p1Name != null && !p1Name.isBlank()) g.setPlayerName(Player.P1, p1Name);
        if (p2Name != null && !p2Name.isBlank()) g.setPlayerName(Player.P2, p2Name);
        store.put(g);
        return g;
    }

    public Game getGameOr404(String id) {
        var g = store.get(id);
        if (g == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        return g;
    }

    // Placement of ships
    public void placeShip(Game game, PlaceShipRequest req) {
        ensurePlacingPhase(game, req.player());

        var board = game.getBoard(req.player());
        var cells = computeCells(req.row(), req.col(), req.orientation(), req.shipType().length);

        if (!canPlaceShip(board, cells))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid placement (overlap/touch/out of bounds)");

        // place
        var ship = new Ship(req.shipType(), cells);
        board.getShips().add(ship);
        for (var c : cells) board.getGrid()[c.row()][c.col()] = CellState.SHIP;

        // if all 5 ships placed -> advance state
        if (board.getShips().size() == 5) {
            if (game.getState() == GameState.PLACING_P1) game.setState(GameState.PLACING_P2);
            else if (game.getState() == GameState.PLACING_P2) game.setState(GameState.TURN_P1);
        }
    }

    // Random placement of ships
    public void randomPlacement(Game game, Player player) {
        ensurePlacingPhase(game, player);
        var board = game.getBoard(player);
        board.getShips().clear();
        for (int r=0;r<Board.SIZE;r++)
            for (int c=0;c<Board.SIZE;c++)
                board.getGrid()[r][c] = CellState.EMPTY;

        for (var type : ShipType.values()) {
            boolean placed = false;
            for (int tries = 0; tries < 500 && !placed; tries++) {
                var ori = rnd.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                int row = rnd.nextInt(Board.SIZE);
                int col = rnd.nextInt(Board.SIZE);
                var cells = computeCells(row, col, ori, type.length);
                if (canPlaceShip(board, cells)) {
                    var ship = new Ship(type, cells);
                    board.getShips().add(ship);
                    for (var cell : cells) board.getGrid()[cell.row()][cell.col()] = CellState.SHIP;
                    placed = true;
                }
            }
            if (!placed)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Random placement failed, try again.");
        }

        if (game.getState() == GameState.PLACING_P1) game.setState(GameState.PLACING_P2);
        else if (game.getState() == GameState.PLACING_P2) game.setState(GameState.TURN_P1);
    }

    // Fire at opponent
    public FireResponse fire(Game game, FireRequest request) {
        ensureTurnPhase(game, request.player());

        var attacker = request.player();
        var defender = attacker == Player.P1 ? Player.P2 : Player.P1;
        var defBoard = game.getBoard(defender);

        if (!defBoard.inBounds(request.row(), request.col()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shot out of bounds");

        var st = defBoard.getGrid()[request.row()][request.col()];

        // ✅ already-fired cell: throw error
        if (st == CellState.HIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cell already targeted");
        }

        if (st == CellState.MISS || st == CellState.SUNK) {
            return new FireResponse(
                    false,                  // hit
                    false,                  // sunk
                    null,                   // sunkType
                    game.getState().name(), // nextState (unchanged)
                    game.getWinner()        // winner (unchanged, usually null)
            );
        }

        boolean hit = false;
        boolean sunk = false;
        String sunkType = null;
        var target = new Coord(request.row(), request.col());

        if (st == CellState.SHIP) {
            hit = true;
            defBoard.getGrid()[request.row()][request.col()] = CellState.HIT;

            for (var ship : defBoard.getShips()) {
                if (ship.registerHit(target)) {
                    if (ship.isSunk()) {
                        sunk = true;
                        sunkType = ship.getType().name();
                        for (var c : ship.getCells())
                            defBoard.getGrid()[c.row()][c.col()] = CellState.SUNK;
                    }
                    break;
                }
            }
        } else {
            defBoard.getGrid()[request.row()][request.col()] = CellState.MISS;
        }

        // check the loser
        if (defBoard.getShips().stream().allMatch(Ship::isSunk)) {
            game.setWinner(attacker);
            game.setState(GameState.FINISHED);
            return new FireResponse(hit, sunk, sunkType, game.getState().name(), game.getWinner());
        }

        // advance turn
        if (game.getState() == GameState.TURN_P1) game.setState(GameState.TURN_P2);
        else if (game.getState() == GameState.TURN_P2) game.setState(GameState.TURN_P1);

        return new FireResponse(hit, sunk, sunkType, game.getState().name(), null);
    }



    // View game state
    public GameViewDto view(Game game, Player viewer) {
        var your = copyGrid(game.getBoard(viewer).getGrid());
        var oppMasked = Masking.maskOpponent(game.getOpponentBoard(viewer).getGrid());
        return new GameViewDto(
                game.getId(),
                game.getState(),
                game.getWinner(),
                game.getPlayerName(Player.P1),
                game.getPlayerName(Player.P2),
                your,
                oppMasked
        );
    }



    //
    private void ensurePlacingPhase(Game game, Player player) {
        if (player == Player.P1 && game.getState() != GameState.PLACING_P1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not in PLACING_P1");
        if (player == Player.P2 && game.getState() != GameState.PLACING_P2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not in PLACING_P2");
    }

    // Ensure it's the player's turn
    private void ensureTurnPhase(Game game, Player player) {
        if (player == Player.P1 && game.getState() != GameState.TURN_P1)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not your turn");
        if (player == Player.P2 && game.getState() != GameState.TURN_P2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not your turn");
    }

    // Compute the list of cells a ship would occupy
    private List<Coord> computeCells(int row, int col, Orientation o, int len) {
        var cells = new ArrayList<Coord>(len);
        for (int i=0;i<len;i++) {
            int r = o == Orientation.HORIZONTAL ? row : row + i;
            int c = o == Orientation.HORIZONTAL ? col + i : col;
            cells.add(new Coord(r, c));
        }
        return cells;
    }

    // Check if a ship can be placed at the given cells
    private boolean canPlaceShip(Board b, List<Coord> cells) {

        for (var c : cells) {
            if (!b.inBounds(c.row(), c.col())) return false;
        }

        for (var c : cells) {
            for (int dr=-1; dr<=1; dr++) {
                for (int dc=-1; dc<=1; dc++) {
                    int rr = c.row()+dr, cc = c.col()+dc;
                    if (b.inBounds(rr, cc) && b.getGrid()[rr][cc] == CellState.SHIP)
                        return false;
                }
            }
        }
        return true;
    }


    private CellState[][] copyGrid(CellState[][] g) {
        var out = new CellState[Board.SIZE][Board.SIZE];
        for (int r=0;r<Board.SIZE;r++)
            System.arraycopy(g[r], 0, out[r], 0, Board.SIZE);
        return out;
    }
}
