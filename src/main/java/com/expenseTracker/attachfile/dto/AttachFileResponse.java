package com.expenseTracker.attachfile.dto;

public record AttachFileResponse(
        String fileGroupId,
        String fileId,
        String orgFileNm,
        long fileSize,
        String fileExt,
        int sortSn
) {}
