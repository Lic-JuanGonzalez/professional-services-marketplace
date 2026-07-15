---
name: deploy
description: >
  Deploy the project to a target environment (staging or prod).
  Runs tests, builds, checks git state, deploys, and verifies health.
  Trigger: user says "deploy", "ship it", "push to staging/prod", or invokes /deploy.
---

## Deploy flow

1. Run `npm test` — abort on failure, show failing tests
2. Run `npm run build` — abort on failure, show error
3. Check `git status` — warn if uncommitted changes, ask if continue
4. Identify target env from user message (staging default, prod requires confirmation)
5. Run `npm run deploy:<env>`
6. Wait for deploy, then hit health endpoint: `curl -s -o /dev/null -w "%{http_code}" <HEALTH_URL>`
7. Report: success (200) or failure with response body

## Prod gate

Before deploying prod:
- Show diff between current branch and last prod tag
- Explicitly ask: "Deploy X commits to prod?" — require yes/y to proceed

## On failure

Show exact error output. Suggest rollback command if deploy partially completed.
