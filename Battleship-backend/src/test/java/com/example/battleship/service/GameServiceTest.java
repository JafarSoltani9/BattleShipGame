package com.example.battleship.service;

import com.example.battleship.domain.*;
import com.example.battleship.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GameService Tests")
class GameServiceTest {
    private GameService gameService;
    private GameStore gameStore;

    @BeforeEach
    void setUp() {
        gameStore = new GameStore();
        gameService = new GameService(gameStore);
    }

    // ========== CREATE GAME TESTS ==========
    @Test
    @DisplayName("Should create game with both player names")
    void testCreateGameWithBothNames() {
        var game = gameService.createGame("Alice", "Bob");

        assertThat(game).isNotNull();
        assertThat(game.getId()).isNotNull().isNotBlank();
        assertThat(game.getPlayerName(Player.P1)).isEqualTo("Alice");
        assertThat(game.getPlayerName(Player.P2)).isEqualTo("Bob");
        assertThat(game.getState()).isEqualTo(GameState.PLACING_P1);
    }

    @Test
    @DisplayName("Should create game with only P1 name")
    void testCreateGameWithP1NameOnly() {

        var game = gameService.createGame("Alice", null);

        assertThat(game.getPlayerName(Player.P1)).isEqualTo("Alice");
        assertThat(game.getPlayerName(Player.P2)).isEqualTo("Player 2");
    }

    @Test
    @DisplayName("Should create game with only P2 name")
    void testCreateGameWithP2NameOnly() {
        var game = gameService.createGame(null, "Bob");

        assertThat(game.getPlayerName(Player.P1)).isEqualTo("Player 1");
        assertThat(game.getPlayerName(Player.P2)).isEqualTo("Bob");
    }

    @Test
    @DisplayName("Should create game with blank names ignored")
    void testCreateGameWithBlankNames() {
        var game = gameService.createGame("  ", "   ");

        assertThat(game.getPlayerName(Player.P1)).isEqualTo("Player 1");
        assertThat(game.getPlayerName(Player.P2)).isEqualTo("Player 2");
    }

    @Test
    @DisplayName("Should store created game in store")
    void testCreateGameStoresInStore() {
        var game = gameService.createGame("Alice", "Bob");
        var retrieved = gameStore.get(game.getId());

        assertThat(retrieved).isEqualTo(game);
    }

    // ========== GET GAME TESTS ==========
    @Test
    @DisplayName("Should retrieve existing game")
    void testGetGameOr404Found() {
        var created = gameService.createGame("Alice", "Bob");
        var retrieved = gameService.getGameOr404(created.getId());

        assertThat(retrieved).isEqualTo(created);
    }

