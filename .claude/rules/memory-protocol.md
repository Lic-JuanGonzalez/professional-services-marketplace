# Memory protocol

Claude MUST follow this protocol every session.

## Session start

1. Read `.claude/memory/MEMORY.md`
2. Read `.claude/memory/progress.md` — resume from last state
3. Read `.claude/memory/project.md` — load decisions and known issues
4. State in first response: what was in progress, what the next step is

## During session

Update `.claude/memory/progress.md` after:
- Any file edit
- Any test run (pass or fail)
- Any decision made (architecture, approach)
- Any direction change
- Hitting a blocker

Keep `progress.md` current. Stale = useless.

## Saving to memory

Save to `project.md` when:
- Architecture decision made — record WHY, not just what
- Bug found with non-obvious root cause
- External dependency or constraint discovered
- Tech debt intentionally created

Save to `feedback.md` when:
- User corrects an approach ("don't do X")
- User confirms a non-obvious choice worked ("yes, exactly that")

Save to `user.md` when:
- User reveals preference about how to work
- User reveals experience level or domain knowledge

## Session end

Write final `progress.md` snapshot:
- Exactly what was done
- Exactly what's next (specific file:line if possible)
- Any blockers
- Files modified this session

## Never lose

- What task was being worked on
- Which files were modified
- What the next concrete step is
- Why architectural decisions were made
