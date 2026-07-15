# Postman collection

- `marketplace-api-collection.postman_collection.json` — 44 requests across Auth, Professionals, Services, Hires, Reviews. Each request has `pm.test` assertions; success responses auto-save ids/tokens into environment variables (`clientToken`, `professionalToken`, `professionalProfileId`, `serviceOfferingId`, `hireId`, etc.) so later requests chain off earlier ones.
- `marketplace-environment.postman_environment.json` — companion environment (`baseUrl` + the variables above, all empty until the collection runs).

## Import

Postman → Import → select both files. Select the **Marketplace - Local Dev** environment (top-right dropdown) before running anything — the test scripts write to it via `pm.environment.set`.

## Run order

Folders run top to bottom via the Collection Runner (Auth → Professionals → Services → Hires → Reviews); later requests depend on ids saved by earlier ones in the same run.

## Prerequisites

Backend must be up first:

```bash
cd ../..
docker compose up -d --build
```

Default `baseUrl` is `http://localhost:8082/api`.

## Newman (CLI)

```bash
npx newman run marketplace-api-collection.postman_collection.json \
  -e marketplace-environment.postman_environment.json
```
