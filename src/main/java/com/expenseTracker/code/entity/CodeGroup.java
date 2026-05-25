package com.expenseTracker.code.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TB_CO_CODE_GROUP")
public class CodeGroup extends BaseEntity {

    @Id
    @Column(name = "GROUP_ID", length = 30)
    private String groupId;

    @Column(name = "GROUP_KND_CD_ID", length = 30)
    private String groupKndCdId;

    @Column(name = "GROUP_NM", length = 200)
    private String groupNm;

    @OneToMany(mappedBy = "codeGroup", fetch = FetchType.LAZY)
    private List<Code> codes = new ArrayList<>();
}
