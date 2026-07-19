import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { apiFetch } from "../../api";
import { useAuth } from "../../context/AuthContext";
import { Message } from "../components/Message";

const emptyForm = { email: "", password: "" };

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState(emptyForm);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    const res = await apiFetch("/auth/login", { method: "POST", body: form });
    setLoading(false);
    if (res.ok) {
      login(res.data);
      const fallback = res.data.role === "PROFESSIONAL" ? "/dashboard" : "/";
      navigate(location.state?.from || fallback, { replace: true });
    } else {
      setError(res.data?.detail || "Invalid email or password.");
    }
  };

  return (
    <div className="auth-page">
      <form onSubmit={submit} className="card auth-form">
        <h1>Log in</h1>
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
          />
        </label>
        {error && <Message tone="error">{error}</Message>}
        <button type="submit" disabled={loading}>
          {loading ? "Logging in…" : "Log in"}
        </button>
        <p className="hint">
          No account yet? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
}
