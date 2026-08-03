package com.expenseTracker.code.repository;

import com.expenseTracker.code.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, String> {

    /** 그룹ID 또는 그룹명에 키워드가 포함된 그룹 검색 (대소문자 무시) */
    List<CodeGroup> findByGroupIdContainingIgnoreCaseOrGroupNmContainingIgnoreCase(String groupId, String groupNm);
}
