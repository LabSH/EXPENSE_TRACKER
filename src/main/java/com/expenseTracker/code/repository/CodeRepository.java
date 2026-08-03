package com.expenseTracker.code.repository;

import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.entity.CodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CodeRepository extends JpaRepository<Code, CodeId> {
    /** 그룹ID에 속한 코드 항목을 정렬순번 순으로 조회 */
    List<Code> findByIdGroupIdOrderBySortSn(String groupId);
    /** 그룹ID에 속한 코드 항목 전체 조회 */
    List<Code> findByIdGroupId(String groupId);
    /** 전체 코드 항목을 그룹ID·정렬순번 순으로 조회 */
    List<Code> findAllByOrderByIdGroupIdAscSortSnAsc();
    /** 그룹ID에 속한 코드 항목 개수 조회 */
    long countByIdGroupId(String groupId);

    /** 그룹ID별 코드 항목 개수를 한 번에 집계 ([0]=그룹ID, [1]=개수) */
    @Query("""
            SELECT M1.id.groupId, COUNT(M1)
            FROM Code M1
            GROUP BY M1.id.groupId
            """)
    List<Object[]> countGroupedByGroupId();
}
