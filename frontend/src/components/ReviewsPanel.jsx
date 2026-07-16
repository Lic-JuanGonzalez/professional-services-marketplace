import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useApiRunner } from "../useApiRunner";
import { JsonPanel } from "./JsonPanel";

export function ReviewsPanel() {
  const { session } = useAuth();
  const { result, loading, run } = useApiRunner();
  const [hireId, setHireId] = useState("");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [professionalId, setProfessionalId] = useState("");

  const create = (e) => {
    e.preventDefault();
    run(`/reviews/hires/${hireId}`, {
      method: "POST",
      body: { rating: Number(rating), comment: comment || undefined },
      token: session?.token,
    });
  };

  const list = () => run(`/reviews/professionals/${professionalId}`);

  return (
    <div className="panel">
      <div className="form-grid">
        <form onSubmit={create} className="card">
          <h3>Leave review (as CLIENT, hire must be COMPLETED)</h3>
          <label>
            Hire id
            <input value={hireId} onChange={(e) => setHireId(e.target.value)} required />
          </label>
          <label>
            Rating (1-5)
            <input
              type="number"
              min={1}
              max={5}
              value={rating}
              onChange={(e) => setRating(e.target.value)}
              required
            />
          </label>
          <label>
            Comment
            <textarea value={comment} onChange={(e) => setComment(e.target.value)} />
          </label>
          <button type="submit" disabled={loading || !session}>
            Submit review
          </button>
          {!session && <p className="hint">Login first.</p>}
        </form>

        <div className="card">
          <h3>Reviews for a professional (public)</h3>
          <label>
            Professional profile id
            <input
              value={professionalId}
              onChange={(e) => setProfessionalId(e.target.value)}
            />
          </label>
          <button type="button" onClick={list} disabled={loading || !professionalId}>
            List reviews
          </button>
        </div>
      </div>

      <JsonPanel result={result} />
    </div>
  );
}
