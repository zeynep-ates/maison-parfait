import { useState } from "react";
import { Link } from "react-router-dom";
import { AuthCard } from "../components/ui/AuthCard";
import { TextField } from "../components/ui/TextField";
import { Button } from "../components/ui/Button";
import { Alert } from "../components/ui/Alert";
import { api, ApiError } from "../lib/api";
import { fieldFromMessage } from "../lib/formErrors";

const FIELD_MAP = [
  ["password", "password"],
  ["email", "email"],
  ["name", "fullName"],
];

export function Register() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [fieldError, setFieldError] = useState({ field: null, message: "" });
  const [loading, setLoading] = useState(false);
  const [submittedEmail, setSubmittedEmail] = useState(null);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setFieldError({ field: null, message: "" });
    setLoading(true);
    try {
      await api.register({ fullName, email, password });
      setSubmittedEmail(email);
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

  if (submittedEmail) {
    return (
      <AuthCard kicker="Almost there" title="Check your inbox">
        <p className="text-[14px] leading-6 text-cocoa">
          We&apos;ve sent a verification link to <span className="text-ink">{submittedEmail}</span>. Follow it to
          activate your account, then sign in.
        </p>
        <Link
          to="/login"
          className="mt-7 inline-block text-[13px] tracking-wide text-berry hover:underline underline-offset-4"
        >
          Back to sign in →
        </Link>
      </AuthCard>
    );
  }

  return (
    <AuthCard kicker="Join us" title="Create your account">
      <Alert>{error}</Alert>
      <form className="space-y-5" onSubmit={handleSubmit}>
        <TextField
          label="Full name"
          type="text"
          autoComplete="name"
          required
          maxLength={150}
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          error={fieldError.field === "fullName" ? fieldError.message : undefined}
        />
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
          autoComplete="new-password"
          required
          minLength={8}
          maxLength={72}
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldError.field === "password" ? fieldError.message : undefined}
        />
        <Button type="submit" variant="accent" loading={loading} className="w-full">
          Create account
        </Button>
      </form>
      <p className="mt-7 text-center text-[13px] text-cocoa">
        Already have an account?{" "}
        <Link to="/login" className="text-berry hover:underline underline-offset-4">
          Sign in
        </Link>
      </p>
    </AuthCard>
  );
}
