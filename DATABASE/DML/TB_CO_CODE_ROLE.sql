-- ============================================================
-- 권한 코드 그룹 & 코드 초기 데이터
-- MariaDB / PostgreSQL 공통
-- ============================================================

-- 코드 그룹
INSERT INTO TB_CO_CODE_GROUP (GROUP_ID, GROUP_NM, RGS_DT, RGS_USER_ID)
VALUES ('ROLE', '사용자 역할', CURRENT_TIMESTAMP, 'SYSTEM');

-- 코드
INSERT INTO TB_CO_CODE (CODE_ID, GROUP_ID, CODE_NM, SORT_SN, USE_AT, DEL_AT, RGS_DT, RGS_USER_ID)
VALUES
    ('ROLE_ADMIN',  'ROLE', '관리자', 1, 'Y', 'N', CURRENT_TIMESTAMP, 'SYSTEM'),
    ('ROLE_USER',   'ROLE', '사용자', 2, 'Y', 'N', CURRENT_TIMESTAMP, 'SYSTEM'),
    ('ROLE_VIEWER', 'ROLE', '뷰어',   3, 'Y', 'N', CURRENT_TIMESTAMP, 'SYSTEM');
