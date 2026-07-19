import { Routes, Route } from "react-router-dom";
import { AdminGate } from "./test/AdminGate";
import { ProdApp } from "./prod/ProdApp";

export default function App() {
  return (
    <Routes>
      <Route path="/test/*" element={<AdminGate />} />
      <Route path="/*" element={<ProdApp />} />
    </Routes>
  );
}
