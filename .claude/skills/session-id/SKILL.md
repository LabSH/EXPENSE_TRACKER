---
name: session-id
description: 현재 세션의 session_id(UUID)를 확인합니다.
model: haiku
---

# 현재 세션 ID 확인

Claude Code는 채팅 안에서 session_id를 보여주는 공식 명령어가 없다. 대신 아래 방법으로 유추한다.

## 1단계: 스크래치패드 경로에서 추출

시스템 프롬프트의 "Scratchpad Directory" 경로에 현재 세션의 session_id(UUID)가 폴더명으로 포함되어 있다.
예: `...\CLAUDE_Setting--claude\31dfe0e2-bdcf-4c0d-8839-940076f1bb64\scratchpad`
→ 이 중 `8-4-4-4-12` 형식의 UUID 부분을 추출한다.

## 2단계: transcript 파일로 교차 검증 (선택)

`~/.claude/projects/<인코딩된 프로젝트 경로>/` 폴더에서 **가장 최근에 수정된 `.jsonl` 파일명**이
1단계에서 추출한 UUID와 같은지 확인한다. 같으면 확실히 맞는 것.

```bash
ls -lt ~/.claude/projects/<인코딩된 프로젝트 경로>/*.jsonl | head -1
```

## 3단계: 사용자에게 보고

추출한 UUID를 session_id로 알려준다.
