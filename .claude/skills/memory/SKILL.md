---
name: memory
description: >
  Persistent context manager. Reads memory on session start, writes progress after
  every meaningful action. Prevents context loss between sessions.
  Trigger: "save progress", "what were we doing", "resume", "update memory",
  or on any session start.
---

## On session start

1. Read `.claude/memory/progress.md`
2. Read `.claude/memory/project.md`
3. State in first response (caveman-compressed):
   - What was in progress
   - What next step is
   - Any blockers

## After every meaningful action

Update `.claude/memory/progress.md`:

```markdown
## Last updated
<ISO timestamp>

## Status
in-progress

## What was being done
<one paragraph — enough to resume cold>

## Last action taken
<specific: edited X:line Y, ran `mvn test`, fixed NullPointerException in Z>

## Next step
<specific: implement method foo() in src/main/java/…/Bar.java:42>

## Blocked on
<empty or specific blocker>

## Files modified this session
- path/to/File.java
- path/to/module.py
```

## What counts as "meaningful"

- File edited
- Test run (any result)
- Build run
- Decision made (architecture, approach, workaround)
- Blocker hit
- Direction changed

## What to save to project.md

Architecture decision → reason → date.
Non-obvious bug root cause.
External constraint discovered.
Tech debt intentionally created.

## Caveman + memory

Memory files use normal prose (not caveman) — they must be readable cold.
Claude's responses to the user: caveman mode.
Progress/project/feedback files: clear sentences, full context.
