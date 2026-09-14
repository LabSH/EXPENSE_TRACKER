package com.expenseTracker.code.service;

import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.dto.GroupResponse;
import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.entity.CodeGroup;
import com.expenseTracker.code.entity.CodeId;
import com.expenseTracker.code.repository.CodeGroupRepository;
import com.expenseTracker.code.repository.CodeRepository;
import com.expenseTracker.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeRepository codeRepository;

    /** 공통코드 그룹 전체 목록 조회 (그룹별 코드 개수 포함) */
    @Transactional(readOnly = true)
    public List<GroupResponse> findAllGroups() {
        return toGroupResponses(codeGroupRepository.findAll());
    }

    /** 그룹ID/그룹명 키워드로 공통코드 그룹 검색 */
    @Transactional(readOnly = true)
    public List<GroupResponse> searchGroups(String keyword) {
        return toGroupResponses(codeGroupRepository
                .findByGroupIdContainingIgnoreCaseOrGroupNmContainingIgnoreCase(keyword, keyword));
    }

    /** 그룹ID에 속한 코드 항목을 정렬순번 순으로 조회 */
    @Transactional(readOnly = true)
    public List<CodeResponse> findCodesByGroup(String groupId) {
        return codeRepository.findByIdGroupIdOrderBySortSn(groupId)
                .stream()
                .map(this::toCodeResponse)
                .toList();
    }

    /** 그룹ID·코드ID로 코드명 조회 (없으면 빈 Optional) */
    @Transactional(readOnly = true)
    public Optional<String> findCodeName(String groupId, String codeId) {
        return codeRepository.findById(CodeId.builder().codeId(codeId).groupId(groupId).build())
                .map(Code::getCodeNm);
    }

    /** 사용중(USE_AT='Y')인 공통코드를 그룹ID별로 묶어 조회 (그룹이 비어 있으면 빈 목록) */
    @Transactional(readOnly = true)
    public Map<String, List<CodeResponse>> findUsableCodesByGroup() {
        Map<String, List<CodeResponse>> codesByGroup = codeRepository.findAllByOrderByIdGroupIdAscSortSnAsc()
                .stream()
                .filter(c -> "Y".equals(c.getUseAt()))
                .map(this::toCodeResponse)
                .collect(Collectors.groupingBy(CodeResponse::groupId));

        Map<String, List<CodeResponse>> result = new LinkedHashMap<>();
        codeGroupRepository.findAll()
                .forEach(g -> result.put(g.getGroupId(), codesByGroup.getOrDefault(g.getGroupId(), List.of())));
        return result;
    }

    /** 공통코드 그룹 등록 (그룹ID는 대문자·언더스코어로 정규화) */
    @Transactional
    public GroupResponse addGroup(String groupId, String groupNm, String userId) {
        CodeGroup group = CodeGroup.builder()
                .groupId(groupId.toUpperCase().replaceAll("\\s+", "_"))
                .groupNm(groupNm)
                .rgsUserId(userId)
                .build();
        codeGroupRepository.save(group);
        return new GroupResponse(group.getGroupId(), group.getGroupNm(), 0);
    }

    /** 공통코드 그룹명 수정 */
    @Transactional
    public GroupResponse updateGroup(String groupId, String groupNm, String userId) {
        CodeGroup group = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("그룹을 찾을 수 없습니다."));
        group.setGroupNm(groupNm);
        group.setUpdUserId(userId);
        return new GroupResponse(groupId, groupNm, codeRepository.countByIdGroupId(groupId));
    }

    /** 공통코드 그룹과 소속 코드 항목을 모두 삭제 */
    @Transactional
    public void deleteGroup(String groupId) {
        codeRepository.deleteAll(codeRepository.findByIdGroupId(groupId));
        codeGroupRepository.deleteById(groupId);
    }

    /** 공통코드 항목 등록 (코드ID는 그룹 내 순번을 이어 자동 채번) */
    @Transactional
    public CodeResponse addCode(String groupId, String codeNm, int sortSn, String useAt, String userId) {
        CodeGroup codeGroup = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("그룹을 찾을 수 없습니다."));

        String codeId = generateNextCodeId(groupId);

        Code code = Code.builder()
                .id(CodeId.builder().codeId(codeId).groupId(groupId).build())
                .codeGroup(codeGroup)
                .codeNm(codeNm)
                .sortSn(sortSn)
                .useAt(useAt)
                .rgsUserId(userId)
                .build();
        codeRepository.save(code);

        return new CodeResponse(codeId, groupId, codeNm, sortSn, useAt);
    }

    /** 공통코드 항목 수정 */
    @Transactional
    public CodeResponse updateCode(String codeId, String groupId, String codeNm, int sortSn, String useAt, String userId) {
        Code code = codeRepository.findById(CodeId.builder().codeId(codeId).groupId(groupId).build())
                .orElseThrow(() -> new BusinessException("코드를 찾을 수 없습니다."));
        code.setCodeNm(codeNm);
        code.setSortSn(sortSn);
        code.setUseAt(useAt);
        code.setUpdUserId(userId);

        return new CodeResponse(codeId, groupId, codeNm, sortSn, useAt);
    }

    /** 공통코드 항목 삭제 */
    @Transactional
    public void deleteCode(String codeId, String groupId) {
        CodeId id = CodeId.builder().codeId(codeId).groupId(groupId).build();
        if (!codeRepository.existsById(id)) {
            throw new BusinessException("코드를 찾을 수 없습니다.");
        }
        codeRepository.deleteById(id);
    }

    /** 그룹 내 마지막 순번을 이어 다음 코드ID를 채번 */
    private String generateNextCodeId(String groupId) {
        int maxSeq = codeRepository.findByIdGroupId(groupId).stream()
                .map(c -> c.getId().getCodeId())
                .filter(id -> id.startsWith(groupId + "_"))
                .mapToInt(id -> {
                    try { return Integer.parseInt(id.substring(groupId.length() + 1)); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max().orElse(0);
        return groupId + "_" + String.format("%03d", maxSeq + 1);
    }

    /** 그룹 목록을 코드 개수(단일 집계 쿼리)와 합쳐 응답 DTO로 변환 */
    private List<GroupResponse> toGroupResponses(List<CodeGroup> groups) {
        Map<String, Long> counts = codeRepository.countGroupedByGroupId().stream()
                .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
        return groups.stream()
                .map(g -> new GroupResponse(g.getGroupId(), g.getGroupNm(), counts.getOrDefault(g.getGroupId(), 0L)))
                .toList();
    }

    /** Code 엔티티를 응답 DTO로 변환 */
    private CodeResponse toCodeResponse(Code code) {
        return new CodeResponse(
                code.getId().getCodeId(),
                code.getId().getGroupId(),
                code.getCodeNm(),
                code.getSortSn() != null ? code.getSortSn() : 0,
                code.getUseAt());
    }
}
