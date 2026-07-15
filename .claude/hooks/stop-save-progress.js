#!/usr/bin/env node
// Stop hook — reminds Claude to save progress before ending session.
// Claude Code fires this when the session is about to end.

const input = JSON.parse(require("fs").readFileSync("/dev/stdin", "utf8"));

// Only inject reminder if there was actual tool use this session
const toolsUsed = input?.session?.toolsUsed || 0;
if (toolsUsed === 0) {
  console.log(JSON.stringify({ decision: "allow" }));
  process.exit(0);
}

// Inject reminder into Claude's context before stopping
console.log(JSON.stringify({
  decision: "allow",
  reason: "REMINDER: Update .claude/memory/progress.md before ending. Write: what was done, next step (specific file:line), blockers, files modified.",
}));
