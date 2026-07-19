package com.expenseTracker.user.entity;

import com.expenseTracker.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Comment("사용자")
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
    @Comment("사용자ID")
    private String userId;

    @Column(name = "LOGIN_ID", length = 50, nullable = false)
    @Comment("로그인ID")
    private String loginId;

    @Column(name = "PASSWD", length = 200, nullable = false)
    @Comment("비밀번호")
    private String passwd;

    @Column(name = "USER_NM", length = 100, nullable = false)
    @Comment("사용자명")
    private String userNm;

    @Column(name = "NICKNAME", length = 100)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "EMAIL", length = 200)
    @Comment("이메일")
    private String email;

    @Column(name = "ROLE_CD", length = 30)
    @Comment("권한코드")
    private String roleCd;

    @Column(name = "USE_AT", length = 1)
    @Comment("사용여부")
    private String useAt;

    @Column(name = "DEL_AT", length = 1)
    @Comment("삭제여부")
    private String delAt;
}
