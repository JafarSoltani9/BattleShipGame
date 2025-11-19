import React, { useEffect, useState } from "react";
import { GameProvider, useGameCtx } from "./context/GameContext";
import Lobby from "./views/Lobby";
import Placement from "./views/Placement";
import Play from "./views/Play";
import GameOver from "./views/GameOver";
import { getGame } from "./api";
import { GAME_STATES, VIEWERS } from "./gameConstants";

function Shell() {
  const { gameId, viewer, setGameId, setViewer } = useGameCtx();
  const [view, setView] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Fetch current view for the selected viewer
  useEffect(() => {
    let alive = true;

    (async () => {
      if (!gameId) return;
      setLoading(true);
      setError(null);
      try {
        const gv = await getGame(gameId, viewer);
        if (alive) setView(gv);
      } catch (err) {
        if (alive) setError(err?.message || "Failed to load game");
      } finally {
        if (alive) setLoading(false);
      }
    })();

    return () => {
      alive = false;
    };
  }, [gameId, viewer]);

  // 👇 NEW: automatically choose viewer based on game state in placement
  useEffect(() => {
    if (!view) return;

    if (view.state === GAME_STATES.PLACING_P1 && viewer !== VIEWERS.P1) {
      setViewer(VIEWERS.P1);
    } else if (
      view.state === GAME_STATES.PLACING_P2 &&
      viewer !== VIEWERS.P2
    ) {
      setViewer(VIEWERS.P2);
    }
  }, [view?.state, viewer, setViewer]);

  const fetchNow = async () => {
    if (!gameId) return;
    setLoading(true);
    setError(null);
    try {
      const gv = await getGame(gameId, viewer);
      setView(gv);
    } catch (err) {
      setError(err?.message || "Failed to load game");
    } finally {
      setLoading(false);
    }
  };

  function handlePlayAgain() {
    setGameId(null);
    setView(null);
  }

  let mainContent = null;
  const state = view?.state;

  if (!gameId) {
    mainContent = (
      <Lobby
        onGameCreated={(id) => {
          setGameId(id);
        }}
      />
    );
  } else if (!view) {
    mainContent = (
      <div className="d-flex justify-content-center mt-4">
        <span>Loading game…</span>
      </div>
    );
  } else if (
    state === GAME_STATES.PLACING_P1 ||
    state === GAME_STATES.PLACING_P2
  ) {
    mainContent = <Placement view={view} refresh={fetchNow} />;
  } else if (state === GAME_STATES.TURN_P1 || state === GAME_STATES.TURN_P2) {
    mainContent = <Play view={view} refresh={fetchNow} />;
  } else if (state === GAME_STATES.FINISHED) {
    mainContent = <GameOver view={view} onPlayAgain={handlePlayAgain} />;
  }

  return (
    <div className="bg-slate-grey text-light min-vh-100 d-flex flex-column">
      <header className="border-bottom border-secondary py-2 px-3">
        <div className="fw-semibold text-center fs-4">Battleship</div>

        <div className="d-flex justify-content-center align-items-center gap-3 flex-wrap mt-2">
          <span className="small">
            Game ID:&nbsp;<code className="text-danger">{gameId ?? "—"}</code>
          </span>
          {/* No manual switch button needed anymore */}
        </div>
      </header>

      <main className="flex-grow-1">
        {loading && gameId && (
          <div className="text-center small text-muted mt-2">Loading...</div>
        )}
        {error && (
          <div className="text-center small text-danger mt-2" role="alert">
            {error}
          </div>
        )}
        {mainContent}
      </main>
    </div>
  );
}

export default function App() {
  return (
    <GameProvider>
      <Shell />
    </GameProvider>
  );
}
