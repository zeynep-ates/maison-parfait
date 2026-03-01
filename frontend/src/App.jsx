import { SiteShell } from "./components/layout/SiteShell";
import { Home } from "./pages/Home";

export default function App() {
  return (
    <SiteShell>
      <Home />
    </SiteShell>
  );
}