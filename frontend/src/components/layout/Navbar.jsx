import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/auth-context";
import { useComingSoon } from "../../context/coming-soon-context";
import { useCart } from "../../context/cart-context";
import { BagIcon, CloseIcon, HeartIcon, MenuIcon, SearchIcon } from "../ui/icons";

const navLinks = [
  { label: "Menu", hash: "#menu" },
  { label: "Collections", hash: "#collections" },
  { label: "About", hash: "#about" },
  { label: "Contact", hash: "#contact" },
];

export function Navbar() {
  const { isAuthenticated, user } = useAuth();
  const notify = useComingSoon();
  const { count } = useCart();
  const [open, setOpen] = useState(false);

  const accountLabel = isAuthenticated ? user?.fullName?.split(" ")[0] || "Account" : "Sign in";
  const accountTo = isAuthenticated ? "/account" : "/login";

  return (
    <header className="sticky top-0 z-30 backdrop-blur bg-blush/60 border-b border-gold/25">
      <div className="mx-auto max-w-6xl px-5 py-4 flex items-center justify-between gap-4">
        <Link to="/" className="flex items-baseline gap-2 shrink-0" onClick={() => setOpen(false)}>
          <span className="font-serif text-[20px] sm:text-[22px] tracking-tight">Maison Parfait</span>
          <span className="hidden sm:inline text-gold text-[14px] italic">patisserie</span>
        </Link>

        <nav className="hidden md:flex items-center gap-7 text-[13px] tracking-wide text-cocoa">
          {navLinks.map((l) => (
            <Link key={l.hash} className="hover:text-ink" to={`/${l.hash}`}>
              {l.label}
            </Link>
          ))}
        </nav>

        <div className="hidden md:flex items-center gap-1">
          <button
            type="button"
            aria-label="Search"
            onClick={() => notify("Search")}
            className="h-10 w-10 inline-flex items-center justify-center rounded-full text-cocoa hover:text-ink hover:bg-cream/60"
          >
            <SearchIcon className="h-[18px] w-[18px]" />
          </button>
          <button
            type="button"
            aria-label="Wishlist"
            onClick={() => notify("Wishlist")}
            className="h-10 w-10 inline-flex items-center justify-center rounded-full text-cocoa hover:text-ink hover:bg-cream/60"
          >
            <HeartIcon className="h-[18px] w-[18px]" />
          </button>
          <button
            type="button"
            aria-label="Cart"
            onClick={() => notify("Cart")}
            className="relative h-10 w-10 inline-flex items-center justify-center rounded-full text-cocoa hover:text-ink hover:bg-cream/60"
          >
            <BagIcon className="h-[18px] w-[18px]" />
            {count > 0 && (
              <span className="absolute top-1 right-1 h-[15px] min-w-[15px] px-[3px] rounded-full bg-berry text-cream text-[9px] leading-[15px] text-center">
                {count > 9 ? "9+" : count}
              </span>
            )}
          </button>

          <Link to={accountTo} className="ml-2 text-[13px] tracking-wide text-cocoa hover:text-ink">
            {accountLabel}
          </Link>

          <button
            type="button"
            onClick={() => notify("Ordering")}
            className="ml-3 h-10 px-5 rounded-full bg-berry text-cream text-[13px] tracking-wide shadow-ambient hover:opacity-95"
          >
            Order Now
          </button>
        </div>

        <button
          type="button"
          aria-label={open ? "Close menu" : "Open menu"}
          aria-expanded={open}
          onClick={() => setOpen((v) => !v)}
          className="md:hidden h-10 w-10 inline-flex items-center justify-center rounded-full text-ink"
        >
          {open ? <CloseIcon className="h-5 w-5" /> : <MenuIcon className="h-5 w-5" />}
        </button>
      </div>

      {open && (
        <div className="md:hidden border-t border-gold/25 bg-cream/95">
          <nav className="mx-auto max-w-6xl px-5 py-5 flex flex-col gap-4 text-[14px] text-cocoa">
            {navLinks.map((l) => (
              <Link key={l.hash} to={`/${l.hash}`} className="hover:text-ink" onClick={() => setOpen(false)}>
                {l.label}
              </Link>
            ))}

            <div className="flex items-center gap-6 pt-4 border-t border-gold/20 text-[13px]">
              <button type="button" onClick={() => notify("Search")} className="hover:text-ink">
                Search
              </button>
              <button type="button" onClick={() => notify("Wishlist")} className="hover:text-ink">
                Wishlist
              </button>
              <button type="button" onClick={() => notify("Cart")} className="hover:text-ink">
                Cart{count > 0 ? ` (${count})` : ""}
              </button>
            </div>

            <Link to={accountTo} onClick={() => setOpen(false)} className="text-ink">
              {accountLabel}
            </Link>

            <button
              type="button"
              onClick={() => {
                notify("Ordering");
                setOpen(false);
              }}
              className="h-11 rounded-full bg-berry text-cream text-[13px] tracking-wide shadow-ambient"
            >
              Order Now
            </button>
          </nav>
        </div>
      )}
    </header>
  );
}
