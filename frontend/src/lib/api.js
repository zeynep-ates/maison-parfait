const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

async function request(path, { method = "GET", body, token } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers,
    credentials: "include",
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;

  const text = await res.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!res.ok) {
    throw new ApiError(data?.message || res.statusText || "Something went wrong", res.status);
  }

  return data;
}

export const api = {
  register: (body) => request("/api/auth/register", { method: "POST", body }),
  verifyEmail: (token) => request("/api/auth/verify-email", { method: "POST", body: { token } }),
  resendVerification: (email) => request("/api/auth/resend-verification", { method: "POST", body: { email } }),
  forgotPassword: (email) => request("/api/auth/forgot-password", { method: "POST", body: { email } }),
  resetPassword: (token, newPassword) =>
    request("/api/auth/reset-password", { method: "POST", body: { token, newPassword } }),
  login: (email, password, rememberMe) =>
    request("/api/auth/login", { method: "POST", body: { email, password, rememberMe } }),
  refresh: () => request("/api/auth/refresh", { method: "POST" }),
  logout: () => request("/api/auth/logout", { method: "POST" }),
  sessions: (token) => request("/api/users/me/sessions", { token }),
  revokeSession: (id, token) => request(`/api/users/me/sessions/${id}`, { method: "DELETE", token }),
  revokeAllSessions: (token) => request("/api/users/me/sessions", { method: "DELETE", token }),
  changePassword: (currentPassword, newPassword, token) =>
    request("/api/users/me/change-password", { method: "POST", body: { currentPassword, newPassword }, token }),
  changeEmail: (newEmail, token) =>
    request("/api/users/me/change-email", { method: "POST", body: { newEmail }, token }),
};
