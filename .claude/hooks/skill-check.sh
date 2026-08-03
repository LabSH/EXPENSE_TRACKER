#!/bin/bash
INPUT=$(cat)
PROMPT=$(echo "$INPUT" | jq -r '.prompt')
RULES_FILE=".claude/skill-rules.json"

[ ! -f "$RULES_FILE" ] && exit 0

# 최근 작업 파일 캐시 주입
[ -f .claude/context-cache.md ] && printf '[최근 작업 파일]\n%s\n' "$(cat .claude/context-cache.md)"

# skill-rules.json에서 키워드 매칭 후 힌트 출력
# 주의: Windows jq.exe는 -r 출력에 CRLF를 섞어 내보내므로 매번 tr -d '\r'로 제거해야 함
HINTS=""
for SKILL in $(jq -r 'keys[]' "$RULES_FILE" | tr -d '\r'); do
  KEYWORDS=$(jq -r --arg s "$SKILL" '.[$s].promptTriggers.keywords[]?' "$RULES_FILE" 2>/dev/null | tr -d '\r')
  for KW in $KEYWORDS; do
    if echo "$PROMPT" | grep -qi "$KW"; then
      DESC=$(jq -r --arg s "$SKILL" '.[$s].description' "$RULES_FILE" | tr -d '\r')
      HINTS="$HINTS\n- $DESC"
      break
    fi
  done
done

[ -n "$HINTS" ] && printf "[관련 스킬 힌트]$HINTS\n"
exit 0
