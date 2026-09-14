# 다음 예정 작업

> 최종 갱신: 2026-09-07

---

## 0. 계좌·카드 — ACCOUNTTYPE 공통코드 DB 반영 ← **먼저 할 작업**

설정 > 계좌·카드 > 추가 모달의 **유형 드롭다운이 비어 있음**. `ACCOUNTTYPE` 공통코드가 DB에 없어서다.
(유형 옵션은 `CodeModelAdvice`가 주입하는 `codes['ACCOUNTTYPE']`로 렌더된다.)

- `DATABASE/DML/TB_CO_CODE_ACCOUNTTYPE.sql` 실행 → 카드/입출금/저축/현금 등록
- 계좌·카드 기능 전체 DB 반영 순서는 아래 "5. 계좌·카드 마이그레이션" 참고

---

## 1. 토스트 디자인 수정

### 현재 상태

공통 토스트 컴포넌트는 **동작까지 완성**되어 있고, **고정지출 화면에만** 연결돼 있다.
애니메이션과 문구는 확정됐고, **남은 건 디자인(외형) 다듬기**다.

### 관련 파일

| 파일 | 역할 |
|---|---|
| `src/main/resources/static/js/common/toast.js` | 컴포넌트 본체. 마크업·클래스가 전부 여기 있음 |
| `src/main/resources/static/css/input.css` | `toastIn` / `toastOut` 키프레임, `body { overflow-x: hidden }` |
| `src/main/resources/templates/config/config.html` | 스크립트 로딩 (사용자·관리자 공통 head) |
| `src/main/resources/templates/fixed-expense/index.html` | 호출부 9곳 |

> `toast.js`의 클래스를 바꾸면 **`npm run tw:build`** 를 다시 돌려야 `output.css`에 반영된다.

### 현재 스펙

```
┌──────────────────────────────────┐
│  ⬤   추가되었습니다.              │   ⬤ = 원형 배지 (상태색 채움 + 흰 아이콘)
└──────────────────────────────────┘

┌──────────────────────────────────┐
│  ⬤   에러가 발생하였습니다.        │   ← 제목 (text-sm, semibold, ink)
│      고정지출을 찾을 수 없습니다.  │   ← 설명 (text-xs, ink-soft)
└──────────────────────────────────┘
```

| 항목 | 값 |
|---|---|
| 위치 | 우상단 `top-6 right-6`, 새 토스트가 아래로 쌓임 |
| 박스 | `min-w-60 max-w-sm`, `px-4 py-3`, `rounded-xl`, `border`, `shadow-lg` |
| 배지 | `w-7 h-7 rounded-full`, 성공 `bg-sage`, 에러 `bg-blush`, 아이콘 흰색 stroke 15px |
| 배경 | 성공 `bg-sage-light border-sage/30`, 에러 `bg-blush-light border-blush/30` |
| 정렬 | `items-center`, 줄 높이 `leading-5`(제목) / `leading-4`(설명) |
| 노출 | 성공 2.5초 / 에러 4초, 클릭 시 즉시 닫힘 |
| 애니메이션 | 화면 오른쪽 밖 → 제자리 (`calc(100% + 1.5rem)`, 진입 .32s / 퇴장 .24s) |

### 바로 직전에 한 수정 (육안 확인 필요)

"토스트 위아래 여백이 안 맞는다"는 피드백으로 아래를 고쳤다. **아직 브라우저로 확인하지 못했다.**

- `items-start` → `items-center` (배지 28px vs 한 줄 텍스트 20px 높이 차이로 텍스트가 위로 떠 보이던 문제)
- 텍스트 블록의 `pt-0.5` 제거
- `py-3.5` → `py-3`
- `leading-5` / `leading-4` 명시 (폰트 메트릭에 따라 높이가 흔들리는 것 방지)

**다음 세션에서 먼저 할 일: 실제 화면에서 여백이 맞는지 확인.** 여전히 어긋나면 위/아래 중 어디가 남는지 보고 조정.

### 이미 결정된 사항 (다시 논의할 필요 없음)

- **위치는 우상단** — 우하단·상단중앙과 비교해서 선택함
- **새 토스트는 아래로 쌓임** — `flex flex-col gap-2` + `appendChild`
- **오른쪽 화면 밖에서 슬라이드 인**
- **배경을 surface가 아니라 상태색(`sage-light`/`blush-light`)으로 쓴 이유** — 사용자 화면(cream)과 관리자 화면(admin)의 surface 토큰이 달라서, 상태색을 써야 한 컴포넌트로 양쪽을 커버할 수 있음. 이걸 흰 카드형으로 바꾸려면 테마 분기가 필요해짐
- **`z-[60]`인 이유** — 모달이 `z-50`이라 그 위에 떠야 함 (저장 실패 시 모달 위에 에러 표시)
- **컨테이너가 `body` 직속인 이유** — htmx가 `#main-content`를 교체해도 살아남아야 함
- **아이콘은 stroke 유지** — 참고 디자인은 fill이었으나 15px에서 뭉개지고, 프로젝트의 다른 아이콘이 전부 stroke임
- **"조회되었습니다" 토스트는 넣지 않음** — 결과가 화면에 바로 보여서 소음이 됨. 첨부파일 업로드 성공도 같은 이유로 제외

