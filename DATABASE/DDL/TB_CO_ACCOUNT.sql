-- ============================================================
-- PostgreSQL
-- ============================================================
CREATE TABLE TB_CO_ACCOUNT (
  ACCOUNT_ID      BIGSERIAL   NOT NULL,
  USER_ID         VARCHAR(36) NOT NULL,
  ACCOUNT_TYPE_CD VARCHAR(30),
  ACCOUNT_NM      VARCHAR(50) NOT NULL,
  ISSUER_NM       VARCHAR(50),
  MEMO            VARCHAR(200),
  USE_AT          VARCHAR(1)  DEFAULT 'Y',
  DEL_AT          VARCHAR(1)  DEFAULT 'N',
  RGS_DT          TIMESTAMP,
  RGS_USER_ID     VARCHAR(30),
  UPD_DT          TIMESTAMP,
  UPD_USER_ID     VARCHAR(30),
  CONSTRAINT pk_tb_co_account      PRIMARY KEY (ACCOUNT_ID),
  CONSTRAINT fk_tb_co_account_user FOREIGN KEY (USER_ID) REFERENCES TB_CO_USER (USER_ID)
);

CREATE INDEX ix_tb_co_account_user_id ON TB_CO_ACCOUNT (USER_ID);

COMMENT ON TABLE  TB_CO_ACCOUNT                 IS '계좌·카드';
COMMENT ON COLUMN TB_CO_ACCOUNT.ACCOUNT_ID      IS '계좌ID';
COMMENT ON COLUMN TB_CO_ACCOUNT.USER_ID         IS '사용자ID';
COMMENT ON COLUMN TB_CO_ACCOUNT.ACCOUNT_TYPE_CD IS '계좌유형코드 (공통코드 ACCOUNTTYPE 그룹 참조. 목록의 아이콘·색상은 이 값으로 화면에서 결정)';
COMMENT ON COLUMN TB_CO_ACCOUNT.ACCOUNT_NM      IS '계좌명 (사용자가 붙이는 별칭)';
COMMENT ON COLUMN TB_CO_ACCOUNT.ISSUER_NM       IS '발급기관명 (국민, 신한 등)';
COMMENT ON COLUMN TB_CO_ACCOUNT.MEMO            IS '참고용 메모 (카드번호·비밀번호 등 민감정보 입력 금지)';
COMMENT ON COLUMN TB_CO_ACCOUNT.USE_AT          IS '사용여부';
COMMENT ON COLUMN TB_CO_ACCOUNT.DEL_AT          IS '삭제여부';
COMMENT ON COLUMN TB_CO_ACCOUNT.RGS_DT          IS '등록일시';
COMMENT ON COLUMN TB_CO_ACCOUNT.RGS_USER_ID     IS '등록사용자ID';
COMMENT ON COLUMN TB_CO_ACCOUNT.UPD_DT          IS '수정일시';
COMMENT ON COLUMN TB_CO_ACCOUNT.UPD_USER_ID     IS '수정사용자ID';
