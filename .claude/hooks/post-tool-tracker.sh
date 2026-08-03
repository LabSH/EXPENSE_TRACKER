#!/bin/bash
INPUT=$(cat)
FILE=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

[ -z "$FILE" ] && exit 0

TOOL=$(echo "$INPUT" | jq -r '.tool_name')
OLD=$(echo "$INPUT" | jq -r '.tool_input.old_string // empty' | head -1)
NEW=$(echo "$INPUT" | jq -r '.tool_input.new_string // empty' | head -1)
SUMMARY=$(echo "$INPUT" | jq -r '(.tool_input.new_string // .tool_input.content // "") | split("\n")[0]' | cut -c1-60)
SESSION=$(echo "$INPUT" | jq -r '.session_id // "unknown"')

# modified-files.log: 변경 전/후 상세 기록
mkdir -p .claude/log
printf "[$(date '+%Y-%m-%d %H:%M:%S')] [session:%s] %s: %s\n  변경전: %s\n  변경후: %s\n" \
  "$SESSION" "$TOOL" "$FILE" "$OLD" "$NEW" >> .claude/log/modified-files.log

# context-cache.md: 파일 + 변경 요약 (최근 20개 유지)
printf -- "- [$(date '+%H:%M')] [session:%s] %s: %s | %s\n" "$SESSION" "$TOOL" "$FILE" "$SUMMARY" >> .claude/context-cache.md
tail -20 .claude/context-cache.md > .claude/context-cache.tmp \
  && cp .claude/context-cache.tmp .claude/context-cache.md \
  && rm .claude/context-cache.tmp

# 서브에이전트 담당 파일 힌트 (skill-rules.json의 fileTriggers.pathPatterns 매칭)
# 주의: Windows jq.exe는 -r 출력에 CRLF를 섞어 내보내므로 매번 tr -d '\r'로 제거해야 함
RULES_FILE=".claude/skill-rules.json"
if [ -f "$RULES_FILE" ]; then
  REL_FILE=$(echo "$FILE" | sed 's#\\#/#g')
  REL_FILE="src/${REL_FILE#*src/}"

  MATCHES=""
  for SKILL in $(jq -r 'keys[]' "$RULES_FILE" | tr -d '\r'); do
    PATTERNS=$(jq -r --arg s "$SKILL" '.[$s].fileTriggers.pathPatterns[]?' "$RULES_FILE" 2>/dev/null | tr -d '\r')
    [ -z "$PATTERNS" ] && continue
    MATCHED=0
    while IFS= read -r PATTERN; do
      [ -z "$PATTERN" ] && continue
      FLAT="${PATTERN//\*\*\//}"
      if [[ "$REL_FILE" == $PATTERN ]] || [[ "$REL_FILE" == $FLAT ]]; then
        MATCHED=1
        break
      fi
    done <<< "$PATTERNS"
    if [ "$MATCHED" = "1" ]; then
      DESC=$(jq -r --arg s "$SKILL" '.[$s].description' "$RULES_FILE" | tr -d '\r')
      MATCHES="${MATCHES}- ${SKILL}: ${DESC}\n"
    fi
  done

  if [ -n "$MATCHES" ]; then
    CONTEXT=$(printf '[담당 서브에이전트 힌트] 방금 수정한 파일과 관련된 서브에이전트:\n%b' "$MATCHES")
    JSON_CONTEXT=$(printf '%s' "$CONTEXT" | jq -Rs .)
    printf '{"hookSpecificOutput":{"hookEventName":"PostToolUse","additionalContext":%s}}\n' "$JSON_CONTEXT"
  fi
fi

exit 0
