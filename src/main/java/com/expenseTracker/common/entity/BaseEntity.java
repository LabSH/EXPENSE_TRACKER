package com.expenseTracker.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "RGS_DT")
    private LocalDateTime rgsDt;

    @Column(name = "RGS_USER_ID", length = 30)
    private String rgsUserId;

    @Column(name = "UPD_DT")
    private LocalDateTime updDt;

    @Column(name = "UPD_USER_ID", length = 30)
    private String updUserId;
}
