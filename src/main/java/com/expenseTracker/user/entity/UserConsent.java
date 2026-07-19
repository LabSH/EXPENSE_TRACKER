package com.expenseTracker.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("사용자동의이력")
@Table(name = "TB_CO_USER_CONSENT",
    indexes = @Index(name = "ix_tb_co_user_consent_user_id", columnList = "USER_ID")
)
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CONSENT_ID", length = 36)
    @Comment("동의이력ID")
    private String consentId;

    @Column(name = "USER_ID", length = 36, nullable = false)
    @Comment("사용자ID")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONSENT_TYPE", length = 20, nullable = false)
    @Comment("동의유형")
    private ConsentType consentType;

    @Column(name = "AGREED", length = 1, nullable = false)
    @Comment("동의여부")
    private String agreed;

    @Column(name = "AGREED_AT", nullable = false)
    @Comment("동의일시")
    private OffsetDateTime agreedAt;
}
