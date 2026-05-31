package com.guts.Guts_IAM.auditlog.export.util;

import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class AuditExcelExporterForUser {

    public static XSSFWorkbook export(List<AuditLogDtoForUser> logs) {

        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("My Audit Logs");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Log ID");
        header.createCell(1).setCellValue("Action");
        header.createCell(2).setCellValue("Resource");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("Message");
        header.createCell(5).setCellValue("Audited On");
        header.createCell(6).setCellValue("IP Address");

        int rowNum = 1;

        for (AuditLogDtoForUser log : logs) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(log.getLogId());
            row.createCell(1).setCellValue(log.getLogAction().name());
            row.createCell(2).setCellValue(log.getResource());
            row.createCell(3).setCellValue(
                    log.getStatus() == null
                            ? "N/A"
                            : log.getStatus().name()
            );
            row.createCell(4).setCellValue(log.getMessage());

            row.createCell(5).setCellValue(
                    log.getAuditedOn() == null ?
                            "" :
                            log.getAuditedOn().toString()
            );

            row.createCell(6).setCellValue(log.getIpAddress());
        }

        for (int i = 0; i < 7; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}