package com.expenseTracker.code.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Comment("공통코드그룹")
@Table(name = "TB_CO_CODE_GROUP")
public class CodeGroup extends BaseEntity {

    @Id
    @Column(name = "GROUP_ID", length = 30)
    @Comment("그룹ID")
    private String groupId;

    @Column(name = "GROUP_KND_CD_ID", length = 30)
    @Comment("그룹종류코드ID")
    private String groupKndCdId;

    @Column(name = "GROUP_NM", length = 200)
    @Comment("그룹명")
    private String groupNm;

    @OneToMany(mappedBy = "codeGroup", fetch = FetchType.LAZY)
    private List<Code> codes = new ArrayList<>();
}
