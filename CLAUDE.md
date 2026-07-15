# CLAUDE.md

## Memory — READ FIRST, EVERY SESSION

Before doing anything, load memory:

```
.claude/memory/MEMORY.md     ← index of all memory files
.claude/memory/progress.md   ← what was in progress last session
.claude/memory/project.md    ← project state, decisions, context
```

After every significant action, update `.claude/memory/progress.md`.
On session end (or when switching tasks), write a full state snapshot.

---

## Project overview

Project: professional-services-marketplace

## Language + stack

**Primary languages:** Java, Python

| Layer         | Java                        | Python                      |
|---------------|-----------------------------|-----------------------------|
| Build         | Maven (`pom.xml`) / Gradle  | uv / Poetry (`pyproject.toml`) |
| Test          | JUnit 5, Mockito            | pytest, pytest-cov          |
| Lint          | Checkstyle, SpotBugs        | ruff, mypy                  |
| Runtime       | Java 21 (LTS)               | Python 3.12+                |

## Dev commands

### Java (Maven)
```bash
mvn compile                  # compile
mvn test                     # run tests
mvn package -DskipTests      # build jar
mvn checkstyle:check         # lint
mvn spotbugs:check           # static analysis
```

### Java (Gradle)
```bash
./gradlew build              # compile + test
./gradlew test               # tests only
./gradlew check              # lint + analysis
./gradlew run                # run app
```

### Python
```bash
uv run pytest                # tests
uv run pytest --cov          # tests + coverage
uv run ruff check .          # lint
uv run ruff format .         # format
uv run mypy .                # type check
uv run python -m <module>    # run module
```

## Architecture

<!-- Fill in per-project. Example: -->
<!-- src/main/java/com/app/   → Java source root -->
<!-- src/test/java/com/app/   → Java tests -->
<!-- src/                     → Python source -->
<!-- tests/                   → Python tests -->

## Rules

See `.claude/rules/` — loaded automatically.

## Critical constraints

<!-- Things Claude must never do. Examples: -->
<!-- - Never modify migration files once committed -->
<!-- - Never call external APIs in tests — use mocks -->
<!-- - Never change pom.xml version manually — use Maven release plugin -->
