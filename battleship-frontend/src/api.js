import { VIEWERS } from "./gameConstants";

const API_ROOT = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

async function handleJson(res) {
  const text = await res.text(); // read body once

  if (!res.ok) {
    throw new Error(text || `HTTP ${res.status}`);
  }

  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    throw new Error("Invalid JSON from server");
  }
}

export async function createGame(p1Name, p2Name) {
  const params = new URLSearchParams();
  if (p1Name) params.append("p1", p1Name);
  if (p2Name) params.append("p2", p2Name);

  const res = await fetch(`${API_ROOT}/api/game?${params.toString()}`, {
    method: "POST",
  });

  // expect CreateGameResponse
  const data = await handleJson(res);

  if (!data) {
    throw new Error("Empty response from createGame");
  }

  return data.id ?? data.gameId ?? data.gameID;
}

// get game by id
export async function getGame(gameId, viewer = VIEWERS.P1) {
  const res = await fetch(
    `${API_ROOT}/api/game/${encodeURIComponent(gameId)}?viewer=${viewer}`,
    { method: "GET" }
  );
  return await handleJson(res);
}

// backend return 200 and return 400 if already placed
export async function randomPlacement(gameId, player) {
  const res = await fetch(
    `${API_ROOT}/api/game/${encodeURIComponent(gameId)}/random-placement`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ player }),
    }
  );

  if (!res.ok) {
    if (res.status === 400) {
      throw new Error("You have already placed all ships for this player.");
    }
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
}

// random-place 2 players
export async function quickStart(gameId) {
  await randomPlacement(gameId, "P1");
  await randomPlacement(gameId, "P2");
}

// ✅ FIXED: use API_ROOT and handleJson
export async function placeShip(
  gameId,
  { player, shipType, row, col, orientation }
) {
  const res = await fetch(
    `${API_ROOT}/api/game/${encodeURIComponent(gameId)}/place-ship`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        player, // "P1" or "P2"
        shipType,
        row,
        col,
        orientation, // "HORIZONTAL" or "VERTICAL"
      }),
    }
  );

  return await handleJson(res); // backend returns 200 with empty body → null
}

// backend returns FireResponse JSON.
export async function fire(gameId, { player, row, col }) {
  const res = await fetch(
    `${API_ROOT}/api/game/${encodeURIComponent(gameId)}/fire`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ player, row, col }),
    }
  );
  return await handleJson(res);
}
