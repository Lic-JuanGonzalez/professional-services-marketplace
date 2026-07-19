import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { Message } from "./Message";

export function RequireAuth({ role, children }) {
  const { session } = useAuth();
  const location = useLocation();

  if (!session) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  }

  if (role && session.role !== role) {
    return <Message tone="error">This page is only available to {role.toLowerCase()} accounts.</Message>;
  }

  return children;
}
