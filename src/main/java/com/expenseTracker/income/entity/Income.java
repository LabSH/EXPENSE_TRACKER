package com.expenseTracker.income.entity;

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
@Comment("수입")
@Table(name = "TB_INCOME", indexes = {
    @Index(name = "ix_tb_income_user_id", columnList = "USER_ID")
})
public class Income extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "INCOME_ID")
    @Comment("수입ID")
    private Long incomeId;

    @Column(name = "USER_ID", length = 36, nullable = false)
    @Comment("사용자ID")
    private String userId;

    @Column(name = "INCOME_TYPE_CD", length = 30, nullable = false)
    @Comment("소득종류코드")
    private String incomeTypeCd;

    @Column(name = "INCOME_DT", nullable = false)
    @Comment("소득일자")
    private LocalDate incomeDt;

    @Column(name = "AMOUNT", precision = 15, scale = 2, nullable = false)
    @Comment("금액")
    private BigDecimal amount;

    @Column(name = "CONTENT", length = 200)
    @Comment("내용")
    private String content;

    @Column(name = "MEMO", length = 500)
    @Comment("메모")
    private String memo;

    @Column(name = "USE_AT", length = 1)
    @Comment("사용여부")
    private String useAt;

    @Column(name = "DEL_AT", length = 1)
    @Comment("삭제여부")
    private String delAt;
}
