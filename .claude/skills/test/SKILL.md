---
name: test
description: >
  Run, debug, and fix failing tests. Can run full suite or single file.
  Trigger: "run tests", "why is this test failing", "fix tests", or invokes /test.
---

## Run modes

- `npm test` — full suite
- `npm test -- --testPathPattern=<file>` — single file
- `npm test -- --watch` — watch mode (background)

## Debug flow

1. Run failing test, capture output
2. Read the test file and the code it tests
3. Identify: wrong expectation vs broken implementation
4. Fix the implementation (not the test) unless test expectation is clearly wrong
5. Re-run to confirm green

## Rules

- Never change a test to make it pass unless the test is provably wrong
- Don't add `// eslint-disable` or `@ts-ignore` to silence test errors
- If test requires a mock, prefer the existing mock patterns in `__mocks__/`
