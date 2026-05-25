package com.expenseTracker.code.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TB_CO_CODE", indexes = {
    @Index(name = "idx_code_01", columnList = "CODE_ID")
})
public class Code extends BaseEntity {

    @EmbeddedId
    private CodeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "GROUP_ID")
    private CodeGroup codeGroup;

    @Column(name = "CODE_NM", length = 200)
    private String codeNm;

    @Column(name = "SORT_SN", precision = 10, scale = 0)
    private Integer sortSn;

    @Column(name = "USE_AT", length = 1)
    private String useAt;

    @Column(name = "DEL_AT", length = 1)
    private String delAt;
}
