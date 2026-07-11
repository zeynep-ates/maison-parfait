import { useCallback, useEffect, useRef, useState } from "react";
import { api, ApiError } from "../lib/api";
import { AuthContext } from "./auth-context";

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [accessToken, setAccessToken] = useState(null);
  const [initializing, setInitializing] = useState(true);
  const tokenRef = useRef(null);
  tokenRef.current = accessToken;

  useEffect(() => {
    let cancelled = false;
    api
      .refresh()
      .then((data) => {
        if (cancelled) return;
        setAccessToken(data.accessToken);
        setUser(data.user);
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setInitializing(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const login = useCallback(async (email, password, rememberMe) => {
    const data = await api.login(email, password, rememberMe);
    setAccessToken(data.accessToken);
    setUser(data.user);
    return data;
  }, []);

  const register = useCallback((body) => api.register(body), []);

  const logout = useCallback(async () => {
    try {
      await api.logout();
    } finally {
      setAccessToken(null);
      setUser(null);
    }
  }, []);

  const refresh = useCallback(async () => {
    const data = await api.refresh();
    setAccessToken(data.accessToken);
    setUser(data.user);
    return data;
  }, []);

  // Runs an authenticated call, silently refreshing and retrying once if the access token expired.
  const withAuth = useCallback(
    async (fn) => {
      try {
        return await fn(tokenRef.current);
      } catch (err) {
        if (err instanceof ApiError && err.status === 401) {
          const data = await refresh();
          return fn(data.accessToken);
        }
        throw err;
      }
    },
    [refresh]
  );

  const value = {
    user,
    accessToken,
    isAuthenticated: !!user,
    initializing,
    login,
    register,
    logout,
    refresh,
    withAuth,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
