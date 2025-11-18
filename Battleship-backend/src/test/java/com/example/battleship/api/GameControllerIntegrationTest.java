package com.example.battleship.api;

import com.example.battleship.domain.*;
import com.example.battleship.dto.*;
import com.example.battleship.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("GameController Integration Tests")
class GameControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameService gameService;

    private String gameId;

    @BeforeEach
    void setUp() throws Exception {
        var game = gameService.createGame("Alice", "Bob");
        gameId = game.getId();
    }

    // ========== CREATE GAME TESTS ==========
    @Test
    @DisplayName("POST /api/game - Should create game with both player names")
    void testCreateGameWithBothNames() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game")
                        .param("p1", "John")
                        .param("p2", "Jane")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isString())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        CreateGameResponse gameResponse = objectMapper.readValue(response, CreateGameResponse.class);
        assertThat(gameResponse.gameId()).isNotBlank();
    }

    @Test
    @DisplayName("POST /api/game - Should create game with blank P2 name")
    void testCreateGameWithP1NameOnly() throws Exception {
        mockMvc.perform(post("/api/game")
                        .param("p1", "Alice")
                        .param("p2", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.gameId").isString());
    }

    @Test
    @DisplayName("POST /api/game - Should create game with blank P1 name")
    void testCreateGameWithP2NameOnly() throws Exception {
        mockMvc.perform(post("/api/game")
                        .param("p1", "")
                        .param("p2", "Bob")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isString());
    }

    @Test
    @DisplayName("POST /api/game - Should create game with blank names")
    void testCreateGameWithNoNames() throws Exception {
        mockMvc.perform(post("/api/game")
                        .param("p1", "")
                        .param("p2", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").isString());
    }

    // ========== VIEW GAME TESTS ==========
    @Test
    @DisplayName("GET /api/game/{id}?viewer=P1 - Should return game view for P1")
    void testViewGameAsP1() throws Exception {
        placeAllShipsForPlayer(gameId, Player.P1);
        placeAllShipsForPlayer(gameId, Player.P2);

        mockMvc.perform(get("/api/game/{id}", gameId)
                        .param("viewer", "P1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.state").value("TURN_P1"))
                .andExpect(jsonPath("$.p1Name").value("Alice"))
                .andExpect(jsonPath("$.p2Name").value("Bob"))
                .andExpect(jsonPath("$.yourBoard").isArray())
                .andExpect(jsonPath("$.opponentBoardMasked").isArray())
                .andExpect(jsonPath("$.winner").isEmpty());
    }

    @Test
    @DisplayName("GET /api/game/{id}?viewer=P2 - Should return game view for P2")
    void testViewGameAsP2() throws Exception {
        placeAllShipsForPlayer(gameId, Player.P1);
        placeAllShipsForPlayer(gameId, Player.P2);

        mockMvc.perform(get("/api/game/{id}", gameId)
                        .param("viewer", "P2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.state").value("TURN_P1"));
    }

    @Test
    @DisplayName("GET /api/game/{id} - Should return 404 for non-existent game")
    void testViewGameNotFound() throws Exception {
        mockMvc.perform(get("/api/game/nonexistent-id")
                        .param("viewer", "P1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/game/{id} - Should return 400 when viewer parameter missing")
    void testViewGameMissingViewer() throws Exception {
        mockMvc.perform(get("/api/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== PLACE SHIP TESTS ==========
    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should place ship successfully")
    void testPlaceShipSuccess() throws Exception {
        PlaceShipRequest req = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);

        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should place ship vertically")
    void testPlaceShipVertical() throws Exception {
        PlaceShipRequest req = new PlaceShipRequest(Player.P1, ShipType.BATTLESHIP, 0, 0, Orientation.VERTICAL);

        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should return 404 for non-existent game")
    void testPlaceShipGameNotFound() throws Exception {
        PlaceShipRequest req = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);

        mockMvc.perform(post("/api/game/nonexistent/place-ship")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should reject placement out of bounds")
    void testPlaceShipOutOfBounds() throws Exception {
        PlaceShipRequest req = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 7, Orientation.HORIZONTAL);

        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should reject overlapping placement")
    void testPlaceShipOverlap() throws Exception {
        PlaceShipRequest req1 = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);
        PlaceShipRequest req2 = new PlaceShipRequest(Player.P1, ShipType.CRUISER, 0, 2, Orientation.HORIZONTAL);

        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should reject invalid request body")
    void testPlaceShipInvalidBody() throws Exception {
        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/game/{id}/place-ship - Should reject placement in wrong game state")
    void testPlaceShipWrongState() throws Exception {
        placeAllShipsForPlayer(gameId, Player.P1);
        placeAllShipsForPlayer(gameId, Player.P2);

        PlaceShipRequest req = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);

        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    // ========== RANDOM PLACEMENT TESTS ==========
    @Test
    @DisplayName("POST /api/game/{id}/random-placement - Should place ships randomly for P1")
    void testRandomPlacementP1() throws Exception {
        RandomPlacementRequest req = new RandomPlacementRequest(Player.P1);

        mockMvc.perform(post("/api/game/{id}/random-placement", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        var view = gameService.view(gameService.getGameOr404(gameId), Player.P1);
        assertThat(view.state()).isEqualTo(GameState.PLACING_P2);
    }

    @Test
    @DisplayName("POST /api/game/{id}/random-placement - Should place ships randomly for P2")
    void testRandomPlacementP2() throws Exception {
        placeAllShipsForPlayer(gameId, Player.P1);

        RandomPlacementRequest req = new RandomPlacementRequest(Player.P2);

        mockMvc.perform(post("/api/game/{id}/random-placement", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        var view = gameService.view(gameService.getGameOr404(gameId), Player.P2);
        assertThat(view.state()).isEqualTo(GameState.TURN_P1);
    }

    @Test
    @DisplayName("POST /api/game/{id}/random-placement - Should return 404 for non-existent game")
    void testRandomPlacementGameNotFound() throws Exception {
        RandomPlacementRequest req = new RandomPlacementRequest(Player.P1);

        mockMvc.perform(post("/api/game/nonexistent/random-placement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/game/{id}/random-placement - Should reject invalid request body")
    void testRandomPlacementInvalidBody() throws Exception {
        mockMvc.perform(post("/api/game/{id}/random-placement", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/game/{id}/random-placement - Should clear existing ships")
    void testRandomPlacementClears() throws Exception {
        PlaceShipRequest shipReq = new PlaceShipRequest(Player.P1, ShipType.CARRIER, 0, 0, Orientation.HORIZONTAL);
        mockMvc.perform(post("/api/game/{id}/place-ship", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipReq)))
                .andExpect(status().isOk());

        RandomPlacementRequest req = new RandomPlacementRequest(Player.P1);
        mockMvc.perform(post("/api/game/{id}/random-placement", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        var view = gameService.view(gameService.getGameOr404(gameId), Player.P1);
        assertThat(view.state()).isEqualTo(GameState.PLACING_P2);
    }

    // ========== FIRE TESTS ==========
    @Test
    @DisplayName("POST /api/game/{id}/fire - Should fire and register hit")
    void testFireHit() throws Exception {
        setupGameForFiring();

        FireRequest req = new FireRequest(Player.P1, 0, 0);

        MvcResult result = mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        FireResponse fireResponse = objectMapper.readValue(response, FireResponse.class);
        assertThat(fireResponse.hit()).isTrue();
        assertThat(fireResponse.sunk()).isFalse();
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should fire and register miss")
    void testFireMiss() throws Exception {
        setupGameForFiring();

        FireRequest req = new FireRequest(Player.P1, 5, 5);

        MvcResult result = mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        FireResponse fireResponse = objectMapper.readValue(response, FireResponse.class);
        assertThat(fireResponse.hit()).isFalse();
        assertThat(fireResponse.sunk()).isFalse();
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should return 404 for non-existent game")
    void testFireGameNotFound() throws Exception {
        FireRequest req = new FireRequest(Player.P1, 0, 0);

        mockMvc.perform(post("/api/game/nonexistent/fire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should reject shot out of bounds")
    void testFireOutOfBounds() throws Exception {
        setupGameForFiring();

        FireRequest req = new FireRequest(Player.P1, 10, 10);

        mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should reject already targeted cell")
    void testFireAlreadyTargeted() throws Exception {
        setupGameForFiring();

        FireRequest req = new FireRequest(Player.P1, 5, 5);

        mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should reject wrong player's turn")
    void testFireWrongTurn() throws Exception {
        setupGameForFiring();

        FireRequest req = new FireRequest(Player.P2, 0, 0);

        mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should detect sunk ship")
    void testFireSunk() throws Exception {
        setupGameForFiring();
        var game = gameService.getGameOr404(gameId);
        var board = game.getBoard(Player.P2);
        var ship = board.getShips().get(0);
        var cells = ship.getCells();

        for (var cell : cells.subList(0, cells.size() - 1)) {
            if (game.getState() == GameState.TURN_P1) {
                FireRequest req = new FireRequest(Player.P1, cell.row(), cell.col());
                mockMvc.perform(post("/api/game/{id}/fire", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isOk());
            }
            if (game.getState() == GameState.TURN_P2) {
                FireRequest missReq = new FireRequest(Player.P2, 9 - cell.row(), cell.col());
                mockMvc.perform(post("/api/game/{id}/fire", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(missReq)))
                        .andExpect(status().isOk());
            }
        }
        if (game.getState() == GameState.TURN_P2) {
            FireRequest missReq = new FireRequest(Player.P2, 9 , 9);
            mockMvc.perform(post("/api/game/{id}/fire", gameId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(missReq)))
                    .andExpect(status().isOk());
        }

        FireRequest finalReq = new FireRequest(Player.P1, cells.get(cells.size() - 1).row(), cells.get(cells.size() - 1).col());
        MvcResult result = mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(finalReq)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        FireResponse fireResponse = objectMapper.readValue(response, FireResponse.class);
        assertThat(fireResponse.sunk()).isTrue();
    }

    @Test
    @DisplayName("POST /api/game/{id}/fire - Should reject invalid request body")
    void testFireInvalidBody() throws Exception {
        setupGameForFiring();

        mockMvc.perform(post("/api/game/{id}/fire", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ========== HELPER METHODS ==========
    private void placeAllShipsForPlayer(String id, Player player) throws Exception {
        int row = 0;
        for (var shipType : ShipType.values()) {
            PlaceShipRequest req = new PlaceShipRequest(player, shipType, row, 0, Orientation.HORIZONTAL);
            mockMvc.perform(post("/api/game/{id}/place-ship", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
            row += 2;
        }
    }

    private void setupGameForFiring() throws Exception {
        placeAllShipsForPlayer(gameId, Player.P1);
        placeAllShipsForPlayer(gameId, Player.P2);
    }

    private void sinkAllShips() throws Exception {
        var game = gameService.getGameOr404(gameId);
        var board = game.getBoard(Player.P2);

        for (var ship : board.getShips()) {
            for (var cell : ship.getCells()) {
                FireRequest req = new FireRequest(Player.P1, cell.row(), cell.col());
                mockMvc.perform(post("/api/game/{id}/fire", gameId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isOk());

                game = gameService.getGameOr404(gameId);
                if (game.getState() == GameState.TURN_P1 && game.getWinner() == null) {
                    FireRequest missReq = new FireRequest(Player.P2, 9 - cell.row(), cell.col());
                    mockMvc.perform(post("/api/game/{id}/fire", gameId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(missReq)))
                            .andExpect(status().isOk());
                }
            }
        }
    }
}
