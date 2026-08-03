package com.expenseTracker.attachfile.repository;

import com.expenseTracker.attachfile.entity.AttachFile;
import com.expenseTracker.attachfile.entity.AttachFileId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachFileRepository extends JpaRepository<AttachFile, AttachFileId> {
    /** 파일그룹ID에 속한 첨부파일을 정렬순번 순으로 조회 */
    List<AttachFile> findByIdFileGroupIdOrderBySortSn(String fileGroupId);
}