### 알려진 미해결 사항

- 여러 개가 쌓인 상태에서 **위쪽 토스트가 먼저 사라지면 아래 것들이 즉시 위로 점프**한다. 부드럽게 밀려 올라가게 하려면 높이 애니메이션이 필요. 우선순위 낮음

---

## 2. 토스트 전체 화면 적용 (미착수)

현재 고정지출에만 연결돼 있다. 나머지 `alert()` **12곳**이 남아 있다.

| 파일 | 개수 |
|---|---|
| `static/js/admin/code.js` | 6 |
| `static/js/admin/users.js` | 5 |
| `static/js/admin/logs.js` | 1 |

`static/js/common/ajax.js`의 `requestJson()`이 모든 HTTP 오류를 한곳에서 `throw`하므로,
**거기에 에러 토스트를 한 번만 물리면** 개별 `catch`의 `alert`를 상당수 제거할 수 있다.

---

## 3. 고정지출 — 백엔드 연동 후 확인 못 한 것

우측 "지출 예정" 패널과 삭제 기능은 **컴파일·JS 문법까지만 검증**했다. 실제 숫자는 미확인.

- 우측 패널 숫자가 실제 등록 건 기준으로 나오는지, `남은 지출 ≤ 결제 예정`인지
- 금액에 소수점이 안 붙는지 (`51666.5` 같은 값)
- 매년·분기 항목이 **결제되는 달에는 적립에서 빠지는지** (이중 계상 방지 로직)
- 삭제 시 `TB_CO_ATTACH_FILE`의 `DEL_AT`가 `Y`로 바뀌는지
- 행 hover 시 아이콘 노출, 행 클릭 시 상세 모달, 아이콘 클릭 시 상세가 같이 열리지 않는지

---

## 4. 결정 보류 중인 설계 항목

- **적립 권장액 대상 범위** — 현재 격월·분기·반기·매년. 격월(÷2)을 포함한 게 맞는지 재검토 여지
- **연체 항목 표시** — 현재 "다가오는 지출"에는 오늘 이후만 내려온다. 지난 날짜(미납)도 보여줄 거면 `feDday()`에 `지남` 라벨 추가 필요 (근거는 해당 함수 주석에 기록해 둠)
- **`feFormatWon` 통합** — `renderFixedExpenses`와 `openFixedExpenseViewModal`이 금액 포맷을 인라인으로 중복 구현 중. 기존 동작 코드라 최소 변경 원칙으로 두었음
- **`FixedExpenseService` 분리** — 313줄로 커짐. 주기 계산 로직을 별도 클래스로 뺄 수 있으나, 수입 도메인에도 필요해지는 시점이 적기

---

## 5. 계좌·카드 마이그레이션 (DB 스크립트 수동 실행)

`ddl-auto=update`라 앱을 먼저 띄우면 COMMENT·DEFAULT 없는 테이블이 생성됨 → DDL을 앱 기동 전에 실행.

1. `DATABASE/DDL/TB_CO_ACCOUNT.sql` — 테이블 생성
2. `DATABASE/DML/TB_CO_CODE_ACCOUNTTYPE.sql` — ACCOUNTTYPE 공통코드 (← 항목 0)
3. `DATABASE/DDL/TB_EX_FIXED_EXPENSE.sql` 하단의 `ADD COLUMN ACCOUNT_ID / AUTO_PAY_AT` 2줄
4. `DATABASE/DML/TB_CO_ACCOUNT_MIGRATION.sql` — 사용자별 기본 계좌 생성 + 고정지출 4건 매핑. 마지막 검증 쿼리에서 `MAPPED = TOTAL` 확인
5. 신규 앱 배포 (이 시점부터 앱이 `PAYMENT_METHOD_CD`를 안 읽음)
6. `ALTER TABLE TB_EX_FIXED_EXPENSE DROP COLUMN PAYMENT_METHOD_CD`
7. `DATABASE/DML/TB_CO_CODE_PAYMENTMETHOD_REMOVE.sql` — PAYMENTMETHOD 그룹 삭제

### 개인정보 처리방침 반영

- `TB_CO_ACCOUNT` 수집항목: 계좌명(별칭)·발급기관명·메모. 카드/계좌번호 뒤 4자리는 **수집하지 않기로 결정**(민감 식별자 회피).
- `MEMO`는 자유 입력이라 사용자가 민감정보를 넣을 수 있음 → 입력란에 "민감정보 입력 금지" 안내 문구 배치(완료). 처리방침에도 "메모는 참고용, 민감정보 입력 금지" 고지 필요.
- 회원 탈퇴 시 `TB_CO_ACCOUNT` 행 파기 대상에 포함(현재 고정지출·수입도 미연동 — 함께 정리 필요).

### 미검증 (DB 반영 + 구동 후 확인)

- 설정 계좌·카드 CRUD, 유형별 아이콘·색, 계좌 사용 중일 때 삭제 차단(400)
- 고정지출 입력 화면 계좌 드롭다운 렌더·저장, 상세 모달 "이체방식" 표시
- 우측 "지출 예정" 패널에서 수동이체 건 강조(왼쪽 gold 바 + "직접이체" 칩)
