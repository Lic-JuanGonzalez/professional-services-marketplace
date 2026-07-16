import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useApiRunner } from "../useApiRunner";
import { JsonPanel } from "./JsonPanel";

const emptyService = { title: "", description: "", category: "", price: "" };

function toBody(form) {
  return { ...form, price: form.price === "" ? undefined : Number(form.price) };
}

export function ServicesPanel() {
  const { session } = useAuth();
  const { result, loading, run } = useApiRunner();
  const [createForm, setCreateForm] = useState(emptyService);
  const [updateId, setUpdateId] = useState("");
  const [updateForm, setUpdateForm] = useState(emptyService);
  const [deleteId, setDeleteId] = useState("");
  const [category, setCategory] = useState("");
  const [lookupId, setLookupId] = useState("");

  const create = (e) => {
    e.preventDefault();
    run("/services", { method: "POST", body: toBody(createForm), token: session?.token });
  };

  const update = (e) => {
    e.preventDefault();
    run(`/services/${updateId}`, {
      method: "PUT",
      body: toBody(updateForm),
      token: session?.token,
    });
  };

  const remove = (e) => {
    e.preventDefault();
    run(`/services/${deleteId}`, { method: "DELETE", token: session?.token });
  };

  const list = () => run("/services", { query: { category } });
  const getById = () => run(`/services/${lookupId}`);

  return (
    <div className="panel">
      <div className="form-grid">
        <form onSubmit={create} className="card">
          <h3>Create service</h3>
          <label>
            Title
            <input
              value={createForm.title}
              onChange={(e) => setCreateForm({ ...createForm, title: e.target.value })}
              required
            />
          </label>
          <label>
            Description
            <textarea
              value={createForm.description}
              onChange={(e) => setCreateForm({ ...createForm, description: e.target.value })}
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
            Price
            <input
              type="number"
              step="0.01"
              value={createForm.price}
              onChange={(e) => setCreateForm({ ...createForm, price: e.target.value })}
              required
            />
          </label>
          <button type="submit" disabled={loading || !session}>
            Create service
          </button>
          {!session && <p className="hint">Login as PROFESSIONAL first.</p>}
        </form>

        <form onSubmit={update} className="card">
          <h3>Update service</h3>
          <label>
            Service id
            <input value={updateId} onChange={(e) => setUpdateId(e.target.value)} required />
          </label>
          <label>
            Title
            <input
              value={updateForm.title}
              onChange={(e) => setUpdateForm({ ...updateForm, title: e.target.value })}
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
            Price
            <input
              type="number"
              step="0.01"
              value={updateForm.price}
              onChange={(e) => setUpdateForm({ ...updateForm, price: e.target.value })}
              required
            />
          </label>
          <button type="submit" disabled={loading || !session}>
            Update service
          </button>
        </form>

        <div className="card">
          <h3>Browse (public)</h3>
          <label>
            Category filter
            <input value={category} onChange={(e) => setCategory(e.target.value)} />
          </label>
          <button type="button" onClick={list} disabled={loading}>
            List services
          </button>
          <label>
            Service id
            <input value={lookupId} onChange={(e) => setLookupId(e.target.value)} />
          </label>
          <button type="button" onClick={getById} disabled={loading || !lookupId}>
            Get by id
          </button>
        </div>

        <form onSubmit={remove} className="card">
          <h3>Delete (soft) service</h3>
          <label>
            Service id
            <input value={deleteId} onChange={(e) => setDeleteId(e.target.value)} required />
          </label>
          <button type="submit" disabled={loading || !session} className="btn-danger">
            Delete service
          </button>
        </form>
      </div>

      <JsonPanel result={result} />
    </div>
  );
}
