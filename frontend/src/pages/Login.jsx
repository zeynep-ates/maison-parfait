import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/auth-context";
import { AuthCard } from "../components/ui/AuthCard";
import { TextField } from "../components/ui/TextField";
import { Button } from "../components/ui/Button";
import { Alert } from "../components/ui/Alert";
import { ApiError } from "../lib/api";
import { fieldFromMessage } from "../lib/formErrors";

const FIELD_MAP = [
  ["password", "password"],
  ["email", "email"],
];

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState("");
  const [fieldError, setFieldError] = useState({ field: null, message: "" });
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldError({ field: null, message: "" });
    setLoading(true);
    try {
      await login(email, password, rememberMe);
      navigate(location.state?.from || "/account", { replace: true });
    } catch (err) {
      const message = err instanceof ApiError ? err.message : "Something went wrong. Please try again.";
      const field = fieldFromMessage(message, FIELD_MAP);
      if (field) {
        setFieldError({ field, message });
      } else {
        setError(message);
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard kicker="Welcome back" title="Sign in to your account">
      {!error && location.state?.resetSuccess && (
        <Alert variant="success">Your password has been reset. Sign in with your new password.</Alert>
      )}
      <Alert>{error}</Alert>
      <form className="space-y-5" onSubmit={handleSubmit}>
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          required
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={fieldError.field === "email" ? fieldError.message : undefined}
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldError.field === "password" ? fieldError.message : undefined}
        />
        <div className="flex items-center justify-between">
          <label className="flex items-center gap-2 text-[13px] text-cocoa">
            <input
              type="checkbox"
              className="h-4 w-4 rounded border-gold/50 accent-berry focus:ring-1 focus:ring-gold"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
            />
            Remember me
          </label>
          <Link to="/forgot-password" className="text-[13px] tracking-wide text-berry hover:underline underline-offset-4">
            Forgot password?
          </Link>
        </div>
        <Button type="submit" variant="accent" loading={loading} className="w-full">
          Sign in
        </Button>
      </form>
      <p className="mt-7 text-center text-[13px] text-cocoa">
        Don&apos;t have an account?{" "}
        <Link to="/register" className="text-berry hover:underline underline-offset-4">
          Create one
        </Link>
      </p>
    </AuthCard>
  );
}
