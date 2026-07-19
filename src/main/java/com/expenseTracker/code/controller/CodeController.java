package com.expenseTracker.code.controller;

import com.expenseTracker.code.dto.AddCodeRequest;
import com.expenseTracker.code.dto.AddGroupRequest;
import com.expenseTracker.code.dto.CodeResponse;
import com.expenseTracker.code.dto.GroupResponse;
import com.expenseTracker.code.dto.UpdateCodeRequest;
import com.expenseTracker.code.dto.UpdateGroupRequest;
import com.expenseTracker.code.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/code")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @GetMapping("/groups")
    public List<GroupResponse> getGroups(@RequestParam(defaultValue = "") String keyword) {
        if (keyword.isBlank()) return codeService.findAllGroups();
        return codeService.searchGroups(keyword);
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse addGroup(@RequestBody AddGroupRequest req, Authentication auth) {
        return codeService.addGroup(req.groupId(), req.groupNm(), auth.getName());
    }

    @PatchMapping("/groups/{groupId}")
    public GroupResponse updateGroup(@PathVariable String groupId,
                                     @RequestBody UpdateGroupRequest req,
                                     Authentication auth) {
        return codeService.updateGroup(groupId, req.groupNm(), auth.getName());
    }

    @DeleteMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable String groupId) {
        codeService.deleteGroup(groupId);
    }

    @GetMapping("/groups/{groupId}/items")
    public List<CodeResponse> getItems(@PathVariable String groupId) {
        return codeService.findCodesByGroup(groupId);
    }

    @PostMapping("/groups/{groupId}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CodeResponse addItem(@PathVariable String groupId,
                                @RequestBody AddCodeRequest req,
                                Authentication auth) {
        return codeService.addCode(groupId, req.codeNm(), req.sortSn(), req.useAt(), auth.getName());
    }

    @PutMapping("/groups/{groupId}/items/{codeId}")
    public CodeResponse updateItem(@PathVariable String groupId,
                                   @PathVariable String codeId,
                                   @RequestBody UpdateCodeRequest req,
                                   Authentication auth) {
        return codeService.updateCode(codeId, groupId, req.codeNm(), req.sortSn(), req.useAt(), auth.getName());
    }

    @DeleteMapping("/groups/{groupId}/items/{codeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable String groupId, @PathVariable String codeId) {
        codeService.deleteCode(codeId, groupId);
    }
}
