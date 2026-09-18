package com.n8n.testlink.fsd.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n8n.testlink.fsd.model.BuildReportRequest;
import com.n8n.testlink.fsd.model.BuildReportResponse;
import com.n8n.testlink.fsd.service.ExcelReportService;
import com.n8n.testlink.fsd.service.TestLinkReportService;

@RestController
@CrossOrigin
@RequestMapping("/testlink/report")
public class TestLinkReportController {

    private final TestLinkReportService testLinkReportService;
    private final ExcelReportService excelReportService;

    public TestLinkReportController(TestLinkReportService testLinkReportService, ExcelReportService excelReportService) 
    {
        this.testLinkReportService = testLinkReportService;
        this.excelReportService = excelReportService;
    }

    @PostMapping(
            value = "/build",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    
    public ResponseEntity<byte[]> getBuild(@RequestBody BuildReportRequest request) throws Exception 
    {

        BuildReportResponse report =testLinkReportService.generateBuildReport(request);

        byte[] excel =excelReportService.generateExcel(report);

        String fileName = createFileName(
                request.getProjectName(),
                request.getTestPlanName(),
                request.getBuildName());

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());

        headers.setContentLength(excel.length);

        // Passed percentage
        headers.set("X-Passed-Percentage",String.format("%.2f%%", report.getPassedPercentage()));

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(excel);
    }

    private String createFileName(String projectName,String testPlanName,String buildName) 
    {

        return sanitize(projectName)
                + "_"
                + sanitize(testPlanName)
                + "_"
                + sanitize(buildName)
                + "_Execution_Report.xlsx";
    }

    private String sanitize(String value) 
    {

        if (value == null || value.isBlank()) 
        {
            return "Unknown";
        }

        return value.trim().replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }
}