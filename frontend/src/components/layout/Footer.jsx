import { Link } from "react-router-dom";
import { useComingSoon } from "../../context/coming-soon-context";

export function Footer() {
  const notify = useComingSoon();

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
            <li>
              <Link className="hover:text-ink" to="/#menu">
                Menu
              </Link>
            </li>
            <li>
              <Link className="hover:text-ink" to="/#collections">
                Collections
              </Link>
            </li>
            <li>
              <Link className="hover:text-ink" to="/#about">
                About
              </Link>
            </li>
            <li>
              <button type="button" onClick={() => notify("Wishlist")} className="hover:text-ink">
                Wishlist
              </button>
            </li>
            <li>
              <button type="button" onClick={() => notify("Cart")} className="hover:text-ink">
                Cart
              </button>
            </li>
          </ul>
        </div>

        <div className="md:col-span-4">
          <p className="text-[12px] tracking-[0.25em] uppercase text-cocoa">Contact</p>
          <div className="mt-4 text-[13px] text-cocoa space-y-2">
            <p>Istanbul, TR</p>
            <p>hello@maisonparfait.com</p>
            <p>+90 (5xx) xxx xx xx</p>
          </div>

          <button
            type="button"
            onClick={() => notify("Order tracking")}
            className="mt-4 text-[13px] tracking-wide text-berry hover:underline underline-offset-4"
          >
            Track your order →
          </button>

          <div className="mt-6 flex gap-3">
            {["Instagram", "Pinterest", "Facebook"].map((s) => (
              <button
                key={s}
                type="button"
                onClick={() => notify(s)}
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
