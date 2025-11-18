
import React, { useState } from "react";
import { Board } from "../components/Board";
import { randomPlacement, placeShip } from "../api";
import { SHIP_TYPES } from "../gameConstants";
import { useGameCtx } from "../context/GameContext";

const SHIPS = [
  { type: SHIP_TYPES.CARRIER, label: "Carrier (5)" },
  { type: SHIP_TYPES.BATTLESHIP, label: "Battleship (4)" },
  { type: SHIP_TYPES.CRUISER, label: "Cruiser (3)" },
  { type: SHIP_TYPES.SUBMARINE, label: "Submarine (3)" },
  { type: SHIP_TYPES.DESTROYER, label: "Destroyer (2)" },
];

export default function Placement({ view, refresh }) {
  const { gameId, viewer } = useGameCtx();
  const [selectedShip, setSelectedShip] = useState(SHIP_TYPES.CARRIER);
  const [orientation, setOrientation] = useState("H");
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState(null);

  if (!gameId) return null;

  const currentPlayerName = viewer === "P1" ? view.p1Name : view.p2Name;

  async function handleRandom() {
    setBusy(true);
    setMessage(null);
    try {
      await randomPlacement(gameId, viewer);
      setMessage("Fleet randomized!");
      await refresh();
    } catch (err) {
      setMessage(err.message || "Random placement failed");
    } finally {
      setBusy(false);
    }
  }

  async function handleCellClick(row, col) {
    setBusy(true);
    setMessage(null);
    try {
      await placeShip(gameId, {
        player: viewer,
        shipType: selectedShip,
        row,
        col,
        orientation,
      });
      setMessage(`Placed ${selectedShip} at row ${row + 1}, col ${col + 1} (${orientation})`);
      await refresh();
    } catch (err) {
      setMessage(err.message || "Invalid placement");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="container py-4 text-light">
      <div className="text-center mb-3">
        <h5>Ship placement – {currentPlayerName}</h5>
      </div>

      <div className="row g-3">
        <div className="col-md-4">
          <div className="card bg-secondary text-light">
            <div className="card-body">
              <h6 className="card-title">Manual placement</h6>

              <div className="mb-2">
                <label className="form-label">Ship</label>
                <select
                  className="form-select form-select-sm"
                  value={selectedShip}
                  onChange={(e) => setSelectedShip(e.target.value)}
                >
                  {SHIPS.map((s) => (
                    <option key={s.type} value={s.type}>
                      {s.label}
                    </option>
                  ))}
                </select>
              </div>

              <div className="mb-2">
                <label className="form-label">Orientation</label>
                <div className="btn-group w-100">
                  <button
                    type="button"
                    className={
                      "btn btn-sm " +
                      (orientation === "H" ? "btn-primary" : "btn-outline-light")
                    }
                    onClick={() => setOrientation("H")}
                  >
                    Horizontal
                  </button>
                  <button
                    type="button"
                    className={
                      "btn btn-sm " +
                      (orientation === "V" ? "btn-primary" : "btn-outline-light")
                    }
                    onClick={() => setOrientation("V")}
                  >
                    Vertical
                  </button>
                </div>
              </div>

              <small className="text-light">
                Click on your board to place the selected ship.
              </small>

              <button
                type="button"
                className="btn btn-success btn-sm w-100 mt-3"
                onClick={handleRandom}
                disabled={busy}
              >
                Randomize Fleet
              </button>

              {message && <div className="mt-2 small">{message}</div>}
            </div>
          </div>
        </div>

        <div className="col-md-8 d-flex justify-content-center">
          <Board
            title="Your Fleet"
            board={view.yourBoard}
            clickable
            disabled={busy}
            showShips
            onCellClick={handleCellClick}
          />
        </div>
      </div>
    </div>
  );
}
