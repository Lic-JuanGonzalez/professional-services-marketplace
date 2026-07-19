import { useEffect, useState } from "react";
import { apiFetch } from "../../api";
import { useAuth } from "../../context/AuthContext";
import { StatusBadge } from "../components/StatusBadge";
import { Message } from "../components/Message";

const emptyProfileForm = { headline: "", bio: "", category: "", location: "", hourlyRate: "" };
const emptyServiceForm = { title: "", description: "", category: "", price: "" };

function toProfileBody(form) {
  return {
    ...form,
    hourlyRate: form.hourlyRate === "" ? undefined : Number(form.hourlyRate),
  };
}

function toServiceBody(form) {
  return { ...form, price: form.price === "" ? undefined : Number(form.price) };
}

function CreateProfileForm({ onCreated }) {
  const { session } = useAuth();
  const [form, setForm] = useState(emptyProfileForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch("/professionals", {
      method: "POST",
      token: session.token,
      body: toProfileBody(form),
    });
    setLoading(false);
    if (res.ok) onCreated(res.data);
    else setError(res.data?.detail || "Could not create your profile.");
  };

  return (
    <form onSubmit={submit} className="card auth-form">
      <h1>Create your professional profile</h1>
      <p className="hint">You need a profile before you can publish services or receive hires.</p>
      <label>
        Headline
        <input
          value={form.headline}
          onChange={(e) => setForm({ ...form, headline: e.target.value })}
          required
        />
      </label>
      <label>
        Bio
        <textarea value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} />
      </label>
      <label>
        Category
        <input
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
          required
        />
      </label>
      <label>
        Location
        <input
          value={form.location}
          onChange={(e) => setForm({ ...form, location: e.target.value })}
        />
      </label>
      <label>
        Hourly rate
        <input
          type="number"
          step="0.01"
          value={form.hourlyRate}
          onChange={(e) => setForm({ ...form, hourlyRate: e.target.value })}
        />
      </label>
      {error && <Message tone="error">{error}</Message>}
      <button type="submit" disabled={loading}>
        {loading ? "Creating…" : "Create profile"}
      </button>
    </form>
  );
}

