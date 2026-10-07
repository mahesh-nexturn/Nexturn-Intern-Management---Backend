package com.nexturn.internmanagement.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

/** Generates an Excel (.xlsx) export of intern performance reports using Apache POI. */
@Service
public class ReportExportService {

    private static final String[] HEADERS = {
        "Intern ID", "Intern Name", "Department", "Mentor", "Total Tasks", "Completed Tasks",
        "Task Completion %", "Present Days", "Total Attendance Days", "Attendance %",
        "Total Trainings", "Completed Trainings", "Training Completion %", "Avg Evaluation Score", "PPO Status"
    };

    public byte[] exportToExcel(List<InternReportDto> reports) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Intern Reports");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (InternReportDto r : reports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.internId());
                row.createCell(1).setCellValue(r.internName());
                row.createCell(2).setCellValue(r.department() == null ? "" : r.department());
                row.createCell(3).setCellValue(r.mentorName() == null ? "Unassigned" : r.mentorName());
                row.createCell(4).setCellValue(r.totalTasks());
                row.createCell(5).setCellValue(r.completedTasks());
                row.createCell(6).setCellValue(round(r.taskCompletionPct()));
                row.createCell(7).setCellValue(r.presentDays());
                row.createCell(8).setCellValue(r.totalAttendanceDays());
                row.createCell(9).setCellValue(round(r.attendancePct()));
                row.createCell(10).setCellValue(r.totalTrainings());
                row.createCell(11).setCellValue(r.completedTrainings());
                row.createCell(12).setCellValue(round(r.trainingCompletionPct()));
                if (r.averageEvaluationScore() != null) {
                    row.createCell(13).setCellValue(round(r.averageEvaluationScore()));
                } else {
                    row.createCell(13).setCellValue("N/A");
                }
                row.createCell(14).setCellValue(r.ppoStatus() == null ? "N/A" : r.ppoStatus());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate report export", e);
        }
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
