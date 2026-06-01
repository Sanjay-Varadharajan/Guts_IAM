package com.guts.Guts_IAM.auditlog.export.service;

import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.auditlog.export.util.AuditExcelExporterForUser;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
import com.guts.Guts_IAM.auditlog.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DownloadAuditLogService {

    private final AuditRepository auditRepository;

    public ByteArrayInputStream downloadMyLogs(String userMail)
            throws IOException {

        List<AuditLogDtoForUser> logs =
                auditRepository
                        .findByUserMailOrderByAuditedOnDesc(userMail)
                        .stream()
                        .map(AuditLogDtoForUser::new)
                        .toList();

        XSSFWorkbook workbook =
                AuditExcelExporterForUser.export(logs);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream downloadAllLogs()
            throws IOException {

        List<AuditLog> logs =
                auditRepository
                        .findAll();

        XSSFWorkbook workbook =
                AuditExcelExporterForUser.exportAll(logs);

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

}