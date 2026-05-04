-- ExpenseTracker 초기 테이블 생성 스크립트

CREATE TABLE IF NOT EXISTS expense (
    id          BIGSERIAL       PRIMARY KEY,
    title       VARCHAR(100)    NOT NULL,
    amount      NUMERIC(15, 2)  NOT NULL,
    category    VARCHAR(50),
    memo        VARCHAR(255),
    expense_date DATE           NOT NULL,
    created_at  TIMESTAMP       DEFAULT NOW(),
    updated_at  TIMESTAMP       DEFAULT NOW()
);
