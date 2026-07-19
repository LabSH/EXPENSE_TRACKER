package com.expenseTracker.code.service;

import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.dto.GroupResponse;
import com.expenseTracker.code.entity.Code;
import com.expenseTracker.code.entity.CodeGroup;
import com.expenseTracker.code.entity.CodeId;
import com.expenseTracker.code.repository.CodeGroupRepository;
import com.expenseTracker.code.repository.CodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeGroupRepository codeGroupRepository;
    private final CodeRepository codeRepository;

    public List<GroupResponse> findAllGroups() {
        return codeGroupRepository.findAll().stream()
                .map(g -> new GroupResponse(
                        g.getGroupId(),
                        g.getGroupNm(),
                        codeRepository.countByIdGroupId(g.getGroupId())))
                .toList();
    }

    public List<GroupResponse> searchGroups(String keyword) {
        return codeGroupRepository
                .findByGroupIdContainingIgnoreCaseOrGroupNmContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(g -> new GroupResponse(
                        g.getGroupId(),
                        g.getGroupNm(),
                        codeRepository.countByIdGroupId(g.getGroupId())))
                .toList();
    }

    @Transactional
    public GroupResponse addGroup(String groupId, String groupNm, String userId) {
        CodeGroup group = new CodeGroup();
        group.setGroupId(groupId.toUpperCase().replaceAll("\\s+", "_"));
        group.setGroupNm(groupNm);
        group.setRgsDt(LocalDateTime.now());
        group.setRgsUserId(userId);
        codeGroupRepository.save(group);
        return new GroupResponse(group.getGroupId(), group.getGroupNm(), 0);
    }

    @Transactional
    public GroupResponse updateGroup(String groupId, String groupNm, String userId) {
        CodeGroup group = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));
        group.setGroupNm(groupNm);
        group.setUpdDt(LocalDateTime.now());
        group.setUpdUserId(userId);
        codeGroupRepository.save(group);
        return new GroupResponse(groupId, groupNm, codeRepository.countByIdGroupId(groupId));
    }

    @Transactional
    public void deleteGroup(String groupId) {
        codeRepository.deleteAll(codeRepository.findByIdGroupId(groupId));
        codeGroupRepository.deleteById(groupId);
    }

    public List<CodeResponse> findCodesByGroup(String groupId) {
        return codeRepository.findByIdGroupIdOrderBySortSn(groupId)
                .stream()
                .map(c -> new CodeResponse(
                        c.getId().getCodeId(),
                        c.getId().getGroupId(),
                        c.getCodeNm(),
                        c.getSortSn() != null ? c.getSortSn() : 0,
                        c.getUseAt()))
                .toList();
    }

    @Transactional
    public CodeResponse addCode(String groupId, String codeNm, int sortSn, String useAt, String userId) {
        int maxSeq = codeRepository.findByIdGroupId(groupId).stream()
                .map(c -> c.getId().getCodeId())
                .filter(id -> id.startsWith(groupId + "_"))
                .mapToInt(id -> {
                    try { return Integer.parseInt(id.substring(groupId.length() + 1)); }
                    catch (NumberFormatException e) { return 0; }
                })
                .max().orElse(0);
        String codeId = groupId + "_" + String.format("%03d", maxSeq + 1);

        CodeGroup codeGroup = codeGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        CodeId id = new CodeId();
        id.setCodeId(codeId);
        id.setGroupId(groupId);

        Code code = new Code();
        code.setId(id);
        code.setCodeGroup(codeGroup);
        code.setCodeNm(codeNm);
        code.setSortSn(sortSn);
        code.setUseAt(useAt);
        code.setRgsDt(LocalDateTime.now());
        code.setRgsUserId(userId);
        codeRepository.save(code);

        return new CodeResponse(codeId, groupId, codeNm, sortSn, useAt);
    }

    @Transactional
    public CodeResponse updateCode(String codeId, String groupId, String codeNm, int sortSn, String useAt, String userId) {
        CodeId id = new CodeId();
        id.setCodeId(codeId);
        id.setGroupId(groupId);

        Code code = codeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("코드를 찾을 수 없습니다."));
        code.setCodeNm(codeNm);
        code.setSortSn(sortSn);
        code.setUseAt(useAt);
        code.setUpdDt(LocalDateTime.now());
        code.setUpdUserId(userId);
        codeRepository.save(code);

        return new CodeResponse(codeId, groupId, codeNm, sortSn, useAt);
    }

    @Transactional
    public void deleteCode(String codeId, String groupId) {
        CodeId id = new CodeId();
        id.setCodeId(codeId);
        id.setGroupId(groupId);

        if (!codeRepository.existsById(id)) {
            throw new IllegalArgumentException("코드를 찾을 수 없습니다.");
        }
        codeRepository.deleteById(id);
    }
}
