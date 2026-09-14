-- ============================================================
-- 계좌·카드 도입 마이그레이션
-- 기존 고정지출의 PAYMENT_METHOD_CD 를 사용자별 기본 계좌로 이관한다.
-- 전제: TB_CO_ACCOUNT 생성(DDL/TB_CO_ACCOUNT.sql), ACCOUNTTYPE 공통코드 등록(DML/TB_CO_CODE_ACCOUNTTYPE.sql),
--       TB_EX_FIXED_EXPENSE 의 ACCOUNT_ID / AUTO_PAY_AT 컬럼 추가가 모두 끝나 있어야 함
-- 재실행해도 계좌가 중복 생성되지 않는다 (NOT EXISTS / ACCOUNT_ID IS NULL 조건)
-- PostgreSQL
-- ============================================================

-- 0) 사전 확인: 자동이체 계열 결제수단(자동이체(카드)/자동이체(계좌))을 쓰던 행이 있는지 본다.
--    0건이면 그대로 진행한다(전 대상 행 AUTO_PAY_AT='N').
--    0건이 아니면 3) 실행 전에 해당 행만 AUTO_PAY_AT='Y'로 보정할지 먼저 결정할 것.
SELECT COUNT(*) AS AUTO_PAY_ROWS
FROM   TB_EX_FIXED_EXPENSE
WHERE  PAYMENT_METHOD_CD IN ('PAYMENTMETHOD_004', 'PAYMENTMETHOD_005');

-- 1) 사용자별 기본 계좌 생성 (사용중인 고정지출이 실제로 쓰던 결제수단 유형에 대해서만)
--    ISSUER_NM / MEMO 는 사용자가 나중에 보완한다
INSERT INTO TB_CO_ACCOUNT (USER_ID, ACCOUNT_TYPE_CD, ACCOUNT_NM, ISSUER_NM, MEMO, USE_AT, DEL_AT, RGS_DT, RGS_USER_ID)
SELECT DISTINCT M1.USER_ID, C1.ACCOUNT_TYPE_CD, C1.ACCOUNT_NM, NULL, NULL, 'Y', 'N', CURRENT_TIMESTAMP, 'SYSTEM'
FROM   TB_EX_FIXED_EXPENSE M1
JOIN  (VALUES
         ('PAYMENTMETHOD_001', 'ACCOUNTTYPE_004', '현금'),
         ('PAYMENTMETHOD_002', 'ACCOUNTTYPE_001', '기본 카드'),
         ('PAYMENTMETHOD_003', 'ACCOUNTTYPE_002', '기본 계좌'),
         ('PAYMENTMETHOD_004', 'ACCOUNTTYPE_001', '기본 카드'),
         ('PAYMENTMETHOD_005', 'ACCOUNTTYPE_002', '기본 계좌')
      ) AS C1 (PAYMENT_METHOD_CD, ACCOUNT_TYPE_CD, ACCOUNT_NM)
  ON  C1.PAYMENT_METHOD_CD = M1.PAYMENT_METHOD_CD
WHERE M1.DEL_AT = 'N'
  AND NOT EXISTS (SELECT 1
                  FROM   TB_CO_ACCOUNT C2
                  WHERE  C2.USER_ID    = M1.USER_ID
                    AND  C2.ACCOUNT_NM = C1.ACCOUNT_NM);

-- 2) 고정지출에 계좌ID 매핑 (삭제된 행도 정보 보존을 위해 매핑 대상에 포함)
UPDATE TB_EX_FIXED_EXPENSE M1
SET    ACCOUNT_ID = C2.ACCOUNT_ID
FROM  (VALUES
         ('PAYMENTMETHOD_001', '현금'),
         ('PAYMENTMETHOD_002', '기본 카드'),
         ('PAYMENTMETHOD_003', '기본 계좌'),
         ('PAYMENTMETHOD_004', '기본 카드'),
         ('PAYMENTMETHOD_005', '기본 계좌')
      ) AS C1 (PAYMENT_METHOD_CD, ACCOUNT_NM)
JOIN   TB_CO_ACCOUNT C2 ON C2.ACCOUNT_NM = C1.ACCOUNT_NM AND C2.DEL_AT = 'N'
WHERE  M1.PAYMENT_METHOD_CD = C1.PAYMENT_METHOD_CD
  AND  C2.USER_ID           = M1.USER_ID
  AND  M1.ACCOUNT_ID IS NULL;

-- 3) 자동이체여부 초기화 (컬럼 DEFAULT 는 신규 INSERT 에만 적용되므로 기존 행은 직접 채운다)
UPDATE TB_EX_FIXED_EXPENSE
SET    AUTO_PAY_AT = 'N'
WHERE  AUTO_PAY_AT IS NULL;

-- 4) 검증: PAYMENT_METHOD_CD 가 있던 그룹은 MAPPED = TOTAL 이어야 한다 (PAYMENT_METHOD_CD IS NULL 그룹 제외)
SELECT M1.PAYMENT_METHOD_CD,
       COUNT(*)             AS TOTAL,
       COUNT(M1.ACCOUNT_ID) AS MAPPED
FROM   TB_EX_FIXED_EXPENSE M1
GROUP  BY M1.PAYMENT_METHOD_CD
ORDER  BY M1.PAYMENT_METHOD_CD;
