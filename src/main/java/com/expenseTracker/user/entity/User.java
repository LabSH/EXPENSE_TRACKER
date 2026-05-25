package com.expenseTracker.user.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "TB_CO_USER",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tb_user_login_id", columnNames = "LOGIN_ID"),
        @UniqueConstraint(name = "uk_tb_user_email",    columnNames = "EMAIL")
    }
)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "USER_ID", length = 36)
    private String userId;

    @Column(name = "LOGIN_ID", length = 50, nullable = false)
    private String loginId;

    @Column(name = "PASSWD", length = 200, nullable = false)
    private String passwd;

    @Column(name = "USER_NM", length = 100, nullable = false)
    private String userNm;

    @Column(name = "NICKNAME", length = 100)
    private String nickname;

    @Column(name = "EMAIL", length = 200)
    private String email;

    @Column(name = "ROLE_CD", length = 30)
    private String roleCd;

    @Column(name = "USE_AT", length = 1)
    private String useAt;

    @Column(name = "DEL_AT", length = 1)
    private String delAt;
}
