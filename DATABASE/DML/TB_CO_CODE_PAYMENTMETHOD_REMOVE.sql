-- ============================================================
-- PAYMENTMETHOD 공통코드 제거
-- 계좌·카드(TB_CO_ACCOUNT) + 고정지출 AUTO_PAY_AT 도입으로 결제수단 코드는 더 이상 사용하지 않는다.
-- 전제: DATABASE/DML/TB_CO_ACCOUNT_MIGRATION.sql 실행 완료 + 신규 애플리케이션 배포 완료
--       (구버전 앱은 PAYMENT_METHOD_CD 공통코드명을 조회하므로 배포 전 삭제 시 화면이 깨진다)
-- PostgreSQL
-- ============================================================

-- TB_CO_CODE.GROUP_ID 가 TB_CO_CODE_GROUP 을 참조하므로 코드 -> 그룹 순서로 삭제한다
DELETE FROM TB_CO_CODE       WHERE GROUP_ID = 'PAYMENTMETHOD';
DELETE FROM TB_CO_CODE_GROUP WHERE GROUP_ID = 'PAYMENTMETHOD';
