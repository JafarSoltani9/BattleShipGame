// src/views/Play.jsx
import React, { useEffect, useState } from "react";
import { Board } from "../components/Board";
import Scoreboard from "../components/Scoreboard";
import { fire, getGame } from "../api";
import { useGameCtx } from "../context/GameContext";
import { GAME_STATES, CELL_STATES } from "../gameConstants";

export default function Play({ view, refresh }) {
  const { gameId, setGameId, setViewer } = useGameCtx();
  const [busy, setBusy] = useState(false);

  // Both perspectives
  const [p1View, setP1View] = useState(null); // P1's view
  const [p2View, setP2View] = useState(null); // P2's view

  // Sunk ships recorded by each shooter
  const [sunkByP1, setSunkByP1] = useState([]); // ships sunk by P1
  const [sunkByP2, setSunkByP2] = useState([]); // ships sunk by P2

  // Toggle: show own fleet vs opponent grid
  const [showFleetP1, setShowFleetP1] = useState(false);
  const [showFleetP2, setShowFleetP2] = useState(false);

  useEffect(() => {
    let alive = true;
    (async () => {
      if (!gameId) return;
      try {
        const [v1, v2] = await Promise.all([
          getGame(gameId, "P1"),
          getGame(gameId, "P2"),
        ]);
        if (alive) {
          setP1View(v1);
          setP2View(v2);
        }
      } catch {
        // silent
      }
    })();
    return () => {
      alive = false;
    };
  }, [gameId, view?.state]); // refresh on state change

  if (!gameId) return null;

  const isYourTurnP1 = view.state === GAME_STATES.TURN_P1;
  const isYourTurnP2 = view.state === GAME_STATES.TURN_P2;
  const inTurn = isYourTurnP1 || isYourTurnP2;

  function countHitsMisses(boardMasked) {
    let hits = 0,
      misses = 0;
    if (!boardMasked) return { hits, misses };
    for (const row of boardMasked) {
      for (const cell of row) {
        if (cell === CELL_STATES.HIT || cell === CELL_STATES.SUNK) hits++;
        else if (cell === CELL_STATES.MISS) misses++;
      }
    }
    return { hits, misses };
  }

  // compute stats from masked boards (shots fired)
  const p1Stats = countHitsMisses(p1View?.opponentBoardMasked);
  const p2Stats = countHitsMisses(p2View?.opponentBoardMasked);

  async function handleFireFor(player, row, col) {
    const allowed =
      (player === "P1" && isYourTurnP1) ||
      (player === "P2" && isYourTurnP2);
    if (!allowed || busy) return;

    setBusy(true);
    try {
      const result = await fire(gameId, { player, row, col });

      // record sunk ship by shooter
      if (result?.sunk && result?.sunkShipType) {
        if (player === "P1") {
          setSunkByP1((prev) => [...prev, result.sunkShipType]);
        } else {
          setSunkByP2((prev) => [...prev, result.sunkShipType]);
        }
      }

      // refresh parent + both perspectives
      await refresh?.();
      const [v1, v2] = await Promise.all([
        getGame(gameId, "P1"),
        getGame(gameId, "P2"),
      ]);
      setP1View(v1);
      setP2View(v2);
    } catch {
      // silent
    } finally {
      setBusy(false);
    }
  }

  // Go back to Lobby
  function goToStart() {
    setViewer("P1");
    setGameId(null);
  }

  // Decide which board & title to show for each player slot
  const p1BoardToShow = showFleetP1
    ? p1View?.yourBoard
    : p1View?.opponentBoardMasked;

  const p1Title = showFleetP1
    ? `${p1View?.p1Name || "Player 1"} — my fleet`
    : `${p1View?.p2Name || "Opponent"} (for ${p1View?.p1Name || "P1"})`;

  const p2BoardToShow = showFleetP2
    ? p2View?.yourBoard
    : p2View?.opponentBoardMasked;

  const p2Title = showFleetP2
    ? `${p2View?.p2Name || "Player 2"} — my fleet`
    : `${p2View?.p1Name || "Opponent"} (for ${p2View?.p2Name || "P2"})`;

  return (
    <div className="container py-3 text-light">
      <div className="text-center mb-3">
        {view.state === GAME_STATES.FINISHED ? (
          <h5>Game over</h5>
        ) : isYourTurnP1 ? (
          <h5>{p1View?.p1Name || "Player 1"}'s turn</h5>
        ) : isYourTurnP2 ? (
          <h5>{p2View?.p2Name || "Player 2"}'s turn</h5>
        ) : (
          <h6>Waiting for the next move…</h6>
        )}
      </div>

      {inTurn ? (
        <div className="row g-4 justify-content-center">
          {/* LEFT SLOT: P1 side */}
          <div className="col-md-5">
            <Board
              title={p1Title}
              board={p1BoardToShow}
              showShips={showFleetP1}
              clickable={!showFleetP1 && isYourTurnP1} // can only click when viewing opponent
              disabled={!isYourTurnP1 || busy || showFleetP1}
              onCellClick={(r, c) => handleFireFor("P1", r, c)}
            />
            <Scoreboard
              title={`Scoreboard — ${p1View?.p1Name || "Player 1"}`}
              sunkShips={sunkByP1}
              hits={p1Stats.hits}
              misses={p1Stats.misses}
            />
            <div className="text-center mt-2">
              <button
                type="button"
                className="btn btn-sm btn-outline-light"
                onClick={() => setShowFleetP1((prev) => !prev)}
              >
                {showFleetP1 ? "Show opponent grid" : "Show my ships"}
              </button>
            </div>
          </div>

          {/* RIGHT SLOT: P2 side */}
          <div className="col-md-5">
            <Board
              title={p2Title}
              board={p2BoardToShow}
              showShips={showFleetP2}
              clickable={!showFleetP2 && isYourTurnP2}
              disabled={!isYourTurnP2 || busy || showFleetP2}
              onCellClick={(r, c) => handleFireFor("P2", r, c)}
            />
            <Scoreboard
              title={`Scoreboard — ${p2View?.p2Name || "Player 2"}`}
              sunkShips={sunkByP2}
              hits={p2Stats.hits}
              misses={p2Stats.misses}
            />
            <div className="text-center mt-2">
              <button
                type="button"
                className="btn btn-sm btn-outline-light"
                onClick={() => setShowFleetP2((prev) => !prev)}
              >
                {showFleetP2 ? "Show opponent grid" : "Show my ships"}
              </button>
            </div>
          </div>
        </div>
      ) : (
        <div className="text-center text-muted">Placement phase…</div>
      )}

      <div className="text-center my-4">
        <button
          type="button"
          className="btn btn-outline-light"
          onClick={goToStart}
        >
          Back to Start
        </button>
      </div>
    </div>
  );
}
