package com.expenseTracker.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

import java.io.Serializable;

// 복합 식별자(@Embeddable)는 JPA 규약상 equals/hashCode 구현이 필수라 @EqualsAndHashCode를 사용한다.
// (연관관계 필드가 없어 순환 참조 위험이 없음)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
