package com.expenseTracker.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "TB_CO_USER_CONSENT",
    indexes = @Index(name = "ix_tb_co_user_consent_user_id", columnList = "USER_ID")
)
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CONSENT_ID", length = 36)
    private String consentId;

    @Column(name = "USER_ID", length = 36, nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "CONSENT_TYPE", length = 20, nullable = false)
    private ConsentType consentType;

    @Column(name = "AGREED", length = 1, nullable = false)
    private String agreed;

    @Column(name = "AGREED_AT", nullable = false)
    private OffsetDateTime agreedAt;
}
