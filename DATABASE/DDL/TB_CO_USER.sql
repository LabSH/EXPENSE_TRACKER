-- ============================================================
-- MariaDB
-- ============================================================
CREATE TABLE `TB_CO_USER` (
  `USER_ID`     varchar(36)  NOT NULL    COMMENT '사용자ID (UUID)',
  `LOGIN_ID`    varchar(50)  NOT NULL    COMMENT '로그인ID',
  `PASSWD`      varchar(200) NOT NULL    COMMENT '비밀번호',
  `USER_NM`     varchar(100) NOT NULL    COMMENT '사용자명',
  `NICKNAME`    varchar(100) DEFAULT NULL COMMENT '닉네임',
  `EMAIL`       varchar(200) DEFAULT NULL COMMENT '이메일',
  `ROLE_CD`     varchar(30)  DEFAULT NULL COMMENT '권한코드 (공통코드 ROLE 그룹 참조)',
  `USE_AT`      varchar(1)   DEFAULT 'Y' COMMENT '사용여부',
  `DEL_AT`      varchar(1)   DEFAULT 'N' COMMENT '삭제여부',
  `RGS_DT`      datetime     DEFAULT NULL COMMENT '등록일시',
  `RGS_USER_ID` varchar(30)  DEFAULT NULL COMMENT '등록사용자ID',
  `UPD_DT`      datetime     DEFAULT NULL COMMENT '수정일시',
  `UPD_USER_ID` varchar(30)  DEFAULT NULL COMMENT '수정사용자ID',
  PRIMARY KEY (`USER_ID`),
  UNIQUE KEY `uk_tb_co_user_login_id` (`LOGIN_ID`),
  UNIQUE KEY `uk_tb_co_user_email` (`EMAIL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='사용자';


-- ============================================================
-- PostgreSQL
-- ============================================================
CREATE TABLE TB_CO_USER (
  USER_ID      VARCHAR(36)  NOT NULL,
  LOGIN_ID     VARCHAR(50)  NOT NULL,
  PASSWD       VARCHAR(200) NOT NULL,
  USER_NM      VARCHAR(100) NOT NULL,
  NICKNAME     VARCHAR(100),
  EMAIL        VARCHAR(200),
  ROLE_CD      VARCHAR(30),
  USE_AT       VARCHAR(1)   DEFAULT 'Y',
  DEL_AT       VARCHAR(1)   DEFAULT 'N',
  RGS_DT       TIMESTAMP,
  RGS_USER_ID  VARCHAR(30),
  UPD_DT       TIMESTAMP,
  UPD_USER_ID  VARCHAR(30),
  CONSTRAINT pk_tb_co_user          PRIMARY KEY (USER_ID),
  CONSTRAINT uk_tb_co_user_login_id UNIQUE (LOGIN_ID),
  CONSTRAINT uk_tb_co_user_email    UNIQUE (EMAIL)
);

COMMENT ON TABLE  TB_CO_USER             IS '사용자';
COMMENT ON COLUMN TB_CO_USER.USER_ID     IS '사용자ID (UUID)';
COMMENT ON COLUMN TB_CO_USER.LOGIN_ID    IS '로그인ID';
COMMENT ON COLUMN TB_CO_USER.PASSWD      IS '비밀번호';
COMMENT ON COLUMN TB_CO_USER.USER_NM     IS '사용자명';
COMMENT ON COLUMN TB_CO_USER.NICKNAME    IS '닉네임';
COMMENT ON COLUMN TB_CO_USER.EMAIL       IS '이메일';
COMMENT ON COLUMN TB_CO_USER.ROLE_CD     IS '권한코드 (공통코드 ROLE 그룹 참조)';
COMMENT ON COLUMN TB_CO_USER.USE_AT      IS '사용여부';
COMMENT ON COLUMN TB_CO_USER.DEL_AT      IS '삭제여부';
COMMENT ON COLUMN TB_CO_USER.RGS_DT      IS '등록일시';
COMMENT ON COLUMN TB_CO_USER.RGS_USER_ID IS '등록사용자ID';
COMMENT ON COLUMN TB_CO_USER.UPD_DT      IS '수정일시';
COMMENT ON COLUMN TB_CO_USER.UPD_USER_ID IS '수정사용자ID';
