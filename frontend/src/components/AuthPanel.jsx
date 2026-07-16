import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useApiRunner } from "../useApiRunner";
import { JsonPanel } from "./JsonPanel";

const emptyRegister = { fullName: "", email: "", password: "", role: "CLIENT" };
const emptyLogin = { email: "", password: "" };

export function AuthPanel() {
  const { session, login, logout } = useAuth();
  const { result, loading, run } = useApiRunner();
  const [registerForm, setRegisterForm] = useState(emptyRegister);
  const [loginForm, setLoginForm] = useState(emptyLogin);

  const handleRegister = async (e) => {
    e.preventDefault();
    const res = await run("/auth/register", { method: "POST", body: registerForm });
    if (res.ok) login(res.data);
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    const res = await run("/auth/login", { method: "POST", body: loginForm });
    if (res.ok) login(res.data);
  };

  return (
    <div className="panel">
      {session ? (
        <div className="session-box">
          <div>
            Logged in as <strong>{session.fullName}</strong> ({session.email}) —{" "}
            <span className="badge">{session.role}</span>
          </div>
          <button type="button" onClick={logout} className="btn-secondary">
            Logout
          </button>
        </div>
      ) : (
        <div className="session-box session-box-empty">No active session.</div>
      )}

      <div className="form-grid">
        <form onSubmit={handleRegister} className="card">
          <h3>Register</h3>
          <label>
            Full name
            <input
              value={registerForm.fullName}
              onChange={(e) => setRegisterForm({ ...registerForm, fullName: e.target.value })}
              required
            />
          </label>
          <label>
            Email
            <input
              type="email"
              value={registerForm.email}
              onChange={(e) => setRegisterForm({ ...registerForm, email: e.target.value })}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={registerForm.password}
              onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
              required
              minLength={8}
            />
          </label>
          <label>
            Role
            <select
              value={registerForm.role}
              onChange={(e) => setRegisterForm({ ...registerForm, role: e.target.value })}
            >
              <option value="CLIENT">CLIENT</option>
              <option value="PROFESSIONAL">PROFESSIONAL</option>
            </select>
          </label>
          <button type="submit" disabled={loading}>
            Register
          </button>
        </form>

        <form onSubmit={handleLogin} className="card">
          <h3>Login</h3>
          <label>
            Email
            <input
              type="email"
              value={loginForm.email}
              onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
              required
            />
          </label>
          <label>
            Password
            <input
              type="password"
              value={loginForm.password}
              onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
              required
            />
          </label>
          <button type="submit" disabled={loading}>
            Login
          </button>
        </form>
      </div>

      <JsonPanel result={result} />
    </div>
  );
}
