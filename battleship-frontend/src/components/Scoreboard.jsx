import React from "react";

export default function Scoreboard({
  title,
  sunkShips = [],
  hits = 0,
  misses = 0,
}) {


  return (
    <div className="mt-3 p-2 border rounded bg-dark text-light">
      <div className="fw-semibold mb-2">{title}</div>
      <div className="small">
        <div>
          Hits: <strong>{hits}</strong>
        </div>
        <div>
          Misses: <strong>{misses}</strong>
        </div>
        <div className="mt-2">Sunk ships:</div>
        {sunkShips.length ? (
          <ul className="mb-0">
            {sunkShips.map((s, i) => (
              <li key={i}>{s}</li>
            ))}
          </ul>
        ) : (
          <div className="text-muted">— none yet —</div>
        )}
      </div>
    </div>
  );
}
