import { createContext, useContext } from "react";

export const ComingSoonContext = createContext(() => {});

export function useComingSoon() {
  return useContext(ComingSoonContext);
}
