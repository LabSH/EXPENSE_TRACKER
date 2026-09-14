package com.expenseTracker.account.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Comment("계좌·카드")
@Table(name = "TB_CO_ACCOUNT", indexes = {
        @Index(name = "ix_tb_co_account_user_id", columnList = "USER_ID")
})
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    @Comment("계좌ID")
    private Long accountId;

    @Column(name = "USER_ID", length = 36, nullable = false)
    @Comment("사용자ID")
    private String userId;

    @Column(name = "ACCOUNT_TYPE_CD", length = 30)
    @Comment("계좌유형코드")
    private String accountTypeCd;

    @Column(name = "ACCOUNT_NM", length = 50, nullable = false)
    @Comment("계좌명")
    private String accountNm;

    @Column(name = "ISSUER_NM", length = 50)
    @Comment("발급기관명")
    private String issuerNm;

    @Column(name = "MEMO", length = 200)
    @Comment("참고용 메모 (민감정보 입력 금지)")
    private String memo;

    @Column(name = "USE_AT", length = 1)
    @Comment("사용여부")
    private String useAt;

    @Column(name = "DEL_AT", length = 1)
    @Comment("삭제여부")
    private String delAt;
}
