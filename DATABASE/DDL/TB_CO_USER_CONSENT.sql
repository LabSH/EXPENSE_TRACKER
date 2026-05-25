-- ============================================================
-- MariaDB
-- ============================================================
CREATE TABLE `TB_CO_USER_CONSENT` (
  `CONSENT_ID`   varchar(36)  NOT NULL    COMMENT '동의이력ID (UUID)',
  `USER_ID`      varchar(36)  NOT NULL    COMMENT '사용자ID',
  `CONSENT_TYPE` varchar(20)  NOT NULL    COMMENT '동의유형 (TERMS/PRIVACY/MARKETING)',
  `AGREED`       varchar(1)   NOT NULL    COMMENT '동의여부 (Y/N)',
  `AGREED_AT`    datetime     NOT NULL    COMMENT '동의일시',
  PRIMARY KEY (`CONSENT_ID`),
  KEY `ix_tb_co_user_consent_user_id` (`USER_ID`),
  CONSTRAINT `fk_tb_co_user_consent_user` FOREIGN KEY (`USER_ID`) REFERENCES `TB_CO_USER` (`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='사용자 동의이력';


-- ============================================================
-- PostgreSQL
-- ============================================================
CREATE TABLE TB_CO_USER_CONSENT (
  CONSENT_ID   VARCHAR(36)  NOT NULL,
  USER_ID      VARCHAR(36)  NOT NULL,
  CONSENT_TYPE VARCHAR(20)  NOT NULL,
  AGREED       VARCHAR(1)   NOT NULL,
  AGREED_AT    TIMESTAMPTZ  NOT NULL,
  CONSTRAINT pk_tb_co_user_consent      PRIMARY KEY (CONSENT_ID),
  CONSTRAINT fk_tb_co_user_consent_user FOREIGN KEY (USER_ID) REFERENCES TB_CO_USER (USER_ID)
);

CREATE INDEX ix_tb_co_user_consent_user_id ON TB_CO_USER_CONSENT (USER_ID);

COMMENT ON TABLE  TB_CO_USER_CONSENT              IS '사용자 동의이력';
COMMENT ON COLUMN TB_CO_USER_CONSENT.CONSENT_ID   IS '동의이력ID (UUID)';
COMMENT ON COLUMN TB_CO_USER_CONSENT.USER_ID      IS '사용자ID';
COMMENT ON COLUMN TB_CO_USER_CONSENT.CONSENT_TYPE IS '동의유형 (TERMS/PRIVACY/MARKETING)';
COMMENT ON COLUMN TB_CO_USER_CONSENT.AGREED       IS '동의여부 (Y/N)';
COMMENT ON COLUMN TB_CO_USER_CONSENT.AGREED_AT    IS '동의일시';
