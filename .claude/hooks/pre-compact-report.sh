#!/bin/bash
# PreCompact 훅: 컨텍스트 압축 전 세션 보고서 자동 생성

input=$(cat)
trigger=$(echo "$input" | jq -r '.trigger // "unknown"')
session_id=$(echo "$input" | jq -r '.session_id // "unknown"')

date=$(date +%Y-%m-%d_%H-%M)
mkdir -p .claude/snapshots
report_file=".claude/snapshots/pre-compact_${date}_${session_id}.md"

# 변경 파일 목록 수집
if [ -f ".claude/log/modified-files.log" ]; then
    files=$(tail -50 .claude/log/modified-files.log)
else
    files="기록 없음"
fi

cat > "$report_file" << EOF
# 세션 작업 요약 (압축 전 자동 생성)
> 생성일시: $(date '+%Y-%m-%d %H:%M')
> 트리거: ${trigger} (auto=자동압축, manual=/compact 명령)

## 변경된 파일 (최근 50건)
\`\`\`
${files}
\`\`\`

## 비고
- 컨텍스트 압축 직전에 자동 생성된 보고서입니다.
- 상세 AI 분석 보고서는 \`/session-report\` 명령으로 수동 생성하세요.
EOF

echo "pre-compact 보고서 생성: $report_file"
