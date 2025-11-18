// src/views/GameOver.jsx
import React from "react";

export default function GameOver({ view, onPlayAgain }) {
  const winnerName =
    view.winner === "P1"
      ? view.p1Name
      : view.winner === "P2"
      ? view.p2Name
      : "Unknown";

  return (
    <div className="container py-5 text-light">
      <div className="row justify-content-center">
        <div className="col-md-5">
          <div className="card bg-secondary text-light">
            <div className="card-body text-center">
              <h3 className="card-title mb-3">Game Over</h3>
              <p className="mb-1">
                Winner: <strong>{winnerName}</strong>
              </p>
              {typeof view.turns === "number" && (
                <p className="small mb-1">Turns: {view.turns}</p>
              )}
              {typeof view.accuracy === "number" && (
                <p className="small mb-1">
                  Accuracy: {(view.accuracy * 100).toFixed(1)}%
                </p>
              )}
              <button
                type="button"
                className="btn btn-primary mt-3 w-100"
                onClick={onPlayAgain}
              >
                Play Again
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
