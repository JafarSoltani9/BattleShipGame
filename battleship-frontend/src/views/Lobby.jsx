import React, { useState } from "react";
import { createGame } from "../api";

export default function Lobby({ onGameCreated }) {
  const [p1Name, setP1Name] = useState("");
  const [p2Name, setP2Name] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError(null);

    const p1 = p1Name.trim();
    const p2 = p2Name.trim();

    //  frontend validation
    if (!p1 || !p2) {
      setError("Both player names are required.");
      return;
    }

    setLoading(true);
    try {
      const id = await createGame(p1, p2);
      onGameCreated(id);
    } catch (err) {
      // In case backend also rejects
      setError(err.message || "Failed to create game");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="d-flex align-items-center justify-content-center vh-100 bg-dark text-light">
      <div className="card bg-secondary text-light" style={{ maxWidth: "400px", width: "100%" }}>
        <div className="card-body">
          <h3 className="card-title text-center mb-3">Battleship</h3>
          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label htmlFor="p1" className="form-label">
                Player 1 name
              </label>
              <input
                id="p1"
                className="form-control"
                value={p1Name}
                onChange={(e) => setP1Name(e.target.value)}
                placeholder="Enter a name"
                required                    
              />
            </div>
            <div className="mb-3">
              <label htmlFor="p2" className="form-label">
                Player 2 name
              </label>
              <input
                id="p2"
                className="form-control"
                value={p2Name}
                onChange={(e) => setP2Name(e.target.value)}
                placeholder="Enter a name"
                required                   
              />
            </div>
            {error && <div className="alert alert-danger py-1 small">{error}</div>}
            <button
              type="submit"
              className="btn btn-primary w-100"
              disabled={loading}
            >
              {loading ? "Creating..." : "New Game"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
