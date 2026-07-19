package com.expenseTracker.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class CodeId implements Serializable {

    @Column(name = "CODE_ID", length = 30)
    @Comment("공통코드ID")
    private String codeId;

    @Column(name = "GROUP_ID", length = 30)
    @Comment("그룹ID")
    private String groupId;
}
