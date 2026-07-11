import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { AuthCard } from "../components/ui/AuthCard";
import { api, ApiError } from "../lib/api";

export function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [status, setStatus] = useState(token ? "pending" : "error"); // pending | success | error
  const [error, setError] = useState(token ? "" : "This verification link is missing its token.");

  useEffect(() => {
    if (!token) return;
    api
      .verifyEmail(token)
      .then(() => setStatus("success"))
      .catch((err) => {
        setStatus("error");
        setError(err instanceof ApiError ? err.message : "Something went wrong. Please try again.");
      });
  }, [token]);

  return (
    <AuthCard kicker="Email verification" title={status === "success" ? "You're verified" : "Verifying your email"}>
      {status === "pending" && <p className="text-[14px] leading-6 text-cocoa">One moment…</p>}
      {status === "success" && (
        <>
          <p className="text-[14px] leading-6 text-cocoa">Your email address has been confirmed.</p>
          <Link
            to="/login"
            className="mt-7 inline-block text-[13px] tracking-wide text-berry hover:underline underline-offset-4"
          >
            Continue to sign in →
          </Link>
        </>
      )}
      {status === "error" && (
        <>
          <p className="text-[14px] leading-6 text-berry">{error}</p>
          <Link
            to="/login"
            className="mt-7 inline-block text-[13px] tracking-wide text-berry hover:underline underline-offset-4"
          >
            Back to sign in →
          </Link>
        </>
      )}
    </AuthCard>
  );
}
