# 스킬(Skills) 작성 양식

## 1. 필수 조건

```
.claude/skills/스킬이름/SKILL.md
```

- 폴더명이 곧 호출 이름이 됨 (`skills/session-report/SKILL.md` → `/session-report`)
- 폴더 안에 스크립트/참고자료 등 부가 파일을 같이 둘 수 있음 (SKILL.md 본문에서 경로로 참조)
- SKILL.md 맨 위에 frontmatter(`---`로 감싼 부분) + 아래에 실행 지침(마크다운) 순서로 작성

**기본 구조 양식**

```markdown
---
name: 스킬이름
description: 이 스킬이 언제, 무엇을 위해 쓰이는지 설명
model: haiku
allowed-tools: Bash(git *) Read
argument-hint: [인자설명]
---

# 스킬 제목

## 수행 절차

### 1단계: ...
### 2단계: ...

$ARGUMENTS
```

`model`/`allowed-tools`/`argument-hint`는 전부 선택 필드라 필요 없으면 지워도 됨 (3번 표 참고).

## 2. 호출 방법

| 방식 | 트리거 |
|---|---|
| 수동 호출 | 사용자가 `/스킬이름` 직접 입력 |
| 자동 호출 | Claude가 대화 맥락과 `description`을 비교해서 알아서 판단 후 실행 |
| 수동 전용으로 제한 | frontmatter에 `disable-model-invocation: true` 추가 (자동 호출만 차단, `/이름` 수동 호출은 그대로 가능) |
| 완전 비활성화 | `settings.json`에 `skillOverrides` 추가 (자동 호출도 `/이름` 수동 호출도 전부 차단) |

**완전 비활성화 예시** (`settings.json`)
```json
{
  "skillOverrides": {
    "스킬이름": "off"
  }
}
```

| 값 | 효과 |
|---|---|
| `"on"` (기본값) | 완전 활성화 |
| `"off"` | 완전 비활성화 (자동/수동 호출 다 차단) |
| `"name-only"` | Claude는 이름만 인지, 사용자는 수동 호출 가능 |
| `"user-invocable-only"` | Claude에게는 안 보임, 사용자는 수동 호출 가능 |

※ 플러그인이 제공하는 스킬에는 `skillOverrides`가 안 먹힘 — `/plugin`으로 따로 관리해야 함

## 3. frontmatter 필드

| 필드 | 필수 | 용도 |
|---|---|---|
| `name` | 권장 | 표시 이름 (보통 폴더명과 동일하게) |
| `description` | 권장 | 자동 호출 판단 기준 — 언제 쓰는지 구체적으로 적을수록 매칭 잘됨 |
| `model` | 선택 | 이 스킬만 다른 모델로 고정 (`haiku`, `sonnet`, `opus` 등) |
| `allowed-tools` | 선택 | 이 스킬 실행 중에만 도구 권한 부여 (`Bash(git *) Read` 형식) |
| `argument-hint` | 선택 | `/이름 [뒤에 올 인자]` 자동완성 힌트 표시용 |

## 4. 주의사항

- `/이름 인자` 형태로 호출하면 뒤에 붙인 텍스트가 SKILL.md 본문의 `$ARGUMENTS`로 치환됨. 본문에 `$ARGUMENTS`를 안 써두면 Claude Code가 알아서 맨 끝에 `ARGUMENTS: 입력값`을 덧붙임
- `description`을 대충 쓰면 자동 호출이 잘 안 됨 — "무엇을 하는지"보다 **"언제 쓰는지"**를 구체적으로 써야 함
- 스킬은 한 번 호출되면 그 세션 내내 컨텍스트에 남아있어서 매 턴 토큰 비용이 붙음 — 불필요하게 크게 쓰지 말 것
- SKILL.md를 새로 만들거나 수정해도 **현재 세션엔 바로 반영 안 되고 다음 세션부터 적용**됨 (실사용 확인됨)
