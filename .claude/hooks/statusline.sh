#!/usr/bin/env bash
# Status line shown in Claude Code UI

BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "no-git")
DIRTY=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')
DIRTY_MARK=""
[ "$DIRTY" -gt 0 ] 2>/dev/null && DIRTY_MARK=" *${DIRTY}"

# Get last progress status
STATUS=$(grep -m1 "^## Status" -A1 .claude/memory/progress.md 2>/dev/null | tail -1 | tr -d '# ')
STATUS=${STATUS:-no-memory}

printf "%s%s | %s" "$BRANCH" "$DIRTY_MARK" "$STATUS"
