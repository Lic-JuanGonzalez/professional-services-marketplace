#!/usr/bin/env bash
# SessionStart hook — injects project context into Claude's system prompt.

BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "no-git")
DIRTY=$(git status --porcelain 2>/dev/null | wc -l | tr -d ' ')

# Detect language
LANG_HINT=""
[ -f "pom.xml" ] && LANG_HINT="Java/Maven"
[ -f "build.gradle" ] || [ -f "build.gradle.kts" ] && LANG_HINT="Java/Gradle"
[ -f "pyproject.toml" ] && LANG_HINT="${LANG_HINT:+$LANG_HINT + }Python/uv"
[ -f "requirements.txt" ] && LANG_HINT="${LANG_HINT:+$LANG_HINT + }Python/pip"

# Print context
cat <<EOF
=== SESSION CONTEXT ===
Branch: $BRANCH | Uncommitted: $DIRTY file(s) | Stack: ${LANG_HINT:-unknown}
Working dir: $(pwd)

=== MEMORY ===
$(cat .claude/memory/progress.md 2>/dev/null || echo "No progress file found.")
=======================
EOF
