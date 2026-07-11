export function TextField({ label, error, className = "", ...props }) {
  return (
    <label className={`block ${className}`}>
      {label && (
        <span className="block text-[12px] tracking-[0.18em] uppercase text-cocoa mb-2">
          {label}
        </span>
      )}
      <input
        className="w-full h-11 px-4 rounded-[10px] hairline-gold bg-cream/60 text-[14px] text-ink placeholder:text-cocoa/60 focus:outline-none focus:ring-1 focus:ring-gold"
        {...props}
      />
      {error && <span className="block mt-1.5 text-[12px] text-berry">{error}</span>}
    </label>
  );
}
