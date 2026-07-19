import { useState } from "react";
import { apiFetch } from "../api";
import { useAuth } from "../context/AuthContext";
import { TestConsoleApp } from "./TestConsoleApp";

export function AdminGate() {
  const { session, login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  if (session?.role === "ADMIN") {
    return <TestConsoleApp />;
  }

  const submit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    const res = await apiFetch("/auth/login", { method: "POST", body: { email, password } });
    setLoading(false);
    if (!res.ok) {
      setError(res.data?.detail || "Login failed");
      return;
    }
    if (res.data.role !== "ADMIN") {
      setError("This account is not an admin.");
      return;
    }
    login(res.data);
  };

  return (
    <div className="app">
      <div className="card" style={{ maxWidth: 360, margin: "80px auto" }}>
        <h3>Admin sign-in</h3>
        <p className="hint">The test console lives behind an admin login.</p>
        <form onSubmit={submit} style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          <label>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={loading}>
            Sign in
          </button>
        </form>
        {error && <p style={{ color: "var(--error)" }}>{error}</p>}
        {session && session.role !== "ADMIN" && (
          <p className="hint">
            Logged in as {session.email} ({session.role}) — not an admin.
          </p>
        )}
      </div>
    </div>
  );
}
