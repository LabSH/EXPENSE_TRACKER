package com.expenseTracker.code.repository;

import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodeRepository extends JpaRepository<Code, CodeId> {
    List<Code> findByIdGroupIdOrderBySortSn(String groupId);
    List<Code> findByIdGroupId(String groupId);
    long countByIdGroupId(String groupId);
}
