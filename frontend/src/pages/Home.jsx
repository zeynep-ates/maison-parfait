import { useCallback, useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import { useComingSoon } from "../context/coming-soon-context";
import { useCart } from "../context/cart-context";
import { ChevronLeftIcon, ChevronRightIcon, HeartIcon } from "../components/ui/icons";
import { formatPrice } from "../lib/currency";

import p1 from "../assets/images/products/entremets-fraises-pistache.jpg";
import p2 from "../assets/images/products/macaronsframboise.jpg";
import p3 from "../assets/images/products/Pistachio-raspberry-and-basil-Paris-Brest.jpg";
import p4 from "../assets/images/products/recette-charlotte-aux-fraises.jpg";
import p5 from "../assets/images/products/tarte-citron-basilic.jpg";

import c1 from "../assets/images/categories/Entremets.jpg";
import c2 from "../assets/images/categories/Macaron.jpg";
import c3 from "../assets/images/categories/tart.jpg";
import c4 from "../assets/images/categories/viennoiserie.jpg";

const featuredProducts = [
  { id: 1, name: "Pistachio Raspberry Paris-Brest", price: 315, tag: "Bestseller", image: p3 },
  { id: 2, name: "Tarte Citron Basil", price: 295, tag: "Seasonal", image: p5 },
  { id: 3, name: "Fraise Pistache Entremet", price: 375, tag: "Limited", image: p1 },
  { id: 4, name: "Framboise Rose Macaron Box (6 pcs)", price: 520, tag: "Gift-ready", image: p2 },
  { id: 5, name: "Charlotte aux Fraises", price: 345, tag: "Classic", image: p4 },
];

const collections = [
  { key: "viennoiserie", title: "Viennoiserie", desc: "Buttery, freshly baked French breakfast pastries crafted daily.", image: c4 },
  { key: "signature", title: "Signature Entremets", desc: "Elegant modern mousse cakes designed for refined indulgence.", image: c1 },
  { key: "classics", title: "French Classics", desc: "Timeless Parisian pastries reimagined with artisanal precision.", image: c3 },
  { key: "maccarons", title: "Macarons & Petit Fours", desc: "Delicate, giftable confections made for sophisticated moments.", image: c2 },
];

const linkClass = "text-[13px] tracking-wide text-berry hover:underline underline-offset-4";

export function Home() {
  const location = useLocation();
  const notify = useComingSoon();
  const { addItem } = useCart();
  const scrollRef = useRef(null);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  useEffect(() => {
    if (!location.hash) return;
    const id = location.hash.slice(1);
    const raf = requestAnimationFrame(() => {
      document.getElementById(id)?.scrollIntoView({ behavior: "smooth", block: "start" });
    });
    return () => cancelAnimationFrame(raf);
  }, [location.hash]);

  const updateScrollState = useCallback(() => {
    const el = scrollRef.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 4);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 4);
  }, []);

  useEffect(() => {
    updateScrollState();
    const el = scrollRef.current;
    if (!el) return;
    el.addEventListener("scroll", updateScrollState, { passive: true });
    window.addEventListener("resize", updateScrollState);
    return () => {
      el.removeEventListener("scroll", updateScrollState);
      window.removeEventListener("resize", updateScrollState);
    };
  }, [updateScrollState]);

  function scrollByCards(direction) {
    const el = scrollRef.current;
    if (!el) return;
    el.scrollBy({ left: el.clientWidth * 0.8 * direction, behavior: "smooth" });
  }

  return (
    <div id="home">
      {/* HERO — compact (square visual) */}
      <section className="mx-auto max-w-6xl px-5 pt-10 pb-10">
        <div className="grid lg:grid-cols-12 gap-8 items-center">
          <div className="lg:col-span-6 text-center lg:text-left">
            <p className="text-[11px] tracking-[0.32em] uppercase text-cocoa">
              Soft luxury patisserie • Istanbul
            </p>

            <h1 className="mt-6 font-serif text-[40px] leading-[1.06] tracking-tight text-ink">
              Sweet, refined, unforgettable.
            </h1>

            <p className="mt-4 text-[15px] leading-7 text-cocoa max-w-md mx-auto lg:mx-0">
              Paris-inspired desserts crafted in small batches — delicate finishes,
              balanced sweetness, and premium presentation.
            </p>

            <div className="mt-7 flex flex-wrap items-center gap-4 justify-center lg:justify-start">
              <a
                href="#menu"
                className="h-11 px-7 rounded-full bg-ink text-cream text-[13px] tracking-wide shadow-ambient hover:opacity-95 inline-flex items-center"
              >
                Explore Menu
              </a>
              <a href="#collections" className={linkClass}>
                View Collections →
              </a>
            </div>

            {/* Micro trust row (compact, not tall cards) */}
            <div className="mt-8 grid grid-cols-3 gap-3 max-w-md mx-auto lg:mx-0">
              {[
                ["4.9", "Rating"],
                ["Same-day", "Select items"],
                ["Gift-ready", "Packaging"],
              ].map(([k, v]) => (
                <div key={k} className="hairline-gold bg-cream/55 rounded-panel px-4 py-3">
                  <div className="font-serif text-[16px] leading-5">{k}</div>
                  <div className="text-[12px] text-cocoa mt-1">{v}</div>
                </div>
              ))}
            </div>
          </div>

          <div className="lg:col-span-6">
            <div className="hairline-gold bg-cream/60 rounded-panel shadow-ambient p-5">
              {/* Square hero image (1:1) but not too tall */}
              <div className="relative overflow-hidden rounded-[10px] border border-gold/25">
                <img src={p1} alt="Fraise Pistache Entremet" className="w-full aspect-square object-cover" />
                <div className="absolute left-4 bottom-4 hairline-gold bg-cream/85 backdrop-blur rounded-[10px] px-4 py-3">
                  <p className="text-[10px] tracking-[0.30em] uppercase text-cocoa">This week</p>
                  <p className="font-serif text-[16px] leading-5">Seasonal Edit</p>
                </div>
              </div>

              <div className="mt-4 flex items-center justify-between">
                <div>
                  <p className="text-[11px] tracking-[0.28em] uppercase text-cocoa">Signature</p>
                  <p className="font-serif text-[18px]">Fraise Pistache Entremet</p>
                </div>
                <a href="#menu" className={linkClass}>
                  View details →
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* FEATURED ROW — horizontal scroll (mobile friendly) */}
      <section id="menu" className="mx-auto max-w-6xl px-5 py-10">
        <div className="flex items-end justify-between gap-6">
          <div>
            <div className="section-kicker">
              <p className="section-kicker__text text-[11px] text-cocoa">Featured</p>
              <span className="section-kicker__dot" />
              <span className="section-kicker__line" />
            </div>
            <h2 className="mt-6 font-serif text-[26px] tracking-tight">A curated selection</h2>
          </div>

          <button type="button" onClick={() => notify("Full menu")} className={`hidden sm:inline ${linkClass}`}>
            View all →
          </button>
        </div>

        <div className="relative mt-7 -mx-5">
          <div
            ref={scrollRef}
            className="px-5 flex gap-5 overflow-x-auto pb-3 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
          >
            {featuredProducts.map((p) => (
              <article
                key={p.id}
                className="min-w-[230px] sm:min-w-[260px] hairline-gold bg-cream/60 rounded-panel overflow-hidden group transition-shadow hover:shadow-ambient flex flex-col"
              >
                <div className="relative border-b border-gold/25">
                  <button
                    type="button"
                    onClick={() => notify(`${p.name} — full details coming soon.`, { raw: true })}
                    className="block w-full appearance-none border-0 bg-transparent p-0 text-left"
                  >
                    <img
                      src={p.image}
                      alt={p.name}
                      className="w-full aspect-square object-cover transition-transform duration-300 group-hover:scale-[1.01]"
                    />
                  </button>
                  <div className="absolute left-3 top-3 px-3 py-1 rounded-full bg-cream/85 border border-gold/25 text-[10px] tracking-[0.25em] uppercase text-cocoa">
                    {p.tag}
                  </div>
                  <button
                    type="button"
                    aria-label="Add to wishlist"
                    onClick={(e) => {
                      e.stopPropagation();
                      notify("Wishlist");
                    }}
                    className="absolute right-3 top-3 h-8 w-8 inline-flex items-center justify-center rounded-full bg-cream/85 border border-gold/25 text-cocoa hover:text-berry"
                  >
                    <HeartIcon className="h-4 w-4" />
                  </button>
                </div>

                <div className="p-4 flex flex-col flex-1">
                  <button
                    type="button"
                    onClick={() => notify(`${p.name} — full details coming soon.`, { raw: true })}
                    className="appearance-none border-0 bg-transparent p-0 text-left font-serif text-[16px] leading-5 hover:text-berry"
                  >
                    {p.name}
                  </button>
                  <div className="mt-auto pt-2 flex items-center justify-between">
                    <p className="text-[13px] text-cocoa">{formatPrice(p.price)}</p>
                    <button
                      type="button"
                      onClick={() => {
                        addItem(p);
                        notify(`Added "${p.name}" to your cart.`, { raw: true });
                      }}
                      className="h-9 px-4 rounded-full bg-berry text-cream text-[12px] tracking-wide hover:opacity-95"
                    >
                      Add
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>

          {canScrollLeft && (
            <div className="pointer-events-none absolute inset-y-0 left-0 w-14 bg-gradient-to-r from-blush/90 to-transparent" />
          )}
          {canScrollRight && (
            <div className="pointer-events-none absolute inset-y-0 right-0 w-14 bg-gradient-to-l from-blush/90 to-transparent" />
          )}

          <button
            type="button"
            aria-label="Scroll left"
            onClick={() => scrollByCards(-1)}
            disabled={!canScrollLeft}
            className="hidden sm:inline-flex absolute left-1 top-1/2 -translate-y-1/2 h-9 w-9 items-center justify-center rounded-full hairline-gold bg-cream/90 text-ink shadow-ambient hover:bg-cream transition disabled:opacity-0 disabled:pointer-events-none"
          >
            <ChevronLeftIcon className="h-4 w-4" />
          </button>
          <button
            type="button"
            aria-label="Scroll right"
            onClick={() => scrollByCards(1)}
            disabled={!canScrollRight}
            className="hidden sm:inline-flex absolute right-1 top-1/2 -translate-y-1/2 h-9 w-9 items-center justify-center rounded-full hairline-gold bg-cream/90 text-ink shadow-ambient hover:bg-cream transition disabled:opacity-0 disabled:pointer-events-none"
          >
            <ChevronRightIcon className="h-4 w-4" />
          </button>
        </div>

        <div className="mt-2 sm:hidden">
          <button type="button" onClick={() => notify("Full menu")} className={linkClass}>
            View all →
          </button>
        </div>
      </section>

      {/* COLLECTIONS — compact grid with square imagery */}
      <section id="collections" className="mx-auto max-w-6xl px-5 py-10">
        <div className="flex items-end justify-between gap-6">
          <div>
            <div className="section-kicker">
              <p className="section-kicker__text text-[11px] text-cocoa">Collections</p>
              <span className="section-kicker__dot" />
              <span className="section-kicker__line" />
            </div>
            <h2 className="mt-6 font-serif text-[26px] tracking-tight">Shop by category</h2>
          </div>

          <button type="button" onClick={() => notify("Collections")} className={`hidden sm:inline ${linkClass}`}>
            Browse all →
          </button>
        </div>

        <div className="mt-7 grid sm:grid-cols-2 lg:grid-cols-4 gap-5">
          {collections.map((c) => (
            <article
              key={c.key}
              className="hairline-gold bg-cream/60 rounded-panel overflow-hidden group transition-shadow hover:shadow-ambient flex flex-col"
            >
              <img
                src={c.image}
                alt={c.title}
                className="w-full aspect-square object-cover border-b border-gold/25 transition-transform duration-300 group-hover:scale-[1.01]"
              />
              <div className="p-4 flex flex-col flex-1">
                <p className="font-serif text-[16px]">{c.title}</p>
                <p className="mt-2 text-[13px] leading-6 text-cocoa">{c.desc}</p>
                <button
                  type="button"
                  onClick={() => notify(c.title)}
                  className={`mt-auto pt-4 inline-block self-start ${linkClass}`}
                >
                  Explore →
                </button>
              </div>
            </article>
          ))}
        </div>

        <div className="mt-3 sm:hidden">
          <button type="button" onClick={() => notify("Collections")} className={linkClass}>
            Browse all →
          </button>
        </div>
      </section>

      {/* BRAND STORY — compact */}
      <section id="about" className="mx-auto max-w-6xl px-5 py-12">
        <div className="hairline-gold bg-cream/60 rounded-panel shadow-ambient p-7 sm:p-9">
          <div className="section-kicker">
            <p className="section-kicker__text text-[11px] text-cocoa">The Maison Philosophy</p>
            <span className="section-kicker__dot" />
            <span className="section-kicker__line" />
          </div>

          <h2 className="mt-6 font-serif text-[28px] leading-[1.15] tracking-tight text-ink">
            Restrained sweetness. Elegant finishes. Desserts designed to be remembered.
          </h2>

          <p className="mt-4 text-[14px] leading-7 text-cocoa max-w-2xl">
            We craft each piece with intention — balanced textures, delicate accents, and
            gift-ready presentation. Our edits change with the season, but the signature remains:
            refined, soft, and quietly luxurious.
          </p>

          <div className="mt-7 flex flex-wrap gap-3">
            {["Small-batch", "Seasonal edits", "Hand-finished", "Premium packaging"].map((t) => (
              <span
                key={t}
                className="px-4 py-2 rounded-full bg-cream/70 border border-gold/25 text-[12px] tracking-wide text-cocoa"
              >
                {t}
              </span>
            ))}
          </div>
        </div>
      </section>

      {/* SHORT BANNER — not tall */}
      <section className="py-10 bg-gradient-to-r from-rose/50 via-blush/40 to-berry/10 border-y border-gold/25">
        <div className="mx-auto max-w-6xl px-5 flex flex-col sm:flex-row items-center justify-between gap-6">
          <div>
            <p className="text-[11px] tracking-[0.32em] uppercase text-cocoa">Seasonal offer</p>
            <p className="mt-2 font-serif text-[22px]">Gift boxes, refined.</p>
            <p className="mt-1 text-[13px] text-cocoa">
              Limited editions with premium presentation — while stocks last.
            </p>
          </div>

          <div className="flex items-center gap-3">
            <span className="px-4 py-2 rounded-full bg-cream/70 border border-gold/25 text-[12px] tracking-wide">
              -20% OFF
            </span>
            <button
              type="button"
              onClick={() => notify("Shop")}
              className="h-11 px-7 rounded-full bg-berry text-cream text-[13px] tracking-wide shadow-ambient hover:opacity-95"
            >
              Shop Now
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
