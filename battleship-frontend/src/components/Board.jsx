import React from "react";
import { CELL_STATES } from "../gameConstants";

const COL_LABELS = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J"];

function cellClass(cell, showShips, clickable, disabled) {
  let base =
    "btn btn-sm p-0 border d-flex align-items-center justify-content-center";
  if (disabled) base += " disabled";

  if (cell === CELL_STATES.EMPTY) return base + " btn-outline-secondary";

  // Ships shown only on your own board
  if (cell === CELL_STATES.SHIP) {
    return base + (showShips ? " btn-secondary" : " btn-outline-secondary");
  }

  // Color shots ONLY on opponent grid
  if (!showShips) {
    if (cell === CELL_STATES.HIT || cell === CELL_STATES.SUNK) {
      return base + " btn-success fw-bold text-white"; // green for hit/sunk
    }
    if (cell === CELL_STATES.MISS) {
      return base + " btn-danger fw-bold text-white"; // red for miss
    }
  } else {
    // Your fleet stays neutral for shot markers
    return base + " btn-outline-secondary";
  }

  return base;
}

export function Board({
  title,
  board,
  clickable,
  disabled,
  showShips,
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
                    
                    {!showShips &&
                      (cell === CELL_STATES.HIT || cell === CELL_STATES.SUNK) &&
                      "✓"}
                    {!showShips && cell === CELL_STATES.MISS && "✕"}
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
