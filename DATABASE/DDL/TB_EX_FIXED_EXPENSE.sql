-- ============================================================
-- PostgreSQL
-- ============================================================
CREATE TABLE TB_EX_FIXED_EXPENSE (
  FIXED_EXPENSE_ID  BIGSERIAL      NOT NULL,
  USER_ID           VARCHAR(36)    NOT NULL,
  ACCOUNT_ID        BIGINT,
  AUTO_PAY_AT       VARCHAR(1)     DEFAULT 'N',
  EXPENSE_CYCLE_CD  VARCHAR(30),
  CATEGORY_CD       VARCHAR(30),
  ANCHOR_DT         DATE           NOT NULL,
  END_DT            DATE,
  AMOUNT            NUMERIC(15, 2) NOT NULL,
  CONTENT           VARCHAR(200)   NOT NULL,
  MEMO              VARCHAR(500),
  FILE_GROUP_ID     VARCHAR(36),
  USE_AT            VARCHAR(1)     DEFAULT 'Y',
  DEL_AT            VARCHAR(1)     DEFAULT 'N',
  RGS_DT            TIMESTAMP,
  RGS_USER_ID       VARCHAR(36),
  UPD_DT            TIMESTAMP,
  UPD_USER_ID       VARCHAR(36),
  CONSTRAINT ck_tb_ex_fixed_expense_end_dt CHECK (END_DT IS NULL OR END_DT >= ANCHOR_DT),
  CONSTRAINT pk_tb_ex_fixed_expense      PRIMARY KEY (FIXED_EXPENSE_ID),
  CONSTRAINT fk_tb_ex_fixed_expense_user FOREIGN KEY (USER_ID) REFERENCES TB_CO_USER (USER_ID)
);

CREATE INDEX ix_tb_ex_fixed_expense_user_id ON TB_EX_FIXED_EXPENSE (USER_ID);

COMMENT ON TABLE  TB_EX_FIXED_EXPENSE                  IS '고정지출';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.FIXED_EXPENSE_ID IS '고정지출ID';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.USER_ID           IS '사용자ID';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.ACCOUNT_ID         IS '계좌ID (TB_CO_ACCOUNT 참조. 이 지출이 빠져나가는 계좌·카드)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.AUTO_PAY_AT        IS '자동이체여부 (Y=자동이체, N=수동이체)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.EXPENSE_CYCLE_CD  IS '지출주기코드 (공통코드 EXPENSECYCLE 그룹 참조)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.CATEGORY_CD       IS '카테고리코드 (공통코드 CATEGORY 그룹 참조)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.ANCHOR_DT          IS '기준일(시작일) (다음 지출 예정일 계산 기준. 매주/격주는 요일, 매월 계열은 일자, 매년은 월일을 이 날짜에서 추출)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.END_DT             IS '종료일 (NULL이면 무기한. 이 날짜를 넘는 지출 예정일은 발생하지 않음. 12개월 할부·약정 등 끝이 있는 고정지출에 사용)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.AMOUNT             IS '금액';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.CONTENT            IS '내용';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.MEMO               IS '메모';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.FILE_GROUP_ID      IS '첨부파일그룹ID (TB_CO_FILES 참조)';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.USE_AT             IS '사용여부';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.DEL_AT             IS '삭제여부';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.RGS_DT             IS '등록일시';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.RGS_USER_ID        IS '등록사용자ID';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.UPD_DT             IS '수정일시';
COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.UPD_USER_ID        IS '수정사용자ID';

-- ============================================================
-- 기존 테이블 마이그레이션 (이미 TB_EX_FIXED_EXPENSE 가 존재하는 경우)
-- ddl-auto=update 는 컬럼만 추가하고 CHECK 제약은 붙이지 않으므로 직접 실행할 것
-- ============================================================
-- ALTER TABLE TB_EX_FIXED_EXPENSE ADD COLUMN IF NOT EXISTS END_DT DATE;
-- COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.END_DT IS '종료일 (NULL이면 무기한. 이 날짜를 넘는 지출 예정일은 발생하지 않음)';
-- ALTER TABLE TB_EX_FIXED_EXPENSE ADD CONSTRAINT ck_tb_ex_fixed_expense_end_dt CHECK (END_DT IS NULL OR END_DT >= ANCHOR_DT);

-- ------------------------------------------------------------
-- 계좌·카드 도입 (결제수단 공통코드 → 사용자별 계좌 + 자동이체 플래그)
-- 실행 순서: 아래 ADD 2줄 → DATABASE/DML/TB_CO_ACCOUNT_MIGRATION.sql → 신규 앱 배포 → 마지막에 DROP 1줄
-- ------------------------------------------------------------
-- ALTER TABLE TB_EX_FIXED_EXPENSE ADD COLUMN IF NOT EXISTS ACCOUNT_ID BIGINT;
-- ALTER TABLE TB_EX_FIXED_EXPENSE ADD COLUMN IF NOT EXISTS AUTO_PAY_AT VARCHAR(1) DEFAULT 'N';
-- COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.ACCOUNT_ID  IS '계좌ID (TB_CO_ACCOUNT 참조. 이 지출이 빠져나가는 계좌·카드)';
-- COMMENT ON COLUMN TB_EX_FIXED_EXPENSE.AUTO_PAY_AT IS '자동이체여부 (Y=자동이체, N=수동이체)';
-- 아래 DROP 은 TB_CO_ACCOUNT_MIGRATION.sql 실행 + 신규 애플리케이션 배포가 모두 끝난 뒤에 실행할 것
-- ALTER TABLE TB_EX_FIXED_EXPENSE DROP COLUMN IF EXISTS PAYMENT_METHOD_CD;
