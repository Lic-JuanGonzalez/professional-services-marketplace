import { useEffect, useState } from "react";
import { apiFetch } from "../../api";
import { ProfessionalCard } from "../components/ProfessionalCard";
import { Message } from "../components/Message";

export function HomePage() {
  const [category, setCategory] = useState("");
  const [appliedCategory, setAppliedCategory] = useState("");
  const [professionals, setProfessionals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    apiFetch("/professionals", { query: { category: appliedCategory } }).then((res) => {
      if (cancelled) return;
      if (res.ok) setProfessionals(res.data);
      else setError(res.data?.detail || "Could not load professionals.");
      setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [appliedCategory]);

  const handleFilter = (e) => {
    e.preventDefault();
    setAppliedCategory(category.trim());
  };

  const clearFilter = () => {
    setCategory("");
    setAppliedCategory("");
  };

  return (
    <div className="panel">
      <section className="hero">
        <h1>Find a professional</h1>
        <p className="subtitle">
          Browse professionals across every category and hire the right one for the job.
        </p>
      </section>

      <form onSubmit={handleFilter} className="filter-bar">
        <label>
          Category
          <input
            value={category}
            onChange={(e) => setCategory(e.target.value)}
            placeholder="e.g. plumbing"
          />
        </label>
        <button type="submit">Filter</button>
        {appliedCategory && (
          <button type="button" className="btn-secondary" onClick={clearFilter}>
            Clear
          </button>
        )}
      </form>

      {loading && <Message>Loading professionals…</Message>}
      {error && <Message tone="error">{error}</Message>}
      {!loading && !error && professionals.length === 0 && (
        <Message>
          No professionals found{appliedCategory ? ` for "${appliedCategory}"` : ""}.
        </Message>
      )}

      <div className="card-grid">
        {professionals.map((professional) => (
          <ProfessionalCard key={professional.id} professional={professional} />
        ))}
      </div>
    </div>
  );
}
