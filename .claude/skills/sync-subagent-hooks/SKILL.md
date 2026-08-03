---
name: sync-subagent-hooks
description: .claude/agents/*.md에 정의된 서브에이전트 목록과, .claude/hooks/*.sh·.claude/skill-rules.json·.claude/settings.json에 박혀있는 서브에이전트 참조/키워드/파일패턴이 서로 어긋나 있지 않은지 감사하고 고칩니다. "서브에이전트가 자동으로 안 불려요", "훅 설정 점검해줘", "skill-rules 동기화" 같은 요청에 사용하세요.
---

# 서브에이전트 ↔ 훅/설정 동기화 감사

## 왜 필요한가

이 레포는 예전에(다른 프로젝트를 복붙했거나, 에이전트를 추가/삭제한 뒤) `.claude/hooks/*.sh`나 `.claude/skill-rules.json`을 갱신하지 않아서, 실제로는 존재하지 않는 서브에이전트를 호출하라고 지시하거나, 전혀 다른 스택(JSP/MyBatis 등)의 키워드·경로 패턴을 그대로 갖고 있는 경우가 있었다. 이런 죽은 참조는 조용히 무시되기 때문에 사용자가 눈치채기 전까지 "서브에이전트가 필요할 때 안 불려요"라는 증상으로만 드러난다.

## 절차

### 1단계: 실제 서브에이전트 목록 확보

`Glob(".claude/agents/*.md")`로 실제 존재하는 에이전트 파일을 모두 찾고, 각각의 frontmatter(`name`)와 `description`을 읽는다. description 안에 "다음 상황에서 반드시 호출할 것" 류의 트리거 조건이나 담당 파일 유형(예: HTML 템플릿, JS, Java Controller)이 적혀 있으면 같이 기록해둔다. 이 목록이 앞으로의 검증 기준(single source of truth)이다.

### 2단계: 설정 파일에서 죽은 참조 찾기

아래 파일들을 읽고, 1단계 목록에 없는 에이전트 이름이 하드코딩되어 있는지 찾는다:
- `.claude/hooks/*.sh` (전체 스캔 — `Grep`으로 "서브에이전트", "agent" 등 키워드 검색)
- `.claude/skill-rules.json`
- `.claude/settings.json`의 `hooks` 섹션에 인라인으로 박힌 command 문자열

죽은 참조(존재하지 않는 에이전트 이름)를 발견하면: 삭제하거나, 실제로 그 자리에 들어가야 할 에이전트로 교체한다. 어느 게 맞는지 애매하면 사용자에게 물어본다.

### 3단계: skill-rules.json 스택 정합성 확인

`.claude/skill-rules.json`의 `promptTriggers.keywords`와 `fileTriggers.pathPatterns`가 **이 프로젝트의 실제 기술 스택·디렉토리 구조**와 맞는지 확인한다. 확인 방법:
- 언급된 확장자(`.jsp`, `.java`, `.html` 등)가 실제 프로젝트에 존재하는 파일 유형인지
- `pathPatterns`에 적힌 디렉토리(`src/main/webapp/...`, `src/main/resources/templates/...` 등)가 `Glob`으로 실제 존재하는지
- 완전히 다른 스택 용어(예: Spring 프로젝트인데 `tiles`, `sqlmap`, `serviceimpl` 같은 MyBatis/Tiles 전용 용어)가 섞여 있는지

맞지 않으면 1단계에서 파악한 실제 에이전트들의 담당 범위(description에 적힌 파일 패턴)를 그대로 반영해서 다시 작성한다. 각 에이전트 1개당 항목 1개, `description`은 언제 호출해야 하는지 짧게, `fileTriggers.pathPatterns`는 그 에이전트가 다루는 실제 파일 위치를 넣는다.

### 4단계: 훅 스크립트가 fileTriggers를 실제로 쓰는지 확인

`skill-rules.json`에 `fileTriggers.pathPatterns`가 있는데 정작 어떤 훅도 그 값을 읽지 않는 경우가 흔하다(`promptTriggers.keywords`만 읽고 방치). 이 경우:
- `PostToolUse`(matcher: `Edit|Write|MultiEdit`) 훅에 "방금 수정한 파일 경로 → 매칭되는 fileTriggers.pathPatterns → 해당 서브에이전트 이름 힌트 출력" 로직을 추가한다.
- 매칭은 bash `[[ "$REL_FILE" == $PATTERN ]]`로 가능하지만, `pattern/**/*.ext` 형태는 하위 디렉토리가 전혀 없는 파일(예: `templates/error.html`처럼 바로 밑에 있는 파일)에는 매칭되지 않는다. `**/`를 제거한 "flat" 패턴도 같이 시도해서 두 케이스를 모두 커버한다:
  ```bash
  FLAT="${PATTERN//\*\*\//}"
  if [[ "$REL_FILE" == $PATTERN ]] || [[ "$REL_FILE" == $FLAT ]]; then MATCHED=1; fi
  ```
- 힌트는 차단(block)이 아니라 `hookSpecificOutput.additionalContext`로 컨텍스트에 주입하는 형태로 만든다(skill-rules.json에 이미 "이건 힌트일 뿐 강제가 아님"이라는 취지가 적혀있다면 그 원칙을 유지).

### 5단계: Windows 환경이면 CRLF 함정 확인 (중요)

Windows에서 Git Bash로 훅을 돌리는 경우, Windows용 `jq.exe`가 `-r` 옵션 출력에 `\r`(캐리지리턴)을 섞어 내보내는 경우가 있다. 이게 껴 있으면:
- `for X in $(jq -r 'keys[]' file.json)` 같은 루프에서 각 값 끝에 보이지 않는 `\r`이 붙어, 이후 `.[$x]` 같은 jq 조회나 `[[ "$a" == "$b" ]]` 문자열 비교가 전부 조용히 실패한다.
- 증상: 겉보기엔 로직이 멀쩡한데 힌트가 하나도 안 뜨거나, 특정 조건이 한 번도 안 걸린다.
- 진단: `bash -x hook.sh < test-input.json 2>&1 | grep -A2 "for X in"` 로 실행해서 변수 값에 `$'...\r'` 형태(ANSI-C quoting에 `\r`)가 보이면 확정.
- 해결: 이 스크립트가 만들어내는 멀티라인 jq 출력을 받는 모든 곳에 `| tr -d '\r'`을 붙인다.

이 함정은 재현이 까다로우니, 고칠 때 반드시 실제로 파이프 테스트해서 확인한다(6단계).

### 6단계: 수정 후 검증

update-config 스킬의 "Constructing a Hook (with verification)" 절차를 따른다:
1. 스크래치패드에 실제 이 프로젝트 파일 경로를 담은 테스트 JSON을 `Write` 도구로 만든다 (echo로 인라인 전달하면 쉘 계층을 거치며 백슬래시 이스케이프가 깨지기 쉬우니 반드시 파일로 작성해서 `cat testfile.json | bash hook.sh`로 파이프).
2. `jq .` 로 테스트 JSON 자체가 유효한지 먼저 확인한다.
3. 매칭되어야 할 경우와 매칭되면 안 되는 경우를 각각 한 번씩 테스트해서, 기대한 서브에이전트 이름이 정확히 힌트에 나오는지 확인한다.
4. 실패하면 `bash -x hook.sh < test.json 2>&1`으로 트레이스를 보고 원인을 좁힌다 (특히 5단계의 CRLF 함정을 의심할 것).
5. 테스트하면서 `.claude/context-cache.md`, `.claude/log/modified-files.log`에 더미 기록이 남았다면 정리한다 (`session:test-session` 같은 테스트용 session_id로 남긴 줄만 골라서 지운다).

### 7단계: 결과 보고

무엇이 죽은 참조였는지, 어떤 파일을 왜 고쳤는지, 검증 결과를 사용자에게 요약 보고한다. 이 프로젝트에 국한된 문제가 아니라 다른 프로젝트에도 있을 법한 패턴(레거시 설정 복붙, jq CRLF 등)이면 그 사실도 짚어준다.
