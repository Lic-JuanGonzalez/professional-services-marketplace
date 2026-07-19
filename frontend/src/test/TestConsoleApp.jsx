import { useState } from "react";
import { Link } from "react-router-dom";
import { AuthPanel } from "../components/AuthPanel";
import { ProfessionalsPanel } from "../components/ProfessionalsPanel";
import { ServicesPanel } from "../components/ServicesPanel";
import { HiresPanel } from "../components/HiresPanel";
import { ReviewsPanel } from "../components/ReviewsPanel";
import { useAuth } from "../context/AuthContext";

const tabs = [
  { id: "auth", label: "Auth", Component: AuthPanel },
  { id: "professionals", label: "Professionals", Component: ProfessionalsPanel },
  { id: "services", label: "Services", Component: ServicesPanel },
  { id: "hires", label: "Hires", Component: HiresPanel },
  { id: "reviews", label: "Reviews", Component: ReviewsPanel },
];

export function TestConsoleApp() {
  const { logout } = useAuth();
  const [activeTab, setActiveTab] = useState(tabs[0].id);
  const ActivePanel = tabs.find((t) => t.id === activeTab).Component;

  return (
    <div className="app">
      <header className="app-header">
        <h1>Marketplace Test Console</h1>
        <p className="subtitle">
          Manual test UI for the Professional Services Marketplace API — every action here maps
          1:1 to a request in the Postman collection.
        </p>
        <p className="hint">
          <Link to="/">&larr; Back to site</Link>
          {" · "}
          <button type="button" className="btn-secondary" onClick={logout}>
            Log out of admin
          </button>
        </p>
      </header>

      <nav className="tabs" role="tablist">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            role="tab"
            aria-selected={activeTab === tab.id}
            className={`tab ${activeTab === tab.id ? "tab-active" : ""}`}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <main>
        <ActivePanel />
      </main>
    </div>
  );
}
