import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/auth-context";
import { TextField } from "../components/ui/TextField";
import { Button } from "../components/ui/Button";
import { Alert } from "../components/ui/Alert";
import { api, ApiError } from "../lib/api";
import { fieldFromMessage } from "../lib/formErrors";
import { describeUserAgent } from "../lib/userAgent";

const PASSWORD_FIELD_MAP = [
  ["current password", "currentPassword"],
  ["password", "newPassword"],
];
const EMAIL_FIELD_MAP = [["email", "newEmail"]];

function formatDate(value) {
  if (!value) return "—";
  return new Date(value).toLocaleString(undefined, { dateStyle: "medium", timeStyle: "short" });
}

function SectionCard({ kicker, title, children }) {
  return (
    <div className="hairline-gold bg-cream/60 rounded-panel shadow-ambient p-7 sm:p-9">
      <div className="section-kicker">
        <p className="section-kicker__text text-[11px] text-cocoa">{kicker}</p>
        <span className="section-kicker__dot" />
        <span className="section-kicker__line" />
      </div>
      <h2 className="mt-5 font-serif text-[20px] tracking-tight text-ink">{title}</h2>
      <div className="mt-6">{children}</div>
    </div>
  );
}

export function Account() {
  const { user, setUser, withAuth, logout } = useAuth();
  const navigate = useNavigate();

  const [sessions, setSessions] = useState([]);
  const [sessionsLoading, setSessionsLoading] = useState(true);
  const [sessionsError, setSessionsError] = useState("");

  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [passwordFieldError, setPasswordFieldError] = useState({ field: null, message: "" });
  const [passwordSuccess, setPasswordSuccess] = useState("");
  const [passwordLoading, setPasswordLoading] = useState(false);

  const [newEmail, setNewEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [emailFieldError, setEmailFieldError] = useState({ field: null, message: "" });
  const [emailSuccess, setEmailSuccess] = useState("");
  const [emailLoading, setEmailLoading] = useState(false);

  const loadSessions = useCallback(async () => {
    setSessionsLoading(true);
    setSessionsError("");
    try {
      const data = await withAuth((token) => api.sessions(token));
      setSessions(data || []);
    } catch (err) {
      setSessionsError(err instanceof ApiError ? err.message : "Couldn't load your sessions.");
    } finally {
      setSessionsLoading(false);
    }
  }, [withAuth]);

  useEffect(() => {
    loadSessions();
  }, [loadSessions]);

  async function handleChangePassword(e) {
    e.preventDefault();
    setPasswordError("");
    setPasswordFieldError({ field: null, message: "" });
    setPasswordSuccess("");
    setPasswordLoading(true);
    try {
      await withAuth((token) => api.changePassword(currentPassword, newPassword, token));
      setPasswordSuccess("Password updated. Your other sessions have been signed out.");
      setCurrentPassword("");
      setNewPassword("");
      loadSessions();
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Something went wrong. Please try again.";
      const field = fieldFromMessage(message, PASSWORD_FIELD_MAP);
      if (field) {
        setPasswordFieldError({ field, message });
      } else {
        setPasswordError(message);
      }
    } finally {
      setPasswordLoading(false);
    }
  }

  async function handleChangeEmail(e) {
    e.preventDefault();
    setEmailError("");
    setEmailFieldError({ field: null, message: "" });
    setEmailSuccess("");
    setEmailLoading(true);
    try {
      await withAuth((token) => api.changeEmail(newEmail, token));
      setEmailSuccess("Check your new inbox for a confirmation link.");
      setUser((prev) => (prev ? { ...prev, pendingEmail: newEmail } : prev));
      setNewEmail("");
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Something went wrong. Please try again.";
      const field = fieldFromMessage(message, EMAIL_FIELD_MAP);
      if (field) {
        setEmailFieldError({ field, message });
      } else {
        setEmailError(message);
      }
    } finally {
      setEmailLoading(false);
    }
  }

  async function handleRevoke(id) {
    setSessionsError("");
    try {
      await withAuth((token) => api.revokeSession(id, token));
      setSessions((prev) => prev.filter((s) => s.id !== id));
    } catch (err) {
      setSessionsError(err instanceof ApiError ? err.message : "Couldn't revoke that session.");
    }
  }

  async function handleRevokeAll() {
    setSessionsError("");
    try {
      await withAuth((token) => api.revokeAllSessions(token));
      loadSessions();
    } catch (err) {
      setSessionsError(err instanceof ApiError ? err.message : "Couldn't revoke your other sessions.");
    }
  }

  async function handleLogout() {
    await logout();
    navigate("/", { replace: true });
  }

  if (!user) return null;

  return (
    <section className="mx-auto max-w-6xl px-5 py-14">
      <div className="flex items-end justify-between gap-6">
        <div>
          <div className="section-kicker">
            <p className="section-kicker__text text-[11px] text-cocoa">Your account</p>
            <span className="section-kicker__dot" />
            <span className="section-kicker__line" />
          </div>
          <h1 className="mt-6 font-serif text-[28px] tracking-tight text-ink">{user.fullName}</h1>
        </div>
        <Button variant="ghost" onClick={handleLogout}>
          Sign out
        </Button>
      </div>

      <div className="mt-10 grid lg:grid-cols-2 gap-6">
        <SectionCard kicker="Profile" title="Account details">
          <dl className="space-y-3 text-[14px]">
            <div className="flex items-center justify-between">
              <dt className="text-cocoa">Email</dt>
              <dd className="text-ink">{user.email}</dd>
            </div>
            {user.pendingEmail && (
              <div className="flex items-center justify-between">
                <dt className="text-cocoa">Pending email</dt>
                <dd className="text-ink">{user.pendingEmail}</dd>
              </div>
            )}
            <div className="flex items-center justify-between">
              <dt className="text-cocoa">Email verified</dt>
              <dd className="text-ink">{user.emailVerified ? "Yes" : "No"}</dd>
            </div>
          </dl>
        </SectionCard>

        <SectionCard kicker="Security" title="Change password">
          <Alert>{passwordError}</Alert>
          <Alert variant="success">{passwordSuccess}</Alert>
          <form className="space-y-4" onSubmit={handleChangePassword}>
            <TextField
              label="Current password"
              type="password"
              autoComplete="current-password"
              required
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              error={passwordFieldError.field === "currentPassword" ? passwordFieldError.message : undefined}
            />
            <TextField
              label="New password"
              type="password"
              autoComplete="new-password"
              required
              minLength={8}
              maxLength={72}
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              error={passwordFieldError.field === "newPassword" ? passwordFieldError.message : undefined}
            />
            <Button type="submit" loading={passwordLoading} className="w-full">
              Update password
            </Button>
          </form>
        </SectionCard>

        <SectionCard kicker="Security" title="Change email">
          <Alert>{emailError}</Alert>
          <Alert variant="success">{emailSuccess}</Alert>
          <form className="space-y-4" onSubmit={handleChangeEmail}>
            <TextField
              label="New email"
              type="email"
              autoComplete="email"
              required
              value={newEmail}
              onChange={(e) => setNewEmail(e.target.value)}
              error={emailFieldError.field === "newEmail" ? emailFieldError.message : undefined}
            />
            <Button type="submit" loading={emailLoading} className="w-full">
              Send confirmation
            </Button>
          </form>
        </SectionCard>

        <SectionCard kicker="Devices" title="Active sessions">
          <Alert>{sessionsError}</Alert>
          {sessionsLoading ? (
            <ul className="space-y-3">
              {[0, 1].map((i) => (
                <li key={i} className="animate-pulse rounded-[10px] border border-gold/25 bg-cream/50 px-4 py-3">
                  <div className="h-3 w-2/5 rounded-full bg-gold/25" />
                  <div className="mt-2.5 h-2.5 w-3/5 rounded-full bg-gold/15" />
                </li>
              ))}
            </ul>
          ) : sessions.length === 0 ? (
            <p className="text-[13px] text-cocoa">You're not signed in anywhere else right now.</p>
          ) : (
            <ul className="space-y-3">
              {sessions.map((s) => (
                <li
                  key={s.id}
                  className="flex items-center justify-between gap-4 rounded-[10px] border border-gold/25 bg-cream/50 px-4 py-3"
                >
                  <div className="min-w-0">
                    <p className="text-[13px] text-ink truncate" title={s.userAgent || undefined}>
                      {describeUserAgent(s.userAgent)}
                    </p>
                    <p className="mt-0.5 text-[12px] text-cocoa">
                      {s.ipAddress || "Unknown IP"} • last used {formatDate(s.lastUsedAt)}
                      {s.current && <span className="ml-2 text-berry">This device</span>}
                    </p>
                  </div>
                  {!s.current && (
                    <button
                      onClick={() => handleRevoke(s.id)}
                      className="shrink-0 text-[12px] tracking-wide text-berry hover:underline underline-offset-4"
                    >
                      Revoke
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
          {sessions.some((s) => !s.current) && (
            <Button variant="ghost" className="mt-6 w-full" onClick={handleRevokeAll}>
              Sign out all other devices
            </Button>
          )}
        </SectionCard>
      </div>
    </section>
  );
}
