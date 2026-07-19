import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { apiFetch } from "../../api";
import { useAuth } from "../../context/AuthContext";
import { StarRating } from "../components/StarRating";
import { Message } from "../components/Message";

function HireForm({ service, onDone, onCancel }) {
  const { session } = useAuth();
  const [scheduledDate, setScheduledDate] = useState("");
  const [notes, setNotes] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch("/hires", {
      method: "POST",
      token: session.token,
      body: {
        serviceOfferingId: service.id,
        scheduledDate: scheduledDate ? new Date(scheduledDate).toISOString() : undefined,
        notes: notes || undefined,
      },
    });
    setLoading(false);
    if (res.ok) onDone();
    else setError(res.data?.detail || "Could not create the hire request.");
  };

  return (
    <form onSubmit={submit} className="hire-form">
      <label>
        Preferred date (optional)
        <input
          type="datetime-local"
          value={scheduledDate}
          onChange={(e) => setScheduledDate(e.target.value)}
        />
      </label>
      <label>
        Notes
        <textarea value={notes} onChange={(e) => setNotes(e.target.value)} />
      </label>
      {error && <Message tone="error">{error}</Message>}
      <div className="form-actions">
        <button type="submit" disabled={loading}>
          {loading ? "Sending…" : "Send request"}
        </button>
        <button type="button" className="btn-secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

function ServiceCard({ service, canHire }) {
  const [open, setOpen] = useState(false);
  const [sent, setSent] = useState(false);

  return (
    <div className="card">
      <h3>{service.title}</h3>
      {service.description && <p>{service.description}</p>}
      <p className="service-price">${service.price}</p>
      {sent && <Message tone="ok">Request sent — track it from “My hires”.</Message>}
      {canHire && !sent && !open && (
        <button type="button" onClick={() => setOpen(true)}>
          Hire
        </button>
      )}
      {canHire && !sent && open && (
        <HireForm
          service={service}
          onDone={() => {
            setOpen(false);
            setSent(true);
          }}
          onCancel={() => setOpen(false)}
        />
      )}
    </div>
  );
}

export function ProfessionalDetailPage() {
  const { id } = useParams();
  const { session } = useAuth();

  const [professional, setProfessional] = useState(null);
  const [services, setServices] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    Promise.all([
      apiFetch(`/professionals/${id}`),
      apiFetch("/services"),
      apiFetch(`/reviews/professionals/${id}`),
    ]).then(([profRes, servicesRes, reviewsRes]) => {
      if (cancelled) return;
      if (!profRes.ok) {
        setError(profRes.data?.detail || "Professional not found.");
        setLoading(false);
        return;
      }
      setProfessional(profRes.data);
      setServices(
        servicesRes.ok
          ? servicesRes.data.filter((s) => String(s.professionalId) === String(id))
          : [],
      );
      setReviews(reviewsRes.ok ? reviewsRes.data : []);
      setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [id]);

  if (loading) return <Message>Loading profile…</Message>;
  if (error) return <Message tone="error">{error}</Message>;
  if (!professional) return null;

  return (
    <div className="panel">
      <Link to="/" className="back-link">
        ← Back to professionals
      </Link>

      <section className="card profile-header">
        <div>
          <h1>{professional.headline}</h1>
          <p className="professional-card-meta">
            {professional.category}
            {professional.location ? ` · ${professional.location}` : ""}
          </p>
          <StarRating rating={professional.avgRating} reviewCount={professional.reviewCount} />
        </div>
        {professional.verified && <span className="badge">Verified</span>}
      </section>

      {professional.bio && <p className="profile-bio">{professional.bio}</p>}

      <section className="dashboard-section">
        <h2>Services</h2>
        {services.length === 0 ? (
          <Message>No active services yet.</Message>
        ) : (
          <div className="card-grid">
            {services.map((service) => (
              <ServiceCard
                key={service.id}
                service={service}
                canHire={session?.role === "CLIENT"}
              />
            ))}
          </div>
        )}
      </section>

      <section className="dashboard-section">
        <h2>Reviews</h2>
        {reviews.length === 0 ? (
          <Message>No reviews yet.</Message>
        ) : (
          <ul className="review-list">
            {reviews.map((review) => (
              <li key={review.id} className="card review-item">
                <div className="review-item-header">
                  <strong>{review.reviewerName}</strong>
                  <StarRating rating={review.rating} />
                </div>
                {review.comment && <p>{review.comment}</p>}
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
