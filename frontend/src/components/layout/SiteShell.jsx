import { Navbar } from "./Navbar";
import { Footer } from "./Footer";

export function SiteShell({ children }) {
  return (
    <div className="min-h-screen noise">
      <div className="w-full bg-cream/40 border-b border-gold/30">
        <div className="mx-auto max-w-6xl px-5 py-2 text-[12px] tracking-wide text-cocoa flex items-center justify-between">
          <span>Same-day delivery in Istanbul • Order before 2 PM</span>
          <span className="hidden sm:inline">Handcrafted • Small batches • Premium ingredients</span>
        </div>
      </div>

      <Navbar />
      <main>{children}</main>
      <Footer />
    </div>
  );
}