    @Test
    @DisplayName("Should throw 404 when game not found")
    void testGetGameOr404NotFound() {
        assertThatThrownBy(() -> gameService.getGameOr404("nonexistent-id"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Game not found");
    }

    // ========== PLACE SHIP TESTS ==========
    @Test
    @DisplayName("Should place ship horizontally")
    void testPlaceShipHorizontal() {
        var game = gameService.createGame("Alice", "Bob");
        var req = new PlaceShipRequest(Player.P1 , ShipType.CARRIER,0, 0, Orientation.HORIZONTAL);

        gameService.placeShip(game, req);

        var board = game.getBoard(Player.P1);
        assertThat(board.getShips()).hasSize(1);
        assertThat(board.getShips().get(0).getType()).isEqualTo(ShipType.CARRIER);
        assertThat(board.getGrid()[0][0]).isEqualTo(CellState.SHIP);
        assertThat(board.getGrid()[0][4]).isEqualTo(CellState.SHIP);
    }

    @Test
    @DisplayName("Should place ship vertically")
    void testPlaceShipVertical() {
        var game = gameService.createGame("Alice", "Bob");
        var req = new PlaceShipRequest(Player.P1, ShipType.BATTLESHIP, 0, 0, Orientation.VERTICAL);

        gameService.placeShip(game, req);

        var board = game.getBoard(Player.P1);
        assertThat(board.getShips()).hasSize(1);
        assertThat(board.getGrid()[0][0]).isEqualTo(CellState.SHIP);
        assertThat(board.getGrid()[3][0]).isEqualTo(CellState.SHIP);
    }

    @Test
    @DisplayName("Should reject placement with invalid state")
    void testPlaceShipInvalidState() {
        var game = gameService.createGame("Alice", "Bob");
        game.setState(GameState.TURN_P1);
        var req = new PlaceShipRequest(Player.P1,ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);

        assertThatThrownBy(() -> gameService.placeShip(game, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not in PLACING_P1");
    }

    @Test
    @DisplayName("Should reject placement out of bounds")
    void testPlaceShipOutOfBounds() {
        var game = gameService.createGame("Alice", "Bob");
        var req = new PlaceShipRequest(Player.P1,ShipType.CARRIER, 0, 7, Orientation.HORIZONTAL);

        assertThatThrownBy(() -> gameService.placeShip(game, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid placement");
    }

    @Test
    @DisplayName("Should reject overlapping ship placement")
    void testPlaceShipOverlap() {
        var game = gameService.createGame("Alice", "Bob");
        var req1 = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);
        var req2 = new PlaceShipRequest(Player.P1, ShipType.CRUISER, 0, 2, Orientation.HORIZONTAL);

        gameService.placeShip(game, req1);

        assertThatThrownBy(() -> gameService.placeShip(game, req2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid placement");
    }

    @Test
    @DisplayName("Should reject touching ship placement")
    void testPlaceShipTouching() {
        var game = gameService.createGame("Alice", "Bob");
        var req1 = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);
        var req2 = new PlaceShipRequest(Player.P1, ShipType.CRUISER, 1, 0, Orientation.HORIZONTAL);

        gameService.placeShip(game, req1);

        assertThatThrownBy(() -> gameService.placeShip(game, req2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid placement");
    }

    @Test
    @DisplayName("Should transition to PLACING_P2 when P1 places 5 ships")
    void testStateTransitionToP2Placing() {
        var game = gameService.createGame("Alice", "Bob");
        placeAllShipsForPlayer(game, Player.P1);

        assertThat(game.getState()).isEqualTo(GameState.PLACING_P2);
    }

    @Test
    @DisplayName("Should transition to TURN_P1 when P2 places 5 ships")
    void testStateTransitionToGameStart() {
        var game = gameService.createGame("Alice", "Bob");
        placeAllShipsForPlayer(game, Player.P1);
        placeAllShipsForPlayer(game, Player.P2);

        assertThat(game.getState()).isEqualTo(GameState.TURN_P1);
    }

    // ========== RANDOM PLACEMENT TESTS ==========
    @Test
    @DisplayName("Should place all ships randomly")
    void testRandomPlacement() {
        var game = gameService.createGame("Alice", "Bob");

        gameService.randomPlacement(game, Player.P1);

        var board = game.getBoard(Player.P1);
        assertThat(board.getShips()).hasSize(5);
        assertThat(game.getState()).isEqualTo(GameState.PLACING_P2);
    }

    @Test
    @DisplayName("Should clear existing ships on random placement")
    void testRandomPlacementClears() {
        var game = gameService.createGame("Alice", "Bob");
        var req = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);
        gameService.placeShip(game, req);

        gameService.randomPlacement(game, Player.P1);

        var board = game.getBoard(Player.P1);
        assertThat(board.getShips()).hasSize(5);
    }

    @Test
    @DisplayName("Should reject random placement in invalid state")
    void testRandomPlacementInvalidState() {
        var game = gameService.createGame("Alice", "Bob");
        game.setState(GameState.TURN_P1);

        assertThatThrownBy(() -> gameService.randomPlacement(game, Player.P1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not in PLACING_P1");
    }

    @Test
    @DisplayName("Should transition to PLACING_P2 after random placement")
    void testRandomPlacementTransition() {
        var game = gameService.createGame("Alice", "Bob");

        gameService.randomPlacement(game, Player.P1);

        assertThat(game.getState()).isEqualTo(GameState.PLACING_P2);
    }

    // ========== FIRE TESTS ==========
    @Test
    @DisplayName("Should register hit on ship")
    void testFireHit() {
        var game = setupGameForFiring();
        var req = new FireRequest(Player.P1, 0, 0);

        var response = gameService.fire(game, req);

        assertThat(response.hit()).isTrue();
        assertThat(response.sunk()).isFalse();
        assertThat(game.getBoard(Player.P2).getGrid()[0][0]).isEqualTo(CellState.HIT);
    }

    @Test
    @DisplayName("Should register miss")
    void testFireMiss() {
        var game = setupGameForFiring();
        var req = new FireRequest(Player.P1, 5, 5);

        var response = gameService.fire(game, req);

        assertThat(response.hit()).isFalse();
        assertThat(response.sunk()).isFalse();
        assertThat(game.getBoard(Player.P2).getGrid()[5][5]).isEqualTo(CellState.MISS);
    }

    @Test
    @DisplayName("Should reject shot out of bounds")
    void testFireOutOfBounds() {
        var game = setupGameForFiring();
        var req = new FireRequest(Player.P1, 10, 10);

        assertThatThrownBy(() -> gameService.fire(game, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Shot out of bounds");
    }

    // @Disabled
    @Test
    @DisplayName("Should reject already targeted cell")
    void testFireAlreadyTargeted() {
        var game = setupGameForFiring();
        var req1 = new FireRequest(Player.P1, 0, 0);
        var req2 = new FireRequest(Player.P2, 0, 0);

        gameService.fire(game, req1);// P1's turn
        gameService.fire(game, req2);// P2's turn now
        assertThatThrownBy(() -> gameService.fire(game, req1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cell already targeted");
    }

    @Test
    @DisplayName("Should reject fire when not player's turn")
    void testFireWrongTurn() {
        var game = setupGameForFiring();
        var req = new FireRequest(Player.P2, 0, 0);

        assertThatThrownBy(() -> gameService.fire(game, req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Not your turn");
    }

    @Test
    @DisplayName("Should switch turn after fire")
    void testFireSwitchTurn() {
        var game = setupGameForFiring();
        var req = new FireRequest(Player.P1, 5, 5);

        gameService.fire(game, req);

        assertThat(game.getState()).isEqualTo(GameState.TURN_P2);
    }

    @Test
    @DisplayName("fire: detect sunk and return sunk ship type")
    void fireSinksShip() {
        var g = setupGameForFiring();
        var opponentBoard = g.getBoard(Player.P2);
        var ship = opponentBoard.getShips().get(0);
        var coords = ship.getCells();

        // hit all but last cell (interleave opponent shots to keep turn mechanics)
        for (int i = 0; i < coords.size() - 1; i++) {
            if (g.getState() == GameState.TURN_P1) {
                gameService.fire(g, new FireRequest(Player.P1, coords.get(i).row(), coords.get(i).col()));
            }

            if (g.getState() == GameState.TURN_P2) {
                // opponent fires somewhere safe
                gameService.fire(g, new FireRequest(Player.P2, 9, coords.get(i).col()));
            }
        }
        System.out.println("State before final blow: " + g.getState());

        // final blow
        var last = coords.get(coords.size() - 1);
        FireResponse resp = null;

        if (g.getState() == GameState.TURN_P2) {
            gameService.fire(g, new FireRequest(Player.P2, 9, 9));
        }
        if (g.getState() == GameState.TURN_P1) {
            resp = gameService.fire(g, new FireRequest(Player.P1, last.row(), last.col()));
        }

        assertThat(resp.sunk()).isTrue();
        assertThat(resp.sunkShipType()).isNotNull();
    }

    @Test
    @DisplayName("Should set winner when all opponent ships sunk")
    void testFireGameOver() {
        var game = setupGameForFiring();

        sinkAllShips(game, Player.P1);

        assertThat(game.getState()).isEqualTo(GameState.FINISHED);
        assertThat(game.getWinner()).isEqualTo(Player.P1);
    }

    // ========== VIEW TESTS ==========
    @Test
    @DisplayName("Should return game view with fog of war")
    void testViewWithFogOfWar() {
        var game = setupGameForFiring();

        var viewP1 = gameService.view(game, Player.P1);

        assertThat(viewP1).isNotNull();
        assertThat(viewP1.gameId()).isEqualTo(game.getId());
        assertThat(viewP1.state()).isEqualTo(GameState.TURN_P1);
        assertThat(viewP1.yourBoard()).isNotNull();
        assertThat(viewP1.opponentBoardMasked()).isNotNull();
    }

    @Test
    @DisplayName("Should mask opponent board for P1")
    void testViewMasksOpponent() {
        var game = setupGameForFiring();

        var viewP1 = gameService.view(game, Player.P1);

        assertThat(viewP1.yourBoard()[0][0]).isEqualTo(CellState.SHIP);
        assertThat(viewP1.opponentBoardMasked()[0][0]).isNotEqualTo(CellState.SHIP);
    }

    @Test
    @DisplayName("Should return player names in view")
    void testViewPlayerNames() {
        var game = gameService.createGame("Alice", "Bob");
        placeAllShipsForPlayer(game, Player.P1);
        game.setState(GameState.PLACING_P2);
        placeAllShipsForPlayer(game, Player.P2);

        var view = gameService.view(game, Player.P1);

        assertThat(view.p1Name()).isEqualTo("Alice");
        assertThat(view.p2Name()).isEqualTo("Bob");
    }

    // ========== HELPER METHODS ==========
    private void placeAllShipsForPlayer(Game game, Player player) {
        int row = 0;
        for (var shipType : ShipType.values()) {
            var req = new PlaceShipRequest(player, shipType, row, 0, Orientation.HORIZONTAL);
            gameService.placeShip(game, req);
            row += 2;
        }
    }

    private Game setupGameForFiring() {
        var game = gameService.createGame("Alice", "Bob");
        placeAllShipsForPlayer(game, Player.P1);
        placeAllShipsForPlayer(game, Player.P2);
        return game;
    }

    private void sinkAllShips(Game game, Player attacker) {
        var defender = attacker == Player.P1 ? Player.P2 : Player.P1;
        var board = game.getBoard(defender);

        for (var ship : board.getShips()) {
            for (var cell : ship.getCells()) {
                var req = new FireRequest(attacker, cell.row(), cell.col());
                gameService.fire(game, req);
                if (game.getState() == GameState.TURN_P1 && attacker == Player.P2) {
                    gameService.fire(game, new FireRequest(Player.P1, 9 - cell.row(), cell.col()));
                } else if (game.getState() == GameState.TURN_P2 && attacker == Player.P1) {
                    gameService.fire(game, new FireRequest(Player.P2, 9 - cell.row(), cell.col()));
                }
            }
        }
    }
}
