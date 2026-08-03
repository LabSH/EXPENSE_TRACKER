---
name: project-dto-naming-pattern
description: expenseTracker uses dto/ package + Request/Response suffixes everywhere, not model/ + Form/DTO as my own spec describes
metadata:
  type: project
---

The actual codebase (as of 2026-08-03, full audit of all 11 domains: admin, attachfile,
auth, code, common, fixedexpense, home, income, log, setting, user) consistently uses a
`{domain}/dto/` package with `Add{X}Request`, `Update{X}Request`, `{X}Response` class
naming in **every single domain** that has request/response payloads (code, log, income,
attachfile, fixedexpense, user). Only one class in the whole codebase (`UserJoinForm`)
follows the `model/` + `{Domain}Form` naming from my own convention doc.

**Why this matters:** This is not an isolated mistake — it's a uniform, deliberate,
project-wide convention that predates (or diverged from) my spec's `model/{Domain}Form`
/`{Domain}DTO` example. My spec's example assumes one Form/one DTO per domain, which
doesn't fit domains needing multiple CRUD payloads (Add/Update variants).

**How to apply:** Do NOT silently mass-rename `dto` → `model` or `Request/Response` →
`Form/DTO` across the codebase in a future audit. This was flagged as a Warning in the
2026-08-03 audit but deliberately NOT auto-executed because: (1) CLAUDE.md's "match
existing style, minimal changes" principle argues the established `dto`/`Request`/
`Response` pattern IS the real convention now, not the outlier; (2) the exact rename
mapping is genuinely ambiguous (multiple valid schemes); (3) scope is large (~25 classes,
6 domains) with real risk (e.g. `SysLogRepository`'s `@Query` has a JPQL constructor
expression referencing the FQCN as a string literal — renaming the class requires
updating that string too, easy to miss). If the user explicitly wants this rename done,
confirm the exact naming scheme first, then follow the reference-search → import update →
package update → move procedure per [[convention-validator]] agent spec.
