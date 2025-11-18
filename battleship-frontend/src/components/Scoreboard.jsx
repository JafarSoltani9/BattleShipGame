import React from "react";
export function HandoffOverlay({ visible, nextPlayerName, onContinue }) {
  if (!visible) return null;
  return (
    <div
      className="position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center"
      style={{ backgroundColor: "rgba(0,0,0,0.7)", zIndex: 1050 }}
    >
      {" "}
      <div
        className="card bg-dark text-light"
        style={{ maxWidth: "400px", width: "100%" }}
      >
        {" "}
        <div className="card-body text-center">
          {" "}
          <h5 className="card-title mb-3">Next Player</h5>{" "}
          <p className="card-text">
            {" "}
            Pass the device to <strong>{nextPlayerName}</strong>.{" "}
          </p>{" "}
          <button
            type="button"
            className="btn btn-primary mt-2"
            onClick={onContinue}
          >
            {" "}
            I am ready{" "}
          </button>{" "}
        </div>{" "}
      </div>{" "}
    </div>
  );
}
export default HandoffOverlay;
