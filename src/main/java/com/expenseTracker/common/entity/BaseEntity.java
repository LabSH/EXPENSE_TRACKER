package com.expenseTracker.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class BaseEntity {

    @CreationTimestamp
    @Column(name = "RGS_DT", updatable = false)
    @Comment("등록일시")
    private LocalDateTime rgsDt;

    @Column(name = "RGS_USER_ID", length = 30)
    @Comment("등록사용자ID")
    private String rgsUserId;

    @UpdateTimestamp
    @Column(name = "UPD_DT")
    @Comment("수정일시")
    private LocalDateTime updDt;

    @Column(name = "UPD_USER_ID", length = 30)
    @Comment("수정사용자ID")
    private String updUserId;
}
