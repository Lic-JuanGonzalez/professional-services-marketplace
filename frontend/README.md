# Marketplace Test Console

Minimal React + Vite frontend to manually exercise the backend API in a browser — not a product UI. One tab per feature module (Auth, Professionals, Services, Hires, Reviews), mirroring the Postman collection in `../docs/api/`. Every action shows the raw request/response so you can see exactly what the API returned.

## Run

Backend must be up first (`docker compose up -d --build` from the repo root).

```bash
npm install
npm run dev
```

Opens on `http://localhost:5173`. Defaults to `VITE_API_BASE_URL=http://localhost:8082/api` (copy `.env.example` to `.env` to override).

## Notes

- Session (JWT + role) is kept in `localStorage`, not cookies — logout clears it.
- No routing library: tabs are local state in `App.jsx`.
- No client-side validation beyond HTML5 `required`/`min`/`max` — the point is to see the API's actual responses (including validation errors), not to prevent bad requests.
