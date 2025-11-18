package com.example.battleship;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PlacementRuleTest {

	@Test
	void shipsCantTouchDiagonally() {
		var svc = new GameService(new GameStore());
		var g = svc.createGame("A","B");

		// P1 phase
		svc.placeShip(g, new PlaceShipRequest(Player.P1, ShipType.DESTROYER, 0,0, Orientation.HORIZONTAL));
		// this placement would diagonally touch (1,2) near (0,1)
		assertThrows(Exception.class, () ->
				svc.placeShip(g, new PlaceShipRequest(Player.P1, ShipType.DESTROYER, 1,2, Orientation.VERTICAL))
		);
	}
}
