package com.expenseTracker.attachfile.dto;

import org.springframework.core.io.Resource;

public record AttachFileDownload(Resource resource, String orgFileNm) {}
