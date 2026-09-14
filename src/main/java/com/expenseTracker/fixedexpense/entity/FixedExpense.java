package com.expenseTracker.fixedexpense.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Comment("고정지출")
@Table(name = "TB_EX_FIXED_EXPENSE")
public class FixedExpense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FIXED_EXPENSE_ID")
    @Comment("고정지출ID")
    private Long fixedExpenseId;

    @Column(name = "USER_ID", length = 36, nullable = false)
    @Comment("사용자ID")
    private String userId;

    @Column(name = "ACCOUNT_ID")
    @Comment("계좌ID (TB_CO_ACCOUNT 참조)")
    private Long accountId;

    @Column(name = "AUTO_PAY_AT", length = 1)
    @Comment("자동이체여부 (Y=자동이체, N=수동이체)")
    private String autoPayAt;

    @Column(name = "EXPENSE_CYCLE_CD", length = 30)
    @Comment("지출주기코드")
    private String expenseCycleCd;

    @Column(name = "CATEGORY_CD", length = 30)
    @Comment("카테고리코드")
    private String categoryCd;

    @Column(name = "ANCHOR_DT", nullable = false)
    @Comment("기준일(시작일) (다음 지출 예정일 계산 기준)")
    private LocalDate anchorDt;

    @Column(name = "END_DT")
    @Comment("종료일 (NULL이면 무기한. 이 날짜를 넘는 지출 예정일은 발생하지 않음)")
    private LocalDate endDt;

    @Column(name = "AMOUNT", precision = 15, scale = 2, nullable = false)
    @Comment("금액")
    private BigDecimal amount;

    @Column(name = "CONTENT", length = 200, nullable = false)
    @Comment("내용")
    private String content;

    @Column(name = "MEMO", length = 500)
    @Comment("메모")
    private String memo;

    @Column(name = "FILE_GROUP_ID", length = 36)
    @Comment("첨부파일그룹ID")
    private String fileGroupId;

    @Column(name = "USE_AT", length = 1)
    @Comment("사용여부")
    private String useAt;

    @Column(name = "DEL_AT", length = 1)
    @Comment("삭제여부")
    private String delAt;
}
