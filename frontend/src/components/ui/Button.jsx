const variants = {
  primary: "bg-ink text-cream shadow-ambient hover:opacity-95",
  accent: "bg-berry text-cream shadow-ambient hover:opacity-95",
  ghost: "hairline-gold bg-cream/60 text-ink hover:bg-cream",
};

export function Button({ variant = "primary", loading = false, children, className = "", disabled, ...props }) {
  return (
    <button
      className={`h-11 px-7 rounded-full text-[13px] tracking-wide transition disabled:opacity-60 disabled:cursor-not-allowed ${variants[variant]} ${className}`}
      disabled={loading || disabled}
      {...props}
    >
      {loading ? "…" : children}
    </button>
  );
}
