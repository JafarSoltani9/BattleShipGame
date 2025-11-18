// src/context/GameContext.jsx
import React, { createContext, useContext, useState } from "react";
import { VIEWERS } from "../gameConstants";

const GameContext = createContext(undefined);

export function GameProvider({ children }) {
  const [gameId, setGameId] = useState(null);
  const [viewer, setViewer] = useState(VIEWERS.P1);

  const value = {
    gameId,
    viewer,
    setGameId,
    setViewer,
  };

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
}
/* eslint-disable react-refresh/only-export-components */

export function useGameCtx() {
  const ctx = useContext(GameContext);
  if (!ctx) {
    throw new Error("useGameCtx must be used within GameProvider");
  }
  return ctx;
}
