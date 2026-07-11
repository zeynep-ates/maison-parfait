import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/auth-context";

export function RequireAuth({ children }) {
  const { isAuthenticated, initializing } = useAuth();
  const location = useLocation();

  if (initializing) {
    return <div className="mx-auto max-w-6xl px-5 py-24 text-center text-[13px] text-cocoa">Loading…</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return children;
}
