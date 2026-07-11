export function AuthCard({ kicker, title, subtitle, children, width = "max-w-md" }) {
  return (
    <section className="mx-auto max-w-6xl px-5 py-14">
      <div className={`mx-auto ${width}`}>
        <div className="section-kicker">
          <p className="section-kicker__text text-[11px] text-cocoa">{kicker}</p>
          <span className="section-kicker__dot" />
          <span className="section-kicker__line" />
        </div>
        <h1 className="mt-6 font-serif text-[28px] tracking-tight text-ink">{title}</h1>
        {subtitle && <p className="mt-3 text-[14px] leading-6 text-cocoa">{subtitle}</p>}
        <div className="mt-8 hairline-gold bg-cream/60 rounded-panel shadow-ambient p-7 sm:p-9">{children}</div>
      </div>
    </section>
  );
}
