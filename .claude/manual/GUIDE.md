# .claude 다른 프로젝트로 이식하기

## 0. 사전 준비물 (체크리스트)

훅 스크립트들이 아래 도구에 의존한다. 새 PC/프로젝트에 옮기기 전에 설치 여부부터 확인할 것.

- [ ] **`jq`** 설치 여부 확인 — `skill-check.sh`, `post-tool-tracker.sh`, `pre-compact-report.sh`, `settings.json`의 `SubagentStart`/`SubagentStop` 커맨드가 전부 JSON 파싱에 `jq`를 씀. 없으면 훅이 조용히 실패함
  - 확인: `jq --version`
  - 설치(Windows): `winget install jqlang.jq` 또는 `choco install jq`
  - 설치(macOS): `brew install jq`
  - 설치(Linux): `apt install jq` 등
- [ ] **`bash`** 실행 가능 여부 — 모든 훅 스크립트(`.sh`)가 bash 기반. Windows는 Git Bash 등이 있어야 함
- [ ] (Windows) **PowerShell** 실행 가능 여부 — `notification.ps1` 훅이 `powershell.exe`를 직접 호출함

## 1. 전역(PC 공용) 경로 vs 프로젝트 경로

이식할 때 옮길 대상은 **프로젝트 경로뿐**이다. 전역 경로는 PC(사용자 계정)에 이미 한 번 세팅되면
모든 프로젝트에 자동 적용되므로 프로젝트마다 복사할 필요가 없다.

| 구분 | 경로 | 적용 범위 |
|---|---|---|
| 전역(PC 공용) | `%USERPROFILE%\.claude\`(Windows) / `~/.claude/`(macOS·Linux) | 이 계정으로 여는 모든 프로젝트 |
| 전역 하위 | `settings.json`, `CLAUDE.md`, `skills/`, `rules/` | 전역 - 모든 프로젝트에 공통 적용 |
| 전역 하위 | `projects/<프로젝트>/memory/` | 전역이지만 프로젝트별로 분리 저장 (자동 메모리) |
| 프로젝트 경로 | `<프로젝트 루트>/.claude/` | 그 프로젝트에서 열었을 때만 적용 |

우선순위(높음→낮음): **local(`.claude/settings.local.json`) > project(`.claude/settings.json`) > user(전역 `settings.json`)**
— 프로젝트 설정이 전역 설정을 덮어쓰는 구조라, 지금 이 폴더의 `settings.json`/`settings.local.json`은
전역과 무관하게 프로젝트 단위로만 이식하면 됨.

## 2. 그대로 사용 가능한 것

포맷/스펙 자체는 프로젝트에 종속되지 않아서 그대로 복붙 가능.

| 항목 | 이유 |
|---|---|
| `settings.json`의 구조(`hooks`, `permissions` 키 형식) | Claude Code 공식 스펙, 프로젝트 무관 |
| `agents/*.md`, `skills/*/SKILL.md`의 frontmatter 형식(`name`, `description`, `tools` 등) | 공식 스펙 |
| `HOOKS_GUIDE.md`, `STRUCTURE.md` 같은 참고 문서 | 프로젝트 내용이 아니라 Claude Code 사용법 자체를 다룸 |

## 3. 주의가 필요한 것 (내용 재작성 필요)

| 유형 | 해당하는 것 | 조치 |
|---|---|---|
| 특정 도메인에 관여되는 내용 | `agents/*.md` 본문, `skills/*/SKILL.md`, `CLAUDE.md`, `skill-rules.json` 키워드 | 새 프로젝트의 도메인/스택/컨벤션에 맞게 재작성 |
| 원래 프로젝트 경로가 박힌 내용 | `settings.local.json`/`settings.json`의 절대경로, `hooks/*.sh`·`*.ps1` 안의 `.claude/...` 상대경로 | 새 프로젝트 경로로 수정 후 직접 실행해 검증 |

## 4. 적용 절차

1. `.claude/` 폴더 전체를 새 프로젝트 루트에 복사
2. **위 3번 표 항목부터** 새 프로젝트에 맞게 수정 (도메인 내용, 하드코딩 경로)
3. `HOOKS_GUIDE.md`의 주의사항(4번)을 참고해 각 훅을 한 번씩 직접 트리거해서 정상 동작 확인
4. 불필요한 도메인 전용 에이전트/스킬(이 프로젝트에만 맞는 것)은 삭제
