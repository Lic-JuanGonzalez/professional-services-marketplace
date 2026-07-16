import { useState } from "react";
import { apiFetch } from "./api";

export function useApiRunner() {
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);

  const run = async (path, options) => {
    setLoading(true);
    try {
      const response = await apiFetch(path, options);
      setResult(response);
      return response;
    } finally {
      setLoading(false);
    }
  };

  return { result, loading, run };
}
