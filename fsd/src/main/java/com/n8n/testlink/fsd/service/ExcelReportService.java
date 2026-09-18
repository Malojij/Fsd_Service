package com.n8n.testlink.fsd.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.n8n.testlink.fsd.model.BuildReportResponse;
import com.n8n.testlink.fsd.model.ExecutionReportRow;

@Service
public class ExcelReportService {
	
	private static final Logger log = LoggerFactory.getLogger(TestLinkReportService.class);

	
	public byte[] generateExcel(BuildReportResponse report) throws IOException 
    {

        try (Workbook workbook = new XSSFWorkbook())
        {

            createSummarySheet(workbook,report);
            createExecutionSheet(workbook,report);
            
            try (ByteArrayOutputStream outputStream =new ByteArrayOutputStream())
            {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private void createSummarySheet(Workbook workbook,BuildReportResponse report) 
    {

        Sheet sheet = workbook.createSheet("Summary");
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        CellStyle percentageStyle = createPercentageStyle(workbook);

        // Remove gridlines
        sheet.setDisplayGridlines(false);

        Row titleRow = sheet.createRow(0);

        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("TestLink Build Execution Report");
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        0,
                        0,
                        3));

        titleRow.setHeightInPoints(28);

        // =========================================================
        // METADATA
        // =========================================================

        int rowNumber = 2;

        rowNumber = addMetadataRow(sheet,rowNumber,"Project",report.getProjectName(),headerStyle,normalStyle);
        rowNumber = addMetadataRow( sheet, rowNumber, "Project ID", String.valueOf(report.getProjectId()), headerStyle, normalStyle);
        rowNumber = addMetadataRow( sheet, rowNumber, "Test Plan", report.getTestPlanName(), headerStyle, normalStyle); 
        rowNumber = addMetadataRow( sheet, rowNumber, "Test Plan ID", String.valueOf(report.getTestPlanId()), headerStyle, normalStyle); 
        rowNumber = addMetadataRow( sheet, rowNumber, "Build", report.getBuildName(), headerStyle, normalStyle); 
        rowNumber = addMetadataRow( sheet, rowNumber, "Build ID", String.valueOf(report.getBuildId()), headerStyle, normalStyle);

        rowNumber++;

        // =========================================================
        // EXECUTION SUMMARY HEADER
        // =========================================================

        Row summaryHeader = sheet.createRow(rowNumber++);
        createCell( summaryHeader, 0, "Execution Status", headerStyle); 
        createCell( summaryHeader, 1, "Count", headerStyle);

        // =========================================================
        // SUMMARY DATA
        // =========================================================

        addSummaryRow( sheet, rowNumber++, "Total", report.getTotal(), normalStyle); 
        addSummaryRow( sheet, rowNumber++, "Passed", report.getPassed(), normalStyle); 
        addSummaryRow( sheet, rowNumber++, "Failed", report.getFailed(), normalStyle); 
        addSummaryRow( sheet, rowNumber++, "Blocked", report.getBlocked(), normalStyle); 
        addSummaryRow( sheet, rowNumber++, "Not Run", report.getNotRun(), normalStyle);

        // =========================================================
        // PASSED PERCENTAGE
        // =========================================================

        Row percentageRow = sheet.createRow(rowNumber);
        createCell( percentageRow, 0, "Passed %", headerStyle);
        Cell percentageCell = percentageRow.createCell(1);
        percentageCell.setCellValue(report.getPassedPercentage() / 100.0);
        percentageCell.setCellStyle(percentageStyle);

        // =========================================================
        // COLUMN WIDTH
        // =========================================================

        sheet.setColumnWidth(0, 28 * 256);
        sheet.setColumnWidth(1, 22 * 256);
        sheet.setColumnWidth(2, 5 * 256);
        sheet.setColumnWidth(3, 5 * 256);
        sheet.createFreezePane(0, 2);
    }

    private void createExecutionSheet(Workbook workbook,BuildReportResponse report) 
    {

        Sheet sheet = workbook.createSheet("Execution Details");
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);
        sheet.setDisplayGridlines(false);

        String[] headers = {

                "Execution ID",
                "Test Case ID",
                "External ID",
                "Full External ID",
                "Test Case Name",
                "Test Suite",
                "Test Case Version",
                "Test Case Version ID",
                "Test Plan ID",
                "Build ID",
                "Build Name",
                "Status",
                "Tester ID",
                "Executed By",
                "Execution Timestamp",
                "Execution Type",
                "Platform",
                "Test Case Status",
                "Test Case Summary",
                "Execution Notes"
        };

        // =========================================================
        // HEADER
        // =========================================================

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < headers.length; i++) 
        {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        headerRow.setHeightInPoints(35);

        // =========================================================
        // DATA
        // =========================================================

        int rowNumber = 1;

        for (ExecutionReportRow data : report.getExecutions()) 
        {
            Row row = sheet.createRow(rowNumber++);

            setCell(row, 0, data.getExecutionId());
            setCell(row, 1, data.getTestCaseId());
            setCell(row, 2, data.getTestCaseExternalId());
            setCell(row, 3, data.getTestCaseExternalIdFull());
            setCell(row, 4, data.getTestCaseName());
            setCell(row, 5, data.getTestSuite());
            setCell(row, 6, data.getTestCaseVersion());
            setCell(row, 7, data.getTestCaseVersionId());
            setCell(row, 8, data.getTestPlanId());
            setCell(row, 9, data.getBuildId());
            setCell(row, 10, data.getBuildName());
            setCell(row, 11, data.getStatus());
            setCell(row, 12, data.getTesterId());
            setCell(row, 13, data.getExecutedBy());
            setCell(row, 14, data.getExecutionTimestamp());
            setCell(row, 15, data.getExecutionType());
            setCell(row, 16, data.getPlatform());
            setCell(row, 17, data.getTestCaseStatus());
            setCell(row, 18, data.getTestCaseSummary());
            setCell(row, 19, data.getNotes());

            
            log.info("Executior{}", data.getExecutedBy());
            
            // Apply proper alignment
            for (int i = 0; i < headers.length; i++) 
            {
                Cell cell = row.getCell(i);
                if (cell == null) 
                {
                    cell = row.createCell(i);
                }

                cell.setCellStyle(normalStyle);

                if (i == 0 || i == 1 || i == 2 || i == 6 || i == 7 || i == 8 || i == 9 || i == 12 || i==13)
                {

                    cell.getCellStyle();
                    CellStyle centeredStyle = createCenteredStyle(workbook);
                    cell.setCellStyle(centeredStyle);

                } else 
                {
                    cell.setCellStyle(normalStyle);
                }
            }

            // Highlight execution status
            applyStatusStyle(workbook,row.getCell(11),data.getStatus());
        }

        // =========================================================
        // FILTER
        // =========================================================

        sheet.setAutoFilter(
                new org.apache.poi.ss.util.CellRangeAddress(
                        0,
                        Math.max(0, rowNumber - 1),
                        0,
                        headers.length - 1));

        // Freeze header
        sheet.createFreezePane(0, 1);

        // =========================================================
        // COLUMN WIDTHS
        // =========================================================

        int[] widths = {

                16, // Execution ID
                16, // Test Case ID
                20, // External ID
                28, // Full External ID
                38, // Test Case Name
                32, // Test Suite
                18, // Version
                22, // Version ID
                16, // Plan ID
                16, // Build ID
                25, // Build Name
                15, // Status
                15, // Tester ID
                25, // Timestamp
                20, // Execution Type
                20, // Platform
                22, // Test Case Status
                55, // Summary
                55  // Notes
        };

        for (int i = 0; i < widths.length; i++) 
        {
            sheet.setColumnWidth(i,widths[i] * 256);
        }

        // =========================================================
        // PRINT SETTINGS
        // =========================================================

        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
        sheet.setAutobreaks(true);
    }

