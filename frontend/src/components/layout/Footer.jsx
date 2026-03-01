export function Footer() {
  return (
    <footer id="contact" className="border-t border-gold/25 bg-cream/45">
      <div className="mx-auto max-w-6xl px-5 py-14 grid md:grid-cols-12 gap-10">
        <div className="md:col-span-5">
          <div className="flex items-baseline gap-2">
            <span className="font-serif text-[20px]">Maison Parfait</span>
            <span className="text-gold text-[13px] italic">patisserie</span>
          </div>
          <p className="mt-3 text-[13px] leading-6 text-cocoa max-w-sm">
            Crafted desserts with a soft, luxurious touch — designed for memorable moments.
          </p>
          <div className="mt-6 motif-divider w-52" />
          <p className="mt-4 text-[12px] text-cocoa">
            © {new Date().getFullYear()} Maison Parfait. All rights reserved.
          </p>
        </div>

        <div className="md:col-span-3">
          <p className="text-[12px] tracking-[0.25em] uppercase text-cocoa">Explore</p>
          <ul className="mt-4 space-y-2 text-[13px] text-cocoa">
            <li><a className="hover:text-ink" href="#menu">Menu</a></li>
            <li><a className="hover:text-ink" href="#collections">Collections</a></li>
            <li><a className="hover:text-ink" href="#about">About</a></li>
          </ul>
        </div>

        <div className="md:col-span-4">
          <p className="text-[12px] tracking-[0.25em] uppercase text-cocoa">Contact</p>
          <div className="mt-4 text-[13px] text-cocoa space-y-2">
            <p>Istanbul, TR</p>
            <p>hello@maisonparfait.com</p>
            <p>+90 (5xx) xxx xx xx</p>
          </div>

          <div className="mt-6 flex gap-3">
            {["Instagram", "Pinterest", "Facebook"].map((s) => (
              <button
                key={s}
                className="h-10 px-4 rounded-full hairline-gold bg-cream/60 text-[13px] text-cocoa hover:bg-cream"
              >
                {s}
              </button>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}