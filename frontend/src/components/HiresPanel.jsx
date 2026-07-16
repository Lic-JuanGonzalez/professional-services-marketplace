import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useApiRunner } from "../useApiRunner";
import { JsonPanel } from "./JsonPanel";

const statuses = ["ACCEPTED", "REJECTED", "COMPLETED", "CANCELLED"];

export function HiresPanel() {
  const { session } = useAuth();
  const { result, loading, run } = useApiRunner();
  const [serviceOfferingId, setServiceOfferingId] = useState("");
  const [notes, setNotes] = useState("");
  const [lookupId, setLookupId] = useState("");
  const [statusId, setStatusId] = useState("");
  const [status, setStatus] = useState(statuses[0]);

  const create = (e) => {
    e.preventDefault();
    run("/hires", {
      method: "POST",
      body: { serviceOfferingId: Number(serviceOfferingId), notes: notes || undefined },
      token: session?.token,
    });
  };

  const getMine = () => run("/hires/mine", { token: session?.token });
  const getById = () => run(`/hires/${lookupId}`, { token: session?.token });

  const updateStatus = (e) => {
    e.preventDefault();
    run(`/hires/${statusId}/status`, {
      method: "PATCH",
      body: { status },
      token: session?.token,
    });
  };

  return (
    <div className="panel">
      <div className="form-grid">
        <form onSubmit={create} className="card">
          <h3>Create hire (as CLIENT)</h3>
          <label>
            Service offering id
            <input
              value={serviceOfferingId}
              onChange={(e) => setServiceOfferingId(e.target.value)}
              required
            />
          </label>
          <label>
            Notes
            <textarea value={notes} onChange={(e) => setNotes(e.target.value)} />
          </label>
          <button type="submit" disabled={loading || !session}>
            Create hire
          </button>
          {!session && <p className="hint">Login first.</p>}
        </form>

        <div className="card">
          <h3>Lookup</h3>
          <button type="button" onClick={getMine} disabled={loading || !session}>
            Get mine
          </button>
          <label>
            Hire id
            <input value={lookupId} onChange={(e) => setLookupId(e.target.value)} />
          </label>
          <button type="button" onClick={getById} disabled={loading || !lookupId || !session}>
            Get by id
          </button>
        </div>

        <form onSubmit={updateStatus} className="card">
          <h3>Update status</h3>
          <label>
            Hire id
            <input value={statusId} onChange={(e) => setStatusId(e.target.value)} required />
          </label>
          <label>
            New status
            <select value={status} onChange={(e) => setStatus(e.target.value)}>
              {statuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" disabled={loading || !session}>
            Update status
          </button>
        </form>
      </div>

      <JsonPanel result={result} />
    </div>
  );
}
