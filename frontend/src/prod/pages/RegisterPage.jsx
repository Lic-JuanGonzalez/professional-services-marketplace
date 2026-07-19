import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { apiFetch } from "../../api";
import { useAuth } from "../../context/AuthContext";
import { Message } from "../components/Message";

const emptyForm = { fullName: "", email: "", password: "", role: "CLIENT" };

export function RegisterPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch("/auth/register", { method: "POST", body: form });
    setLoading(false);
    if (res.ok) {
      login(res.data);
      navigate(res.data.role === "PROFESSIONAL" ? "/dashboard" : "/", { replace: true });
    } else {
      setError(res.data?.detail || "Could not create the account.");
    }
  };

  return (
    <div className="auth-page">
      <form onSubmit={submit} className="card auth-form">
        <h1>Create an account</h1>
        <label>
          Full name
          <input
            value={form.fullName}
            onChange={(e) => setForm({ ...form, fullName: e.target.value })}
            required
          />
        </label>
        <label>
          Email
          <input
            type="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
            minLength={8}
          />
        </label>
        <label>
          I am a
          <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
            <option value="CLIENT">Client, looking to hire</option>
            <option value="PROFESSIONAL">Professional, offering services</option>
          </select>
        </label>
        {error && <Message tone="error">{error}</Message>}
        <button type="submit" disabled={loading}>
          {loading ? "Creating account…" : "Register"}
        </button>
        <p className="hint">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      </form>
    </div>
  );
}
