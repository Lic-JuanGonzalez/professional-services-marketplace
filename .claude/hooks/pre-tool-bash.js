#!/usr/bin/env node
// PreToolUse hook for Bash commands.
// Blocks dangerous commands before they run.
// Claude Code passes tool input via stdin as JSON.

const input = JSON.parse(require("fs").readFileSync("/dev/stdin", "utf8"));
const cmd = input?.tool_input?.command || "";

const BLOCKED = [
  /rm\s+-rf\s+\//,          // rm -rf /
  /git push --force\s+\w/,   // force push to remote
  /DROP\s+TABLE/i,           // SQL drop table
  /truncate\s+/i,            // SQL truncate
];

const hit = BLOCKED.find((re) => re.test(cmd));

if (hit) {
  console.log(JSON.stringify({
    decision: "block",
    reason: `Blocked by pre-tool-bash hook: matched pattern ${hit}`,
  }));
  process.exit(0);
}

// Allow
console.log(JSON.stringify({ decision: "allow" }));
