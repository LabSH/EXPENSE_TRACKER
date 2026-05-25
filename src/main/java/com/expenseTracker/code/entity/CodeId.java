package com.expenseTracker.code.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class CodeId implements Serializable {

    @Column(name = "CODE_ID", length = 30)
    private String codeId;

    @Column(name = "GROUP_ID", length = 30)
    private String groupId;
}
