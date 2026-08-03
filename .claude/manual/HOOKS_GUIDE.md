# 훅(Hooks) 설정 양식

> 공식 문서: https://code.claude.com/docs/ko/hooks-guide

## 1. 필수 조건

```json
{
  "hooks": {
    "이벤트명": [
      { "matcher": "패턴", "hooks": [ { "type": "command", "command": "bash .claude/hooks/스크립트.sh" } ] }
    ]
  }
}
```

- 공식 키만 사용: `matcher`, `hooks`, `type`, `command`, `timeout`(선택). `description` 등 임의 필드는 넣지 않는다.
- 스크립트 안 `.claude/...` 경로는 프로젝트 루트 기준.
- 로그/출력에 파일 내용을 넣을 땐 `printf '%s' "$내용"` 형태로 (포맷 문자열에 직접 넣지 않기).

## 2. 이벤트 종류

**matcher는 대소문자를 구분한다** — 반드시 도구명 그대로 정확히 표기 (`Bash`, `Edit` O / `bash`, `edit` X).

| 이벤트 | matcher |
|---|---|
| `PreToolUse` | 필요 (도구 이름, 예: `"Bash"`, `"Edit\|Write"`) |
| `PostToolUse` | 필요 (도구 이름) |
| `UserPromptSubmit` | 상관없음 (써도 무시됨) |
| `Notification` | 상관없음 (써도 무시됨) |
| `Stop` | 상관없음 (써도 무시됨) |
| `SubagentStart` / `SubagentStop` | 상관없음 (써도 무시됨) |
| `PreCompact` | 상관없음 (써도 무시됨) |
| `SessionStart` | 필요 (`"startup"`, `"resume"`, `"clear"`, `"compact"`) |

> 표에 없는 이벤트를 쓰려면 최신 공식 문서(hooks reference)를 먼저 확인 — 이 목록은 실사용 검증된 것만 담음.

**`PreToolUse`/`PostToolUse` 매처용 도구명** (대소문자 정확히 일치, `|`로 여러 개 조합 가능)

- 파일/코드: `Read`, `Write`, `Edit`, `Glob`, `Grep`, `NotebookEdit`
- 실행: `Bash`, `PowerShell`
- 검색/웹: `WebFetch`, `WebSearch`
- 서브에이전트/워크플로우: `Agent`, `Workflow`
- 기타 자주 쓰는 것: `TaskCreate`/`TaskUpdate`/`TaskList`, `AskUserQuestion`, `Artifact`
- MCP 서버 도구: `mcp__서버명__도구명` (예: `"mcp__filesystem__.*"`으로 서버 전체 매칭)
- `MultiEdit`은 별도 도구 없음 — `Edit`에 통합됨 (settings.json에 남아있어도 무해하지만 실제로는 안 씀)

## 3. 이벤트 설명

**발생 시점**

| 이벤트 | 발생 시점 |
|---|---|
| `PreToolUse` | 도구 실행 직전 |
| `PostToolUse` | 도구 실행 직후 |
| `UserPromptSubmit` | 사용자가 프롬프트 제출 시 |
| `Notification` | Claude가 확인/질문 팝업 띄울 때 |
| `Stop` | 메인 응답 종료 시 |
| `SubagentStart` / `SubagentStop` | 서브에이전트 시작/종료 시 |
| `PreCompact` | 컨텍스트 압축 직전 |

**공통 필드** (이벤트 종류 상관없이 모든 stdin JSON에 항상 들어있음)

| 필드명 | 내용 |
|---|---|
| `session_id` | 현재 세션 고유 ID |
| `transcript_path` | 대화 기록(JSONL) 파일 경로 |
| `cwd` | 현재 작업 디렉토리 |
| `hook_event_name` | 어떤 이벤트로 실행됐는지 (`"PreToolUse"` 등) |

**stdin 필드 내용** (공통 필드 외에 이벤트별로 추가되는 것, `jq -r '.필드명'`으로 추출)

| 이벤트 | 필드명 | 내용 |
|---|---|---|
| `PreToolUse` | `tool_name` | 실행될 도구 이름 (예: `Bash`, `Edit`) |
| `PreToolUse` | `tool_input` | 그 도구에 전달되는 입력값 객체 (예: Edit면 `file_path`, `old_string`, `new_string`) |
| `PostToolUse` | `tool_name` | 방금 실행된 도구 이름 |
| `PostToolUse` | `tool_input` | 실행에 사용된 입력값 객체 |
| `UserPromptSubmit` | `prompt` | 사용자가 입력한 프롬프트 원문 텍스트 |
| `Notification` | `message` | 팝업에 표시되는 알림 문구 |
| `Notification` | `notification_type` | 알림 종류 (확인 요청/대기 등) |
| `Stop` | - | 공통 필드 외 별도 필드 없음 |
| `SubagentStart` / `SubagentStop` | `agent_type` | 실행된 서브에이전트 이름 (예: `Explore`, `impact-analyzer`) |
| `PreCompact` | `trigger` | 압축 방식: `manual`(수동 `/compact`) 또는 `auto`(자동 압축) |

## 4. 주의사항

- **exit code**: 0=정상, 2=차단(`PreToolUse`/`UserPromptSubmit`/`Stop`/`SubagentStop`만 실제로 막힘, 나머지는 이미 벌어진 뒤라 안 막힘). 로깅용 훅은 항상 `exit 0`. (지금 훅들은 전부 로깅용이라 안 쓰지만, 나중에 위험한 동작을 막는 훅을 만들 때 필요해서 남겨둠)
- **jq 필터에 큰따옴표 섞을 때**: JSON 이스케이프가 이중으로 꼬이기 쉬움 → 작성 후 `echo '{...}' | jq -r '필터'`로 단독 실행해 문법 에러 없는지 반드시 확인.
- **훅 추가 후에는 실제로 한 번 트리거해서 로그/출력이 기대대로 나오는지 확인**할 것.
