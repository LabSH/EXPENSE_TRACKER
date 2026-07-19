package com.expenseTracker.code.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Comment("공통코드")
@Table(name = "TB_CO_CODE", indexes = {
    @Index(name = "idx_code_01", columnList = "CODE_ID")
})
public class Code extends BaseEntity {

    @EmbeddedId
    private CodeId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "GROUP_ID")
    @Comment("공통코드그룹")
    private CodeGroup codeGroup;

    @Column(name = "CODE_NM", length = 200)
    @Comment("코드명")
    private String codeNm;

    @Column(name = "SORT_SN", precision = 10, scale = 0)
    @Comment("정렬순번")
    private Integer sortSn;

    @Column(name = "USE_AT", length = 1)
    @Comment("사용여부")
    private String useAt;
}
