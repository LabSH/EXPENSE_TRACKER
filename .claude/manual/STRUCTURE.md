# claude 디렉토리 구조

## 0. 구조도

```
프로젝트 루트/
├── CLAUDE.md                # 권장 - 프로젝트 지침 문서화
└── .claude/
    ├── settings.json         # 필수 - 권한/훅/모델 등 공용 설정
    ├── settings.local.json   # 권장 - 개인 로컬 오버라이드 (git 제외)
    ├── agents/                # 권장 - 서브에이전트 정의
    │   └── 이름.md
    ├── skills/                 # 권장 - 재사용 가능한 절차/컨벤션
    │   └── 이름/SKILL.md
    └── rules/                  # 상황에 따라 - 경로별 조건부 지침
```

## 1. 공식 구조 (Claude Code가 실제로 인식)

| 항목 | 용도 | 권장도 |
|---|---|---|
| `settings.json` | 권한/훅/모델 등 프로젝트 공용 설정 | 필수 |
| `CLAUDE.md` | 프로젝트 지침 문서화 | 권장 |
| `settings.local.json` | 개인용 로컬 오버라이드 (git 제외 대상) | 권장 |
| `skills/*/SKILL.md` | 재사용 가능한 절차/컨벤션 정의, `/SKILLNAME`으로 호출 | 권장 |
| `agents/*.md` | 반복되는 전문 도메인 작업이 있을 때 서브에이전트로 분리 | 권장 |
| `rules/` | 프로젝트가 커져서 경로별 조건부 지침이 필요할 때 | 상황에 따라 |

**주의**: `hooks/` 라는 폴더 자체는 공식 스펙이 아니다. 훅은 `settings.json`의 `"hooks"` 키에서
`command` 필드로 임의 경로를 지정하는 방식이고, `.claude/hooks/`는 그 스크립트를 모아두는 **관례**일 뿐 —
Claude Code가 이 폴더를 자동으로 스캔하지 않는다.

이 외에 `commands/`(레거시, 스킬로 대체 권장), `workflows/`, `output-styles/`, `agent-memory/`는
특수 목적용이라 일반적인 권장 목록에서 제외함.

## 2. 연결 관계

- `agents/*.md`의 `skills:` 필드 → `skills/*/SKILL.md` (에이전트가 작업 시 참조할 컨벤션 지정)
- `settings.json`의 `hooks` 키 → 임의 경로의 스크립트 실행 (경로/폴더명은 관례일 뿐 공식 스펙 아님)
