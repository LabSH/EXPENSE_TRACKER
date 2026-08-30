# 색상 토큰 접근성 개선 — 변경 및 롤백 안내
> 작업일시: 2026-08-26 23:45

## 변경 이유
사용자(cream)/관리자(admin) 두 테마의 색온도 분리(H40 웜 / H250 쿨)는 유지하되,
아래 두 결함을 수정.

1. 보조 텍스트 토큰이 WCAG AA(4.5:1) 미달 — `ink-muted` 2.35, `text-soft` 4.29, `text-muted` 2.31
2. `ink` 계열이 쿨(H249) 고정이라 웜 배경인 cream 화면에서 색온도 충돌
3. 액센트 `sage` 4.43으로 문턱 미달 (violet은 5.73으로 통과 — 테마 간 불균등)

## 변경 내용

### 1) `src/main/resources/static/css/input.css` — @theme 값 5개
| 토큰 | 변경 전 | 변경 후 | 대비(표면 위) |
|---|---|---|---|
| `--color-ink-soft`   | `#6E6A85` | `#58556B` | 5.06 → 7.02 |
| `--color-ink-muted`  | `#A8A4C0` | `#757386` | 2.35 → 4.51 |
| `--color-text-soft`  | `#7C7870` | `#5A5752` | 4.29 → 7.01 |
| `--color-text-muted` | `#ADA89F` | `#78746E` | 2.31 → 4.53 |
| `--color-sage`       | `#3a845a` | `#398259` | 4.43 → 4.54 |

- 모든 값은 원본의 색상(H)·채도(S)를 유지한 채 명도만 낮춰 산출
- 3단 램프를 16:1 / 7:1 / 4.5:1 로 재배치해 본문·보조·약함 단계가 구분되도록 함
- `ink-soft`/`ink-muted`/`text-soft`/`text-muted` 는 텍스트 전용으로만 쓰이는 것을 grep으로 확인 후 변경
- `sage` 는 bg(26)/border(19)/text(28)에 쓰이나 변화폭이 1~2단위라 육안 차이 없음

### 2) `input.css` — `.theme-cream` 스코프 블록 신규 추가
`.hidden` 규칙 바로 아래에 unlayered 로 추가. `@layer theme` 의 `:root` 보다 우선순위가 높음.

```css
.theme-cream {
    --color-ink:       #2C2A26;
    --color-ink-soft:  #5A5752;
    --color-ink-muted: #78746E;
}
```

사용자 화면에서만 ink 토큰을 웜 값으로 덮어씀. admin 화면은 이 클래스가 없어 쿨 값 유지.
템플릿의 `text-ink` 등 클래스명은 하나도 바꾸지 않음 (CSS 변수 상속으로 해결).

### 3) `src/main/resources/templates/index.html` — body 클래스
```
<body class="bg-cream font-sans text-ink">
→ <body class="theme-cream bg-cream font-sans text-ink">
```
사용자 화면은 전부 이 레이아웃 안에서 htmx로 `#main-content` 만 교체되므로 body 한 곳이면 충분.
admin 4개 페이지(`admin/index`, `admin/code`, `admin/users`, `admin/logs`)는 각자 body를 가지며 미변경.

### 4) `src/main/resources/static/css/output.css`
`npm run tw:build` 결과물 (수동 편집 아님).

## 최종 대비비 (전부 AA 통과)
```
cream  ink 13.97 / ink-soft 7.01 / ink-muted 4.53
admin  ink 16.42 / ink-soft 7.02 / ink-muted 4.51
액센트 sage 4.54 / violet 5.73
로그인 text-soft 7.01 / text-muted 4.53
```

## 롤백 방법

`input.css` 와 `templates/index.html` 은 이 작업 직전 HEAD와 완전히 동일한 상태였음.
따라서 아래 한 줄이면 정확히 원복됨.

```bash
git checkout -- src/main/resources/static/css/input.css src/main/resources/templates/index.html
npm run tw:build
```

`output.css` 는 `git checkout` 하지 말 것 — 이 작업 이전부터 다른 세션의 미커밋 변경이 섞여 있어
직접 되돌리면 그 변경까지 날아감. 위처럼 `input.css` 를 원복한 뒤 재빌드하는 방식이 안전함.

### 부분 롤백
- **색온도 통일만 취소** (대비 개선은 유지): `input.css` 의 `.theme-cream` 블록 삭제 + `index.html` body에서 `theme-cream` 제거 후 재빌드
- **대비 개선만 취소** (색온도 통일은 유지): 위 표의 "변경 전" 값으로 되돌린 뒤 재빌드

## 미적용 (보류)
`--color-cream*` 과 `--color-bg`/`--color-surface`/`--color-surface-2`/`--color-border` 는
값이 완전히 동일한 중복 토큰이나, 통합하려면 로그인/회원가입/에러 템플릿의 클래스명을
일괄 치환해야 해서 이번 작업에서 제외. 시각적 결함이 아닌 토큰 정리 이슈.
