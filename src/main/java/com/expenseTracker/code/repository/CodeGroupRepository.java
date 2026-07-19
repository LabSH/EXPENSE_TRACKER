package com.expenseTracker.code.repository;

import com.expenseTracker.code.entity.CodeGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeGroupRepository extends JpaRepository<CodeGroup, String> {

    List<CodeGroup> findByGroupIdContainingIgnoreCaseOrGroupNmContainingIgnoreCase(String groupId, String groupNm);
}
