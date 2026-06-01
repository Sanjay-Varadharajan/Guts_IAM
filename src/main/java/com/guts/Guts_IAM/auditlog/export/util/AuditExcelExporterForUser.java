package com.guts.Guts_IAM.auditlog.export.util;

import com.guts.Guts_IAM.auditlog.dto.AuditLogDtoForUser;
import com.guts.Guts_IAM.auditlog.model.AuditLog;
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

    public static XSSFWorkbook exportAll(List<AuditLog> logs) {

        XSSFWorkbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Audit Logs");

        Row header = sheet.createRow(0);

        header.createCell(0).setCellValue("Log ID");
        header.createCell(1).setCellValue("Action");
        header.createCell(2).setCellValue("User ID");
        header.createCell(3).setCellValue("User Email");
        header.createCell(4).setCellValue("Role");
        header.createCell(5).setCellValue("Resource");
        header.createCell(6).setCellValue("Resource ID");
        header.createCell(7).setCellValue("Status");
        header.createCell(8).setCellValue("Message");
        header.createCell(9).setCellValue("Audited On");
        header.createCell(10).setCellValue("IP Address");
        header.createCell(11).setCellValue("Location");
        header.createCell(12).setCellValue("Latitude");
        header.createCell(13).setCellValue("Longitude");
        header.createCell(14).setCellValue("User Agent");

        int rowNum = 1;

        for (AuditLog log : logs) {

            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(
                    log.getLogId() == null ? "" : String.valueOf(log.getLogId())
            );

            row.createCell(1).setCellValue(
                    log.getLogAction() == null
                            ? "N/A"
                            : log.getLogAction().name()
            );

            row.createCell(2).setCellValue(
                    log.getUserId() == null
                            ? ""
                            : String.valueOf(log.getUserId())
            );

            row.createCell(3).setCellValue(
                    log.getUserMail() == null
                            ? ""
                            : log.getUserMail()
            );

            row.createCell(4).setCellValue(
                    log.getRoleName() == null
                            ? ""
                            : log.getRoleName()
            );

            row.createCell(5).setCellValue(
                    log.getResource() == null
                            ? ""
                            : log.getResource()
            );

            row.createCell(6).setCellValue(
                    log.getResourceId() == null
                            ? ""
                            : log.getResourceId()
            );

            row.createCell(7).setCellValue(
                    log.getStatus() == null
                            ? "N/A"
                            : log.getStatus().name()
            );

            row.createCell(8).setCellValue(
                    log.getMessage() == null
                            ? ""
                            : log.getMessage()
            );

            row.createCell(9).setCellValue(
                    log.getAuditedOn() == null
                            ? ""
                            : log.getAuditedOn().toString()
            );

            row.createCell(10).setCellValue(
                    log.getIpAddress() == null
                            ? ""
                            : log.getIpAddress()
            );

            row.createCell(11).setCellValue(
                    log.getLocation() == null
                            ? ""
                            : log.getLocation()
            );

            row.createCell(12).setCellValue(
                    log.getLatitude() == null
                            ? ""
                            : String.valueOf(log.getLatitude())
            );

            row.createCell(13).setCellValue(
                    log.getLongitude() == null
                            ? ""
                            : String.valueOf(log.getLongitude())
            );

            row.createCell(14).setCellValue(
                    log.getUserAgent() == null
                            ? ""
                            : log.getUserAgent()
            );
        }

        for (int i = 0; i < 15; i++) {
            sheet.autoSizeColumn(i);
        }

        return workbook;
    }
}