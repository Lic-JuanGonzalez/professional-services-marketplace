import { Navigate, Route, Routes } from "react-router-dom";
import "./prod.css";
import { NavBar } from "./components/NavBar";
import { RequireAuth } from "./components/RequireAuth";
import { HomePage } from "./pages/HomePage";
import { ProfessionalDetailPage } from "./pages/ProfessionalDetailPage";
import { LoginPage } from "./pages/LoginPage";
import { RegisterPage } from "./pages/RegisterPage";
import { MyHiresPage } from "./pages/MyHiresPage";
import { ProfessionalDashboardPage } from "./pages/ProfessionalDashboardPage";

export function ProdApp() {
  return (
    <>
      <NavBar />
      <div className="app">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/professionals/:id" element={<ProfessionalDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            path="/hires"
            element={
              <RequireAuth role="CLIENT">
                <MyHiresPage />
              </RequireAuth>
            }
          />
          <Route
            path="/dashboard"
            element={
              <RequireAuth role="PROFESSIONAL">
                <ProfessionalDashboardPage />
              </RequireAuth>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </>
  );
}
