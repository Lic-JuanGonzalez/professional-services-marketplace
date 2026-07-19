import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

function navLinkClass({ isActive }) {
  return isActive ? "nav-link nav-link-active" : "nav-link";
}

export function NavBar() {
  const { session, logout } = useAuth();

  return (
    <header className="nav-bar">
      <div className="nav-bar-inner">
        <Link to="/" className="nav-brand">
          Marketplace
        </Link>
        <nav className="nav-links">
          {session ? (
            <>
              {session.role === "PROFESSIONAL" && (
                <NavLink to="/dashboard" className={navLinkClass}>
                  Dashboard
                </NavLink>
              )}
              {session.role === "CLIENT" && (
                <NavLink to="/hires" className={navLinkClass}>
                  My hires
                </NavLink>
              )}
              <span className="nav-user">{session.fullName}</span>
              <button type="button" className="btn-secondary" onClick={logout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className={navLinkClass}>
                Login
              </NavLink>
              <NavLink to="/register" className={navLinkClass}>
                Register
              </NavLink>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
