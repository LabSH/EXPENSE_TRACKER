---
name: project-build-tool
description: expenseTracker uses Gradle (gradlew) — verified working command to check compilation
metadata:
  type: project
---

expenseTracker is a Gradle project (has `gradlew`/`gradlew.bat` at repo root, not Maven).
To verify Java changes compile after edits, run `./gradlew compileJava -q` from the repo
root via the Bash tool (Git Bash). This was confirmed working on 2026-08-03 after a full
convention audit of all 11 domains under `src/main/java/com/expenseTracker` — it compiled
cleanly (only a pre-existing deprecation note, no errors) after adding validation
annotations, Javadoc, and query alias fixes across ~20 files.

**How to apply:** Always run this compile check as the final verification step after
making Java edits in this repo, instead of assuming success from Edit tool not erroring.
