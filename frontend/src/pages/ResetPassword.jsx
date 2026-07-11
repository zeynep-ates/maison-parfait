import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { AuthCard } from "../components/ui/AuthCard";
import { TextField } from "../components/ui/TextField";
import { Button } from "../components/ui/Button";
import { Alert } from "../components/ui/Alert";
import { api, ApiError } from "../lib/api";
import { fieldFromMessage } from "../lib/formErrors";

const FIELD_MAP = [["password", "newPassword"]];

export function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const navigate = useNavigate();
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [fieldError, setFieldError] = useState({ field: null, message: "" });
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldError({ field: null, message: "" });
    if (newPassword !== confirmPassword) {
      setError("Passwords don't match.");
      return;
    }
    if (!token) {
      setError("This reset link is missing its token.");
      return;
    }
    setLoading(true);
    try {
      await api.resetPassword(token, newPassword);
      navigate("/login", { replace: true, state: { resetSuccess: true } });
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
    <AuthCard kicker="Password reset" title="Set a new password">
      <Alert>{error}</Alert>
      <form className="space-y-5" onSubmit={handleSubmit}>
        <TextField
          label="New password"
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={72}
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          error={fieldError.field === "newPassword" ? fieldError.message : undefined}
        />
        <TextField
          label="Confirm new password"
          type="password"
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={72}
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
        />
        <Button type="submit" variant="accent" loading={loading} className="w-full">
          Reset password
        </Button>
      </form>
      <p className="mt-7 text-center text-[13px] text-cocoa">
        <Link to="/login" className="text-berry hover:underline underline-offset-4">
          Back to sign in
        </Link>
      </p>
    </AuthCard>
  );
}
