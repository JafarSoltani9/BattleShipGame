import React from "react";
import { CELL_STATES } from "../gameConstants";

const COL_LABELS = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];

function cellClass(cell, showShips, clickable, disabled) {
  let base =
    "btn btn-sm p-0 border d-flex align-items-center justify-content-center";
  if (disabled) base += " disabled";

  // 👇 When we are showing *our own fleet*
  if (showShips) {
    // Show ship cells (including hit/sunk ship cells) as filled
    if (
      cell === CELL_STATES.SHIP ||
      cell === CELL_STATES.HIT ||
      cell === CELL_STATES.SUNK
    ) {
      return base + " btn-secondary";
    }

    // A miss on our own board can have a different outline if you want
    if (cell === CELL_STATES.MISS) {
      return base + " btn-outline-danger";
    }

    // Everything else = empty water
    return base + " btn-outline-secondary";
  }

  // 👇 When we are looking at the opponent board (normal play view)
  // Ships are hidden → look like empty water
  if (cell === CELL_STATES.EMPTY || cell === CELL_STATES.SHIP) {
    return base + " btn-outline-secondary";
  }

  // Shots: color hits and misses
  if (cell === CELL_STATES.HIT || cell === CELL_STATES.SUNK) {
    return base + " btn-success fw-bold text-white"; // green for hit/sunk
  }
  if (cell === CELL_STATES.MISS) {
    return base + " btn-danger fw-bold text-white"; // red for miss
  }

  return base;
}

export function Board({
  title,
  board,
  clickable = false,
  disabled = false,
  showShips = false,
  onCellClick,
}) {
  const effectiveBoard =
    board && board.length
      ? board
      : Array.from({ length: 10 }, () =>
          Array.from({ length: 10 }, () => CELL_STATES.EMPTY)
        );

  return (
    <div className="text-center">
      <h6 className="mb-2">{title}</h6>
      <table className="table table-borderless mb-0">
        <thead>
          <tr>
            <th style={{ width: "20px" }}></th>
            {COL_LABELS.map((label) => (
              <th key={label} className="text-center small">
                {label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {effectiveBoard.map((row, r) => (
            <tr key={r}>
              <td className="text-end align-middle small">{r + 1}</td>
              {row.map((cell, c) => (
                <td key={c} className="p-1 text-center">
                  <button
                    type="button"
                    disabled={disabled || !clickable}
                    className={cellClass(cell, showShips, clickable, disabled)}
                    style={{ width: "32px", height: "32px" }}
                    onClick={() => {
                      if (!disabled && clickable && onCellClick)
                        onCellClick(r, c);
                    }}
                    aria-label={`cell-${r}-${c}`}
                    title={`${COL_LABELS[c]}${r + 1}`}
                  >
                    {/* Opponent view: show ✓ / ✕ for shots */}
                    {!showShips &&
                      (cell === CELL_STATES.HIT ||
                        cell === CELL_STATES.SUNK) &&
                      "✓"}
                    {!showShips && cell === CELL_STATES.MISS && "✕"}
                    {/* Own fleet view: no symbols needed, ships are shown by color */}
                  </button>
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default Board;
