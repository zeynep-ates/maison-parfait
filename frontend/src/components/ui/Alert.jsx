export function Alert({ variant = "error", children }) {
  if (!children) return null;
  const styles =
    variant === "error"
      ? "border-berry/40 bg-berry/5 text-berry"
      : "border-gold/40 bg-gold/10 text-ink";
  return (
    <div className={`mb-5 rounded-[10px] border px-4 py-3 text-[13px] leading-5 ${styles}`}>{children}</div>
  );
}
