export function Navbar() {
  return (
    <header className="sticky top-0 z-30 backdrop-blur bg-blush/60 border-b border-gold/25">
      <div className="mx-auto max-w-6xl px-5 py-4 flex items-center justify-between">
        <a href="#home" className="flex items-baseline gap-2">
          <span className="font-serif text-[22px] tracking-tight">Maison Parfait</span>
          <span className="text-gold text-[14px] italic">patisserie</span>
        </a>

        <nav className="hidden md:flex items-center gap-7 text-[13px] tracking-wide text-cocoa">
          <a className="hover:text-ink" href="#menu">Menu</a>
          <a className="hover:text-ink" href="#collections">Collections</a>
          <a className="hover:text-ink" href="#about">About</a>
          <a className="hover:text-ink" href="#contact">Contact</a>
        </nav>

        <div className="flex items-center gap-3">
          <button className="h-10 px-5 rounded-full bg-berry text-cream text-[13px] tracking-wide shadow-ambient hover:opacity-95">
            Order Now
          </button>
        </div>
      </div>
    </header>
  );
}