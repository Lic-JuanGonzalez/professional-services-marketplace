#!/usr/bin/env node
// PostToolUse hook — fires after any tool call.
// Example: log tool usage or send a desktop notification on completion.

const input = JSON.parse(require("fs").readFileSync("/dev/stdin", "utf8"));
const toolName = input?.tool_name || "unknown";
const exitCode = input?.tool_response?.exit_code;

// Example: notify on failed bash commands
if (toolName === "Bash" && exitCode !== 0) {
  // Could trigger: notify-send, osascript, etc.
  process.stderr.write(`[hook] Bash failed (exit ${exitCode})\n`);
}

// Always pass through — no blocking in PostToolUse
console.log(JSON.stringify({ decision: "allow" }));
