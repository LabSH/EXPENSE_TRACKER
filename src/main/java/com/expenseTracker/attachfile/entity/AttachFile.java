package com.expenseTracker.attachfile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Comment("공통 첨부파일")
@Table(name = "TB_CO_FILES")
public class AttachFile {

    @EmbeddedId
    private AttachFileId id;

    @Column(name = "ORG_FILE_NM", length = 255, nullable = false)
    @Comment("원본파일명")
    private String orgFileNm;

    @Column(name = "SAVE_FILE_NM", length = 255, nullable = false)
    @Comment("저장파일명")
    private String saveFileNm;

    @Column(name = "FILE_PATH", length = 500, nullable = false)
    @Comment("저장경로")
    private String filePath;

    @Column(name = "FILE_SIZE", precision = 15, scale = 0)
    @Comment("파일크기(byte)")
    private Long fileSize;

    @Column(name = "FILE_EXT", length = 20)
    @Comment("확장자")
    private String fileExt;

    @Column(name = "SORT_SN", precision = 10, scale = 0)
    @Comment("정렬순번")
    private Integer sortSn;

    @Column(name = "DEL_AT", length = 1)
    @Comment("삭제여부")
    private String delAt;

    @CreationTimestamp
    @Column(name = "RGS_DT", updatable = false)
    @Comment("등록일시")
    private LocalDateTime rgsDt;

    @Column(name = "RGS_USER_ID", length = 36)
    @Comment("등록사용자ID")
    private String rgsUserId;
}
