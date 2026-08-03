package com.expenseTracker.attachfile.controller;

import com.expenseTracker.attachfile.dto.AttachFileDownload;
import com.expenseTracker.attachfile.dto.AttachFileResponse;
import com.expenseTracker.attachfile.service.AttachFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class AttachFileController {

    private final AttachFileService attachFileService;

    /** 파일 목록 업로드 */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<AttachFileResponse>> upload(
            @RequestParam(required = false) String fileGroupId,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(attachFileService.upload(fileGroupId, files, principal.getUsername()));
    }

    /** 파일그룹ID에 속한 첨부파일 목록 조회 */
    @GetMapping("/{fileGroupId}")
    public List<AttachFileResponse> list(@PathVariable String fileGroupId) {
        return attachFileService.findByGroup(fileGroupId);
    }

    /** 첨부파일 다운로드 */
    @GetMapping("/{fileGroupId}/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable String fileGroupId, @PathVariable String fileId) {
        AttachFileDownload download = attachFileService.download(fileGroupId, fileId);
        String encodedName = UriUtils.encode(download.orgFileNm(), StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName)
                .body(download.resource());
    }

    /** 첨부파일 삭제 */
    @DeleteMapping("/{fileGroupId}/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable String fileGroupId, @PathVariable String fileId) {
        attachFileService.delete(fileGroupId, fileId);
        return ResponseEntity.noContent().build();
    }
}
