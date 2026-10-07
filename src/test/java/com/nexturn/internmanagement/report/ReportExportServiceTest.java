package com.nexturn.internmanagement.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

/**
 * Pure unit test (no Spring context) for the Excel export used by {@code GET /reports/export}:
 * verifies the generated workbook has the expected header row and correctly maps report fields
 * into cells, including null-safe handling of optional evaluation score / PPO status.
 */
class ReportExportServiceTest {

    private final ReportExportService service = new ReportExportService();

    @Test
    void exportsHeaderRowAndDataRows() throws Exception {
        InternReportDto withEvaluation = new InternReportDto(
                1L, "Alice", "Engineering", 10L, "Mentor One",
                8, 6, 75.0, 18, 20, 90.0, 4, 3, 75.0, 8.5, "Eligible");
        InternReportDto withoutEvaluation = new InternReportDto(
                2L, "Bob", "Design", 11L, "Mentor Two",
                0, 0, 0.0, 0, 0, 0.0, 0, 0, 0.0, null, null);

        byte[] excel = service.exportToExcel(List.of(withEvaluation, withoutEvaluation));

        assertThat(excel).isNotEmpty();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel))) {
            Sheet sheet = workbook.getSheet("Intern Reports");
            assertThat(sheet).isNotNull();

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Intern ID");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Intern Name");

            Row aliceRow = sheet.getRow(1);
            assertThat(aliceRow.getCell(1).getStringCellValue()).isEqualTo("Alice");
            assertThat(aliceRow.getCell(3).getStringCellValue()).isEqualTo("Mentor One");
            assertThat(aliceRow.getCell(13).getNumericCellValue()).isEqualTo(8.5);
            assertThat(aliceRow.getCell(14).getStringCellValue()).isEqualTo("Eligible");

            Row bobRow = sheet.getRow(2);
            assertThat(bobRow.getCell(13).getStringCellValue()).isEqualTo("N/A");
            assertThat(bobRow.getCell(14).getStringCellValue()).isEqualTo("N/A");
        }
    }

    @Test
    void handlesEmptyReportList() {
        byte[] excel = service.exportToExcel(List.of());
        assertThat(excel).isNotEmpty();
    }
}