function EditProfileForm({ profile, onSaved, onCancel }) {
  const { session } = useAuth();
  const [form, setForm] = useState({
    headline: profile.headline || "",
    bio: profile.bio || "",
    category: profile.category || "",
    location: profile.location || "",
    hourlyRate: profile.hourlyRate ?? "",
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch(`/professionals/${profile.id}`, {
      method: "PUT",
      token: session.token,
      body: toProfileBody(form),
    });
    setLoading(false);
    if (res.ok) onSaved(res.data);
    else setError(res.data?.detail || "Could not update your profile.");
  };

  return (
    <form onSubmit={submit} className="card">
      <h3>Edit profile</h3>
      <label>
        Headline
        <input
          value={form.headline}
          onChange={(e) => setForm({ ...form, headline: e.target.value })}
          required
        />
      </label>
      <label>
        Bio
        <textarea value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} />
      </label>
      <label>
        Category
        <input
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
          required
        />
      </label>
      <label>
        Location
        <input
          value={form.location}
          onChange={(e) => setForm({ ...form, location: e.target.value })}
        />
      </label>
      <label>
        Hourly rate
        <input
          type="number"
          step="0.01"
          value={form.hourlyRate}
          onChange={(e) => setForm({ ...form, hourlyRate: e.target.value })}
        />
      </label>
      {error && <Message tone="error">{error}</Message>}
      <div className="form-actions">
        <button type="submit" disabled={loading}>
          {loading ? "Saving…" : "Save changes"}
        </button>
        <button type="button" className="btn-secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

function CreateServiceForm({ onCreated }) {
  const { session } = useAuth();
  const [form, setForm] = useState(emptyServiceForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch("/services", {
      method: "POST",
      token: session.token,
      body: toServiceBody(form),
    });
    setLoading(false);
    if (res.ok) {
      setForm(emptyServiceForm);
      onCreated(res.data);
    } else {
      setError(res.data?.detail || "Could not create the service.");
    }
  };

  return (
    <form onSubmit={submit} className="card">
      <h3>New service</h3>
      <label>
        Title
        <input
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          required
        />
      </label>
      <label>
        Description
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
      </label>
      <label>
        Category
        <input
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
          required
        />
      </label>
      <label>
        Price
        <input
          type="number"
          step="0.01"
          value={form.price}
          onChange={(e) => setForm({ ...form, price: e.target.value })}
          required
        />
      </label>
      {error && <Message tone="error">{error}</Message>}
      <button type="submit" disabled={loading}>
        {loading ? "Publishing…" : "Publish service"}
      </button>
    </form>
  );
}

function EditServiceForm({ service, onSaved, onCancel }) {
  const { session } = useAuth();
  const [form, setForm] = useState({
    title: service.title,
    description: service.description || "",
    category: service.category,
    price: service.price,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch(`/services/${service.id}`, {
      method: "PUT",
      token: session.token,
      body: toServiceBody(form),
    });
    setLoading(false);
    if (res.ok) onSaved(res.data);
    else setError(res.data?.detail || "Could not update the service.");
  };

  return (
    <form onSubmit={submit} className="card">
      <label>
        Title
        <input
          value={form.title}
          onChange={(e) => setForm({ ...form, title: e.target.value })}
          required
        />
      </label>
      <label>
        Description
        <textarea
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
      </label>
      <label>
        Category
        <input
          value={form.category}
          onChange={(e) => setForm({ ...form, category: e.target.value })}
          required
        />
      </label>
      <label>
        Price
        <input
          type="number"
          step="0.01"
          value={form.price}
          onChange={(e) => setForm({ ...form, price: e.target.value })}
          required
        />
      </label>
      {error && <Message tone="error">{error}</Message>}
      <div className="form-actions">
        <button type="submit" disabled={loading}>
          {loading ? "Saving…" : "Save"}
        </button>
        <button type="button" className="btn-secondary" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

function ServiceCard({ service, onChanged }) {
  const { session } = useAuth();
  const [editing, setEditing] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const remove = async () => {
    setBusy(true);
    setError("");
    const res = await apiFetch(`/services/${service.id}`, {
      method: "DELETE",
      token: session.token,
    });
    setBusy(false);
    if (res.ok) onChanged();
    else setError(res.data?.detail || "Could not delete the service.");
  };

  if (editing) {
    return (
      <EditServiceForm
        service={service}
        onSaved={() => {
          setEditing(false);
          onChanged();
        }}
        onCancel={() => setEditing(false)}
      />
    );
  }

  return (
    <div className="card">
      <h3>{service.title}</h3>
      {service.description && <p>{service.description}</p>}
      <p className="professional-card-meta">{service.category}</p>
      <p className="service-price">${service.price}</p>
      {error && <Message tone="error">{error}</Message>}
      <div className="form-actions">
        <button type="button" className="btn-secondary" onClick={() => setEditing(true)}>
          Edit
        </button>
        <button type="button" className="btn-danger" onClick={remove} disabled={busy}>
          {busy ? "Removing…" : "Remove"}
        </button>
      </div>
    </div>
  );
}

function ServicesTab({ profileId }) {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    apiFetch("/services").then((res) => {
      if (cancelled) return;
      if (res.ok) {
        setServices(res.data.filter((s) => String(s.professionalId) === String(profileId)));
      } else {
        setError(res.data?.detail || "Could not load your services.");
      }
      setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [profileId, refreshKey]);

  const refresh = () => setRefreshKey((k) => k + 1);

  return (
    <div className="dashboard-section">
      <CreateServiceForm onCreated={refresh} />
      <h2>Your services</h2>
      {loading && <Message>Loading your services…</Message>}
      {error && <Message tone="error">{error}</Message>}
      {!loading && !error && services.length === 0 && (
        <Message>You haven&apos;t published any services yet.</Message>
      )}
      {!loading && !error && services.length > 0 && (
        <div className="card-grid">
          {services.map((service) => (
            <ServiceCard key={service.id} service={service} onChanged={refresh} />
          ))}
        </div>
      )}
    </div>
  );
}

function HireRow({ hire, onChanged }) {
  const { session } = useAuth();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const transition = async (status) => {
    setBusy(true);
    setError("");
    const res = await apiFetch(`/hires/${hire.id}/status`, {
      method: "PATCH",
      token: session.token,
      body: { status },
    });
    setBusy(false);
    if (res.ok) onChanged();
    else setError(res.data?.detail || "Could not update this hire.");
  };

  return (
    <div className="card">
      <div className="hire-card-header">
        <h3>{hire.serviceTitle}</h3>
        <StatusBadge status={hire.status} />
      </div>
      <p className="hint">Client #{hire.clientId}</p>
      {hire.scheduledDate && (
        <p className="hint">Scheduled: {new Date(hire.scheduledDate).toLocaleString()}</p>
      )}
      {hire.notes && <p>{hire.notes}</p>}
      {error && <Message tone="error">{error}</Message>}
      {hire.status === "PENDING" && (
        <div className="form-actions">
          <button type="button" onClick={() => transition("ACCEPTED")} disabled={busy}>
            Accept
          </button>
          <button
            type="button"
            className="btn-danger"
            onClick={() => transition("REJECTED")}
            disabled={busy}
          >
            Reject
          </button>
        </div>
      )}
      {hire.status === "ACCEPTED" && (
        <button type="button" onClick={() => transition("COMPLETED")} disabled={busy}>
          Mark completed
        </button>
      )}
    </div>
  );
}

function HiresTab({ profileId }) {
  const { session } = useAuth();
  const [hires, setHires] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    apiFetch("/hires/mine", { token: session.token }).then((res) => {
      if (cancelled) return;
      if (res.ok) {
        setHires(res.data.filter((h) => String(h.professionalId) === String(profileId)));
      } else {
        setError(res.data?.detail || "Could not load your incoming hires.");
      }
      setLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [profileId, session.token, refreshKey]);

  const refresh = () => setRefreshKey((k) => k + 1);

  return (
    <div className="dashboard-section">
      <h2>Incoming hires</h2>
      {loading && <Message>Loading incoming hires…</Message>}
      {error && <Message tone="error">{error}</Message>}
      {!loading && !error && hires.length === 0 && <Message>No hire requests yet.</Message>}
      {!loading && !error && hires.length > 0 && (
        <div className="card-grid">
          {hires.map((hire) => (
            <HireRow key={hire.id} hire={hire} onChanged={refresh} />
          ))}
        </div>
      )}
    </div>
  );
}

export function ProfessionalDashboardPage() {
  const { session } = useAuth();
  const [profile, setProfile] = useState(null);
  const [profileLoading, setProfileLoading] = useState(true);
  const [error, setError] = useState("");
  const [editingProfile, setEditingProfile] = useState(false);
  const [tab, setTab] = useState("services");

  useEffect(() => {
    let cancelled = false;
    setProfileLoading(true);
    setError("");
    apiFetch("/professionals").then((res) => {
      if (cancelled) return;
      if (res.ok) {
        const mine = res.data.find((p) => String(p.userId) === String(session.userId));
        setProfile(mine || null);
      } else {
        setError(res.data?.detail || "Could not load your profile.");
      }
      setProfileLoading(false);
    });
    return () => {
      cancelled = true;
    };
  }, [session.userId]);

  if (profileLoading) return <Message>Loading your dashboard…</Message>;
  if (error) return <Message tone="error">{error}</Message>;

  if (!profile) {
    return (
      <div className="panel">
        <CreateProfileForm onCreated={setProfile} />
      </div>
    );
  }

  return (
    <div className="panel">
      {editingProfile ? (
        <EditProfileForm
          profile={profile}
          onSaved={(updated) => {
            setProfile(updated);
            setEditingProfile(false);
          }}
          onCancel={() => setEditingProfile(false)}
        />
      ) : (
        <section className="card dashboard-header">
          <div>
            <h1>{profile.headline}</h1>
            <p className="professional-card-meta">
              {profile.category}
              {profile.location ? ` · ${profile.location}` : ""}
            </p>
            {profile.hourlyRate != null && (
              <p className="professional-card-rate">${profile.hourlyRate}/hr</p>
            )}
          </div>
          <button type="button" className="btn-secondary" onClick={() => setEditingProfile(true)}>
            Edit profile
          </button>
        </section>
      )}

      <div className="tabs">
        <button
          type="button"
          className={tab === "services" ? "tab tab-active" : "tab"}
          onClick={() => setTab("services")}
        >
          Services
        </button>
        <button
          type="button"
          className={tab === "hires" ? "tab tab-active" : "tab"}
          onClick={() => setTab("hires")}
        >
          Incoming hires
        </button>
      </div>

      {tab === "services" ? (
        <ServicesTab profileId={profile.id} />
      ) : (
        <HiresTab profileId={profile.id} />
      )}
    </div>
  );
}
