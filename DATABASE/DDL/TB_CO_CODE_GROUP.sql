-- ============================================================
-- PostgreSQL
-- ============================================================
CREATE TABLE TB_CO_CODE_GROUP (
  GROUP_ID        VARCHAR(30)  NOT NULL,
  GROUP_KND_CD_ID VARCHAR(30),
  GROUP_NM        VARCHAR(200),
  RGS_DT          TIMESTAMP,
  RGS_USER_ID     VARCHAR(30),
  UPD_DT          TIMESTAMP,
  UPD_USER_ID     VARCHAR(30),
  CONSTRAINT pk_tb_co_code_group PRIMARY KEY (GROUP_ID)
);

COMMENT ON TABLE  TB_CO_CODE_GROUP                IS '공통코드그룹';
COMMENT ON COLUMN TB_CO_CODE_GROUP.GROUP_ID        IS '그룹ID';
COMMENT ON COLUMN TB_CO_CODE_GROUP.GROUP_KND_CD_ID IS '그룹종류코드ID';
COMMENT ON COLUMN TB_CO_CODE_GROUP.GROUP_NM        IS '그룹명';
COMMENT ON COLUMN TB_CO_CODE_GROUP.RGS_DT          IS '등록일시';
COMMENT ON COLUMN TB_CO_CODE_GROUP.RGS_USER_ID     IS '등록사용자ID';
COMMENT ON COLUMN TB_CO_CODE_GROUP.UPD_DT          IS '수정일시';
COMMENT ON COLUMN TB_CO_CODE_GROUP.UPD_USER_ID     IS '수정사용자ID';
