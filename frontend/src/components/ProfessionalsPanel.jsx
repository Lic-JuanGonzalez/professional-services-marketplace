import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useApiRunner } from "../useApiRunner";
import { JsonPanel } from "./JsonPanel";

const emptyProfile = { headline: "", bio: "", category: "", location: "", hourlyRate: "" };

function toBody(form) {
  return { ...form, hourlyRate: form.hourlyRate === "" ? undefined : Number(form.hourlyRate) };
}

export function ProfessionalsPanel() {
  const { session } = useAuth();
  const { result, loading, run } = useApiRunner();
  const [createForm, setCreateForm] = useState(emptyProfile);
  const [updateId, setUpdateId] = useState("");
  const [updateForm, setUpdateForm] = useState(emptyProfile);
  const [category, setCategory] = useState("");
  const [lookupId, setLookupId] = useState("");

  const create = (e) => {
    e.preventDefault();
    run("/professionals", { method: "POST", body: toBody(createForm), token: session?.token });
  };

  const update = (e) => {
    e.preventDefault();
    run(`/professionals/${updateId}`, {
      method: "PUT",
      body: toBody(updateForm),
      token: session?.token,
    });
  };

  const list = () => run("/professionals", { query: { category } });
  const getById = () => run(`/professionals/${lookupId}`);

  return (
    <div className="panel">
      <div className="form-grid">
        <form onSubmit={create} className="card">
          <h3>Create my profile</h3>
          <label>
            Headline
            <input
              value={createForm.headline}
              onChange={(e) => setCreateForm({ ...createForm, headline: e.target.value })}
              required
            />
          </label>
          <label>
            Bio
            <textarea
              value={createForm.bio}
              onChange={(e) => setCreateForm({ ...createForm, bio: e.target.value })}
            />
          </label>
          <label>
            Category
            <input
              value={createForm.category}
              onChange={(e) => setCreateForm({ ...createForm, category: e.target.value })}
              required
            />
          </label>
          <label>
            Location
            <input
              value={createForm.location}
              onChange={(e) => setCreateForm({ ...createForm, location: e.target.value })}
            />
          </label>
          <label>
            Hourly rate
            <input
              type="number"
              step="0.01"
              value={createForm.hourlyRate}
              onChange={(e) => setCreateForm({ ...createForm, hourlyRate: e.target.value })}
            />
          </label>
          <button type="submit" disabled={loading || !session}>
            Create profile
          </button>
          {!session && <p className="hint">Login as PROFESSIONAL first.</p>}
        </form>

        <form onSubmit={update} className="card">
          <h3>Update profile</h3>
          <label>
            Profile id
            <input value={updateId} onChange={(e) => setUpdateId(e.target.value)} required />
          </label>
          <label>
            Headline
            <input
              value={updateForm.headline}
              onChange={(e) => setUpdateForm({ ...updateForm, headline: e.target.value })}
              required
            />
          </label>
          <label>
            Category
            <input
              value={updateForm.category}
              onChange={(e) => setUpdateForm({ ...updateForm, category: e.target.value })}
              required
            />
          </label>
          <label>
            Hourly rate
            <input
              type="number"
              step="0.01"
              value={updateForm.hourlyRate}
              onChange={(e) => setUpdateForm({ ...updateForm, hourlyRate: e.target.value })}
            />
          </label>
          <button type="submit" disabled={loading || !session}>
            Update profile
          </button>
        </form>

        <div className="card">
          <h3>Browse (public)</h3>
          <label>
            Category filter
            <input value={category} onChange={(e) => setCategory(e.target.value)} />
          </label>
          <button type="button" onClick={list} disabled={loading}>
            List professionals
          </button>
          <label>
            Profile id
            <input value={lookupId} onChange={(e) => setLookupId(e.target.value)} />
          </label>
          <button type="button" onClick={getById} disabled={loading || !lookupId}>
            Get by id
          </button>
        </div>
      </div>

      <JsonPanel result={result} />
    </div>
  );
}