    private int addMetadataRow(Sheet sheet,int rowNumber,String key,String value,CellStyle headerStyle,CellStyle normalStyle) 
    {
        Row row =sheet.createRow(rowNumber);
        createCell(row,0,key,headerStyle);
        createCell(row,1,value,normalStyle);
        return rowNumber + 1;
    }

    private void addSummaryRow(Sheet sheet,int rowNumber,String status,int count,CellStyle style) 
    {
        Row row =sheet.createRow(rowNumber);
        createCell(row,0,status,style);
        createCell(row,1,count,style);
    }
    
    @SuppressWarnings("unused")
	private void addPercentageRow(Sheet sheet, int rowNumber,String label,double percentage,CellStyle style) 
    {

        Row row = sheet.createRow(rowNumber);
        createCell(row,0,label,style);
        Cell cell = row.createCell(1);
        cell.setCellValue(percentage / 100.0);
        CellStyle percentageStyle = sheet.getWorkbook().createCellStyle();
        percentageStyle.cloneStyleFrom(style);
        percentageStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("0.00%"));
        cell.setCellStyle(percentageStyle);
    }

    private void createCell(Row row,int column,String value,CellStyle style) 
    {
        Cell cell =row.createCell(column);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row,int column,int value,CellStyle style) 
    {
        Cell cell =row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void setCell(Row row,int column,Object value) 
    {
        Cell cell =row.createCell(column);

        if (value == null) 
        {
            cell.setCellValue("");
        } 
        else if (value instanceof Number number) 
        {
            cell.setCellValue(number.doubleValue());
        }
        else 
        {
            cell.setCellValue(String.valueOf(value));
        }
    }

    private CellStyle createTitleStyle(Workbook workbook) 
    {

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) 
    {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        // Clean light background
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());

        return style;
    }

    private CellStyle createNormalStyle(Workbook workbook) 
    {

        CellStyle style = workbook.createCellStyle();

        style.setFont(createNormalFont(workbook));
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());

        return style;
    }
    
    private CellStyle createCenteredStyle(Workbook workbook)
    {
        CellStyle style = createNormalStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }
    
    private Font createNormalFont(Workbook workbook) 
    {
        Font font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);

        return font;
    }
    
    private CellStyle createPercentageStyle(Workbook workbook) 
    {
        CellStyle style =createCenteredStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00%"));
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        return style;
    }
    
    private void applyStatusStyle(Workbook workbook,Cell cell,String status) 
    {
        if (cell == null) 
        {
            return;
        }

        if (status == null) 
        {
            return;
        }

        CellStyle style =createCenteredStyle(workbook);
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String value =status.trim().toUpperCase();

        switch (value) 
        {
            case "P":
            case "PASSED":style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
                break;

            case "F":
            case "FAILED":style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
                break;

            case "B":
            case "BLOCKED":style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                break;

            case "N":
            case "NOT_RUN":
            case "NOT RUN":style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                break;

            default:style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        }

        cell.setCellStyle(style);
    }
}