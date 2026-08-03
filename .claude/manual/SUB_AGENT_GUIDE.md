# 서브에이전트(Sub-agents) 작성 양식

## 1. 만드는 방법

`/agents` 인터랙티브 마법사는 제거됐다. 대신 방법은 두 가지뿐이다.

1. **Claude에게 직접 요청** — "~하는 서브에이전트를 만들어줘" 하면 Claude가 `.md` 파일을 작성해줌
2. **파일 직접 작성/수정** — 아래 경로에 `.md` 파일을 두면 Claude Code가 자동으로 인식

| 범위 | 경로 |
|---|---|
| 프로젝트 전용 | `.claude/agents/` |
| 개인 전체(PC 공용) | `~/.claude/agents/` |

## 2. frontmatter 필드

| 필드 | 필수 여부 | 용도 |
|---|----|---|
| `name` | 필수 | 에이전트 이름 |
| `description` | 필수 | 언제 이 에이전트에게 위임해야 하는지 설명 |
| `tools` | 선택 | 사용 가능한 도구를 콤마로 나열 (`Read, Grep, Glob, Edit, Write`). 생략하면 전체 도구 상속 |
| `disallowedTools` | 선택 | `tools`로 상속받은 도구 중 명시적으로 차단할 도구 나열 — 넓게 상속시키되 특정 위험 도구만 막고 싶을 때 |
| `model` | 선택 | 사용할 모델 (`sonnet`, `opus`, `haiku` 등) |
| `color` | 선택 | 표시 색상 (`red`, `blue`, `green`, `yellow`, `purple` 등) |
| `skills` | 선택 | 이 에이전트가 참조할 스킬 목록 |
| `memory` | 선택 | 세션 간 기억 유지 범위 — `project`(`.claude/agent-memory/`, git 공유 가능) / `user`(`~/.claude/agent-memory/`, PC 전체 공유) / `local`(`.claude/agent-memory-local/`, gitignore 대상). 생략하면 메모리 없음 |
| `isolation` | 선택 | `worktree`로 설정하면 격리된 git worktree에서 실행 — 여러 에이전트가 동시에 파일을 건드려도 충돌 안 남 |
| `effort` | 선택 | 추론 강도 오버라이드 (`low`, `medium`, `high`, `xhigh`, `max`) — 단순 작업엔 낮게 줘서 비용 절감 |

## 3. 기본 구조 양식

```markdown
---
name: 에이전트이름
description: 언제 이 에이전트에게 위임해야 하는지 설명
tools: Read, Grep, Glob, Edit, Write
disallowedTools: Bash
model: sonnet
color: blue
skills:
  - 관련스킬이름
memory: project
isolation: worktree
effort: medium
---

에이전트 역할과 수행 지침을 여기 마크다운으로 작성
```

`name`/`description` 외엔 전부 선택 필드. 이 프로젝트의 실제 에이전트 파일들(`agents/*.md`)은
`tools`, `model`, `color`, `skills`까지만 쓰고 있고, `disallowedTools`/`memory`/`isolation`/`effort`는
아직 아무도 안 쓰는 상태 (필요할 때 골라서 추가하면 됨).

## 4. 주의사항

- `tools:` 필드를 아예 안 쓰면 **모든 도구를 상속** — 위험한 작업을 시킬 에이전트라면 오히려 명시적으로 좁혀서 써야 함
- 새로 만들거나 수정한 에이전트도 스킬과 마찬가지로 **현재 세션엔 바로 반영 안 될 수 있음** — 새 세션에서 확인할 것
