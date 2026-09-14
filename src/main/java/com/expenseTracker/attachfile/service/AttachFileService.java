package com.expenseTracker.attachfile.service;

import com.expenseTracker.attachfile.dto.AttachFileDownload;
import com.expenseTracker.attachfile.dto.AttachFileResponse;
import com.expenseTracker.attachfile.entity.AttachFile;
import com.expenseTracker.attachfile.entity.AttachFileId;
import com.expenseTracker.attachfile.repository.AttachFileRepository;
import com.expenseTracker.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachFileService {

    // doc/xls/hwp는 모두 OLE 복합 문서 포맷이라 매직 넘버가 동일하고,
    // docx/xlsx는 모두 ZIP 컨테이너 포맷이라 매직 넘버가 동일함 (내부 구조까지는 검사하지 않음)
    private static final byte[] SIG_OLE = {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1};
    private static final byte[] SIG_ZIP = {0x50, 0x4B, 0x03, 0x04};
    private static final Map<String, List<byte[]>> EXTENSION_SIGNATURES = Map.ofEntries(
            Map.entry("jpg", List.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})),
            Map.entry("jpeg", List.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})),
            Map.entry("png", List.of(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})),
            Map.entry("gif", List.of(new byte[]{0x47, 0x49, 0x46, 0x38})),
            Map.entry("pdf", List.of(new byte[]{0x25, 0x50, 0x44, 0x46})),
            Map.entry("doc", List.of(SIG_OLE)),
            Map.entry("xls", List.of(SIG_OLE)),
            Map.entry("hwp", List.of(SIG_OLE)),
            Map.entry("docx", List.of(SIG_ZIP)),
            Map.entry("xlsx", List.of(SIG_ZIP))
    );

    private final AttachFileRepository attachFileRepository;

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.allowed-extensions:}")
    private String allowedExtensionsProp;

    @Value("${app.file.blocked-extensions:}")
    private String blockedExtensionsProp;

    private Set<String> allowedExtensions;
    private Set<String> blockedExtensions;

    /** 설정값으로부터 허용/차단 확장자 목록을 초기화 */
    @PostConstruct
    private void initExtensionRules() {
        allowedExtensions = parseExtensions(allowedExtensionsProp);
        blockedExtensions = parseExtensions(blockedExtensionsProp);
    }

    /** 콤마로 구분된 확장자 문자열을 소문자 Set으로 파싱 */
    private Set<String> parseExtensions(String prop) {
        return Arrays.stream(prop.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    /** 파일 목록을 업로드하고 저장 (그룹ID 없으면 신규 발급) */
    @Transactional
    public List<AttachFileResponse> upload(String fileGroupId, List<MultipartFile> files, String userId) {
        String groupId = StringUtils.hasText(fileGroupId) ? fileGroupId : UUID.randomUUID().toString();
        int sortSn = attachFileRepository.findByIdFileGroupIdOrderBySortSn(groupId).size();

        List<AttachFile> saved = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            saved.add(storeFile(groupId, file, ++sortSn, userId));
        }
        attachFileRepository.saveAll(saved);
        return saved.stream().map(this::toResponse).toList();
    }

    /** 파일그룹ID로 삭제되지 않은 첨부파일 목록 조회 */
    @Transactional(readOnly = true)
    public List<AttachFileResponse> findByGroup(String fileGroupId) {
        return attachFileRepository.findByIdFileGroupIdOrderBySortSn(fileGroupId)
                .stream()
                .filter(f -> "N".equals(f.getDelAt()))
                .map(this::toResponse)
                .toList();
    }

    /** 다운로드용 리소스와 원본 파일명을 조회 */
    @Transactional(readOnly = true)
    public AttachFileDownload download(String fileGroupId, String fileId) {
        AttachFile attachFile = getFile(fileGroupId, fileId);
        return new AttachFileDownload(loadAsResource(attachFile), attachFile.getOrgFileNm());
    }

    /** 첨부파일 논리 삭제 */
    @Transactional
    public void delete(String fileGroupId, String fileId) {
        getFile(fileGroupId, fileId).setDelAt("Y");
    }

    /** 파일그룹에 속한 첨부파일을 모두 논리 삭제 */
    @Transactional
    public void deleteByGroup(String fileGroupId) {
        attachFileRepository.findByIdFileGroupIdOrderBySortSn(fileGroupId).stream()
                .filter(f -> "N".equals(f.getDelAt()))
                .forEach(f -> f.setDelAt("Y"));
    }

    /** 삭제되지 않은 첨부파일을 그룹ID·파일ID로 조회, 없으면 예외 발생 */
    private AttachFile getFile(String fileGroupId, String fileId) {
        AttachFileId id = AttachFileId.builder()
                .fileGroupId(fileGroupId)
                .fileId(fileId)
                .build();

        return attachFileRepository.findById(id)
                .filter(f -> "N".equals(f.getDelAt()))
                .orElseThrow(() -> new BusinessException("파일을 찾을 수 없습니다."));
    }

    /** 저장경로로부터 다운로드 가능한 Resource를 로드 */
    private Resource loadAsResource(AttachFile attachFile) {
        try {
            Resource resource = new UrlResource(Path.of(attachFile.getFilePath()).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new BusinessException("파일을 읽을 수 없습니다.");
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new BusinessException("파일 경로가 올바르지 않습니다.");
        }
    }

    /** 확장자/시그니처 검증 후 파일을 디스크에 저장하고 엔티티를 생성 */
    private AttachFile storeFile(String fileGroupId, MultipartFile file, int sortSn, String userId) {
        String fileId = UUID.randomUUID().toString();
        String orgFileNm = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String fileExt = getExtension(orgFileNm);
        validateExtension(fileExt);
        validateSignature(fileExt, file);
        String saveFileNm = fileId + (fileExt.isEmpty() ? "" : "." + fileExt);

        Path dir = Path.of(uploadDir, fileGroupId);
        Path target = dir.resolve(saveFileNm);
        try {
            Files.createDirectories(dir);
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장에 실패했습니다.", e);
        }

        return AttachFile.builder()
                .id(AttachFileId.builder().fileGroupId(fileGroupId).fileId(fileId).build())
                .orgFileNm(orgFileNm)
                .saveFileNm(saveFileNm)
                .filePath(target.toString())
                .fileSize(file.getSize())
                .fileExt(fileExt)
                .sortSn(sortSn)
                .delAt("N")
                .rgsUserId(userId)
                .build();
    }

    /** 허용/차단 확장자 목록으로 확장자 유효성 검증 */
    private void validateExtension(String fileExt) {
        if (!allowedExtensions.isEmpty()) {
            if (!allowedExtensions.contains(fileExt)) {
                throw new BusinessException("허용되지 않는 파일 형식입니다: " + fileExt);
            }
            return;
        }
        if (blockedExtensions.contains(fileExt)) {
            throw new BusinessException("허용되지 않는 파일 형식입니다: " + fileExt);
        }
    }

    /** 파일 바이너리 시그니처가 확장자와 일치하는지 검증 */
    private void validateSignature(String fileExt, MultipartFile file) {
        List<byte[]> signatures = EXTENSION_SIGNATURES.get(fileExt);
        if (signatures == null) return;

        int headerLen = signatures.stream().mapToInt(sig -> sig.length).max().orElse(0);
        byte[] header = readHeader(file, headerLen);
        boolean matched = signatures.stream().anyMatch(sig -> startsWith(header, sig));
        if (!matched) {
            throw new BusinessException("파일 내용이 확장자와 일치하지 않습니다: " + fileExt);
        }
    }

    /** 파일 앞부분 지정 길이만큼 바이트를 읽음 */
    private byte[] readHeader(MultipartFile file, int length) {
        try (InputStream is = file.getInputStream()) {
            byte[] buf = new byte[length];
            int read = is.readNBytes(buf, 0, length);
            return read == length ? buf : Arrays.copyOf(buf, read);
        } catch (IOException e) {
            throw new UncheckedIOException("파일을 읽을 수 없습니다.", e);
        }
    }

    /** data가 prefix로 시작하는지 확인 */
    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    /** 파일명에서 확장자를 추출 (없으면 빈 문자열) */
    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx == -1 ? "" : fileName.substring(idx + 1).toLowerCase();
    }

    /** AttachFile 엔티티를 응답 DTO로 변환 */
    private AttachFileResponse toResponse(AttachFile f) {
        return new AttachFileResponse(
                f.getId().getFileGroupId(),
                f.getId().getFileId(),
                f.getOrgFileNm(),
                f.getFileSize() != null ? f.getFileSize() : 0,
                f.getFileExt(),
                f.getSortSn() != null ? f.getSortSn() : 0);
    }
}
