import { useCallback, useRef, useState } from "react";
import { ComingSoonContext } from "./coming-soon-context";

export function ComingSoonProvider({ children }) {
  const [message, setMessage] = useState(null);
  const timeoutRef = useRef(null);

  const notify = useCallback((label, { raw = false } = {}) => {
    setMessage(raw ? label : `${label} is coming soon.`);
    clearTimeout(timeoutRef.current);
    timeoutRef.current = setTimeout(() => setMessage(null), 2400);
  }, []);

  return (
    <ComingSoonContext.Provider value={notify}>
      {children}
      <div
        aria-live="polite"
        className={`pointer-events-none fixed inset-x-0 bottom-6 z-50 flex justify-center px-5 transition-all duration-300 ${
          message ? "opacity-100 translate-y-0" : "opacity-0 translate-y-2"
        }`}
      >
        {message && (
          <div className="hairline-gold bg-ink text-cream text-[13px] tracking-wide px-5 py-3 rounded-full shadow-ambient">
            {message}
          </div>
        )}
      </div>
    </ComingSoonContext.Provider>
  );
}
