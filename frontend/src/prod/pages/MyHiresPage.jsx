import { useEffect, useState } from "react";
import { apiFetch } from "../../api";
import { useAuth } from "../../context/AuthContext";
import { StatusBadge } from "../components/StatusBadge";
import { Message } from "../components/Message";

function ReviewForm({ hireId, onDone }) {
  const { session } = useAuth();
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch(`/reviews/hires/${hireId}`, {
      method: "POST",
      token: session.token,
      body: { rating: Number(rating), comment: comment || undefined },
    });
    setLoading(false);
    if (res.ok) onDone();
    else setError(res.data?.detail || "Could not submit the review.");
  };

  return (
    <form onSubmit={submit} className="review-form">
      <label>
        Rating
        <select value={rating} onChange={(e) => setRating(e.target.value)}>
          {[5, 4, 3, 2, 1].map((n) => (
            <option key={n} value={n}>
              {n} star{n > 1 ? "s" : ""}
            </option>
          ))}
        </select>
      </label>
      <label>
        Comment
        <textarea value={comment} onChange={(e) => setComment(e.target.value)} />
      </label>
      {error && <Message tone="error">{error}</Message>}
      <button type="submit" disabled={loading}>
        {loading ? "Submitting…" : "Submit review"}
      </button>
    </form>
  );
}

function HireCard({ hire, reviewed, onChange }) {
  const { session } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [reviewOpen, setReviewOpen] = useState(false);

  const cancel = async () => {
    setBusy(true);
    setError("");
    const res = await apiFetch(`/hires/${hire.id}/status`, {
      method: "PATCH",
      token: session.token,
      body: { status: "CANCELLED" },
    });
    setBusy(false);
    if (res.ok) onChange();
    else setError(res.data?.detail || "Could not cancel the hire.");
  };

  return (
    <div className="card">
      <div className="hire-card-header">
        <h3>{hire.serviceTitle}</h3>
        <StatusBadge status={hire.status} />
      </div>
      {hire.scheduledDate && (
        <p className="hint">Scheduled: {new Date(hire.scheduledDate).toLocaleString()}</p>
      )}
      {hire.notes && <p>{hire.notes}</p>}
      {error && <Message tone="error">{error}</Message>}
      {hire.status === "PENDING" && (
        <button type="button" className="btn-danger" onClick={cancel} disabled={busy}>
          {busy ? "Cancelling…" : "Cancel request"}
        </button>
      )}
      {hire.status === "COMPLETED" && !reviewed && !reviewOpen && (
        <button type="button" onClick={() => setReviewOpen(true)}>
          Leave a review
        </button>
      )}
      {hire.status === "COMPLETED" && !reviewed && reviewOpen && (
        <ReviewForm
          hireId={hire.id}
          onDone={() => {
            setReviewOpen(false);
            onChange();
          }}
        />
      )}
      {hire.status === "COMPLETED" && reviewed && <p className="hint">You reviewed this hire.</p>}
    </div>
  );
}

export function MyHiresPage() {
  const { session } = useAuth();
  const [hires, setHires] = useState([]);
  const [reviewedHireIds, setReviewedHireIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    apiFetch("/hires/mine", { token: session.token }).then(async (res) => {
      if (cancelled) return;
      if (!res.ok) {
        setError(res.data?.detail || "Could not load your hires.");
        setLoading(false);
        return;
      }
      const list = res.data;
      const completedProfessionalIds = [
        ...new Set(list.filter((h) => h.status === "COMPLETED").map((h) => h.professionalId)),
      ];
      const reviewResponses = await Promise.all(
        completedProfessionalIds.map((pid) => apiFetch(`/reviews/professionals/${pid}`)),
      );
      if (cancelled) return;
      const reviewed = new Set();
      reviewResponses.forEach((r) => {
        if (r.ok) r.data.forEach((review) => reviewed.add(review.hireId));
      });
      setHires(list);
      setReviewedHireIds(reviewed);
      setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [session.token, refreshKey]);

  const refresh = () => setRefreshKey((k) => k + 1);

  return (
    <div className="panel">
      <h1>My hires</h1>
      {loading && <Message>Loading your hires…</Message>}
      {error && <Message tone="error">{error}</Message>}
      {!loading && !error && hires.length === 0 && (
        <Message>You haven&apos;t hired anyone yet. Browse professionals to get started.</Message>
      )}
      {!loading && !error && hires.length > 0 && (
        <div className="card-grid">
          {hires.map((hire) => (
            <HireCard
              key={hire.id}
              hire={hire}
              reviewed={reviewedHireIds.has(hire.id)}
              onChange={refresh}
            />
          ))}
        </div>
      )}
    </div>
  );
}
