package com.n8n.testlink.fsd.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.n8n.testlink.fsd.client.TestLinkReportClient;
import com.n8n.testlink.fsd.model.BuildReportRequest;
import com.n8n.testlink.fsd.model.BuildReportResponse;
import com.n8n.testlink.fsd.model.ExecutionReportRow;

import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionStatus;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.Execution;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

@Service
public class TestLinkReportService {

	private static final Logger log = LoggerFactory.getLogger(TestLinkReportService.class);
	private final TestLinkReportClient client;

	public TestLinkReportService(TestLinkReportClient client) 
	{
		this.client = client;
	}

	
	public BuildReportResponse generateBuildReport(BuildReportRequest request) throws Exception 
	{
		validateRequest(request);

		log.info("=====================MJ==========================");
		log.info("TestLink Build Report generation started");
		log.info("Project  : {}", request.getProjectName());
		log.info("Plan     : {}", request.getTestPlanName());
		log.info("Build    : {}", request.getBuildName());
		log.info("=====================MJ==========================");

		TestProject project = client.getProject(request.getProjectName());
		TestPlan testPlan = client.getTestPlan(request.getProjectName(), request.getTestPlanName());
		Build build = client.getBuild(testPlan.getId(), request.getBuildName());

		List<TestCase> testCases = client.getTestCases(testPlan.getId(), build.getId());

		BuildReportResponse response = new BuildReportResponse();

		response.setProjectName(project.getName());
		response.setProjectId(project.getId());

		response.setTestPlanName(testPlan.getName());
		response.setTestPlanId(testPlan.getId());

		response.setBuildName(build.getName());
		response.setBuildId(build.getId());

		List<ExecutionReportRow> rows = new ArrayList<>();

		for (TestCase testCase : testCases) 
		{

			try {

				log.info("Retrieving executions for TestCase ID={}, Name={}", testCase.getId(), testCase.getName());

				List<Map<String, Object>> executions = client.getExecutionsForBuild(testPlan.getId(), testCase.getId(),build.getId());

				if (executions == null || executions.isEmpty()) 
				{
				    ExecutionReportRow row = createRowFromTestCase(testCase,testPlan,build);
				    String status = "NOT_RUN";

				    if (testCase.getExecutionStatus() != null) 
				    {
				        status = testCase.getExecutionStatus().toString();
				    }

				    if (testCase.getExecutionStatus() != null) 
				    {
			            status = normalizeStatus(testCase.getExecutionStatus().toString());
			        }
				    
				    row.setStatus(status);
				    row.setNotes("Execution details were not returned by TestLink API. "+ "Status obtained from TestCase execution status.");
				    rows.add(row);
				    log.info("No execution details found for TestCase ID={}",testCase.getId());
				    log.info("Using TestCase execution status={}",status + "\n");
				}

				
				for (Map<String, Object> executionData : executions) 
				{
					log.info("Execution found for TestCase ID={} : {}", testCase.getId(), executionData);
					ExecutionReportRow row = createRowFromXmlRpc(testCase, executionData, testPlan.getId(),build.getId(), build.getName());
					rows.add(row);
				}

			} catch (Exception e) 
			{

				log.warn("Unable to retrieve executions for test case ID={} Name={}", testCase.getId(),testCase.getName(), e);
				ExecutionReportRow row = createRow(testCase, null, testPlan, build);
				row.setStatus(testCase.getExecutionStatus() != null ? testCase.getExecutionStatus().toString()
						: ExecutionStatus.NOT_RUN.toString());
				row.setNotes("Unable to retrieve execution details: " + e.getMessage());
				rows.add(row);
			}
		}

		response.setExecutions(rows);
		response.setTotal(rows.size());
		calculateSummary(response);

		/**log.info("Report completed. Total={}, Passed={}, Failed={}, Blocked={}, NotRun={}", response.getTotal(),
				response.getPassed(), response.getFailed(), response.getBlocked(), response.getNotRun());**/

		return response;
	}

	private String normalizeStatus(String status) {

	    if (status == null || status.isBlank())  
	    {
	        return "Not Run";
	    }

	    switch (status.trim().toLowerCase()) 
	    {

	        case "p":
	        case "passed":return "Passed";

	        case "f":
	        case "failed":return "Failed";

	        case "b":
	        case "blocked":return "Blocked";

	        case "n":
	        case "not_run":
	        case "not run":return "Not Run";

	        default: return status;
	    }
	}
	private ExecutionReportRow createRow(TestCase testCase, Execution execution, TestPlan testPlan, Build build)throws Exception 
	{

		ExecutionReportRow row = new ExecutionReportRow();
		row.setTestCaseId(testCase.getId());
		row.setTestCaseExternalId(testCase.getExternalId());
		row.setTestCaseExternalIdFull(testCase.getFullExternalId());
		row.setTestCaseName(testCase.getName());
		row.setTestCaseVersion(testCase.getVersion());
		row.setTestCaseVersionId(testCase.getVersionId());
		row.setTestPlanId(testPlan.getId());
		row.setBuildId(build.getId());
		row.setBuildName(build.getName());

		if (testCase.getTestSuiteId() != null) 
		{
			row.setTestSuite(client.getTestSuitePath(testCase.getTestSuiteId()));
		}

	
		if (testCase.getPlatform() != null) 
		{
			row.setPlatform(testCase.getPlatform().getName());
		}

	
		if (testCase.getTestCaseStatus() != null) 
		{
			row.setTestCaseStatus(testCase.getTestCaseStatus().toString());
		}

		row.setTestCaseSummary(testCase.getSummary());

		if (execution == null) 
		{

			ExecutionStatus status = testCase.getExecutionStatus();
			row.setStatus(status == null ? ExecutionStatus.NOT_RUN.toString() : status.toString());
			return row;
		}


		row.setExecutionId(execution.getId());
		row.setStatus(execution.getStatus() == null ? null : execution.getStatus().toString());
		row.setTesterId(execution.getTesterId());
		row.setTestCaseVersionId(execution.getTestCaseVersionId());
		row.setTestCaseVersion(execution.getTestCaseVersionNumber());
		row.setExecutionType(execution.getExecutionType() == null ? null : execution.getExecutionType().toString());
		row.setNotes(execution.getNotes());

		if (execution.getExecutionTimeStamp() != null) 
		{
			row.setExecutionTimestamp(formatDate(execution.getExecutionTimeStamp()));
		}

		return row;
	}

	private ExecutionReportRow createRowFromXmlRpc(TestCase testCase, Map<String, Object> data, Integer testPlanId,Integer buildId, String buildName) throws Exception 
	{

		ExecutionReportRow row = new ExecutionReportRow();

		row.setTestCaseId(testCase.getId());
		row.setTestCaseExternalId(testCase.getExternalId());
		row.setTestCaseExternalIdFull(testCase.getFullExternalId());
		row.setTestCaseName(testCase.getName());
		row.setTestCaseVersion(testCase.getVersion());
		row.setTestCaseVersionId(testCase.getVersionId());


		row.setTestPlanId(testPlanId);
		row.setBuildId(buildId);
		row.setBuildName(buildName);
		row.setTesterId(getInteger(data, "tester_id"));

		if (testCase.getTestSuiteId() != null) 
		{
			row.setTestSuite(client.getTestSuitePath(testCase.getTestSuiteId()));
		}


		if (testCase.getTestCaseStatus() != null) 
		{
			row.setTestCaseStatus(testCase.getTestCaseStatus().toString());
		}


		if (testCase.getExecutionType() != null)
		{
			row.setExecutionType(testCase.getExecutionType().toString());
		}

		row.setTestCaseSummary(testCase.getSummary());
		row.setExecutionId(getInteger(data, "id"));
		row.setStatus(getString(data, "status"));
		row.setTesterId(getInteger(data, "tester_id"));
		row.setExecutionTimestamp(getString(data, "execution_ts"));
		row.setNotes(getString(data, "notes"));
		row.setPlatform(getString(data, "platform_name"));
		return row;
	}

	private Integer getInteger(Map<String, Object> data, String key) 
	{

		if (data == null)
		{
			return null;
		}

		Object value = data.get(key);

		if (value == null) 
		{
			return null;
		}

		if (value instanceof Number) 
		{
			return ((Number) value).intValue();
		}

		try 
		{
			return Integer.valueOf(String.valueOf(value));

		} catch (NumberFormatException e) 
		{
			return null;
		}
	}

	private String getString(Map<String, Object> data, String key) 
	{

		if (data == null) 
		{
			return null;
		}

		Object value = data.get(key);

		if (value == null) 
		{
			return null;
		}

		return String.valueOf(value);
	}

	private void calculateSummary(BuildReportResponse response) 
	{

	    int passed = 0;
	    int failed = 0;
	    int blocked = 0;
	    int notRun = 0;

	    for (ExecutionReportRow row : response.getExecutions()) 
	    {
	        String status = row.getStatus();

	        if (status == null) 
	        {
	            notRun++;
	            continue;
	        }

	        switch (status.trim().toUpperCase()) 
	        {
	            case "PASSED":
	            case "P":passed++;
	                break;

	            case "FAILED":
	            case "F":failed++;
	                break;

	            case "BLOCKED":
	            case "B":blocked++;
	                break;

	            case "NOT_RUN":
	            case "NOT RUN":
	            case "N":
	            default:notRun++;
	                break;
	        }
	    }

	    int total = response.getExecutions().size();

	    response.setTotal(total);
	    response.setPassed(passed);
	    response.setFailed(failed);
	    response.setBlocked(blocked);
	    response.setNotRun(notRun);

	    double passedPercentage = 0.0;

	    if (total > 0) 
	    {
	        passedPercentage = (passed * 100.0) / total;
	    }

	    response.setPassedPercentage(passedPercentage);

	    log.info("Report completed. Total={}, Passed={}, Failed={}, Blocked={}, NotRun={}, PassedPercentage={}%",
	            total,
	            passed,
	            failed,
	            blocked,
	            notRun,
	            String.format("%.2f", passedPercentage));
	}

	private String formatDate(Date date)
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

	private void validateRequest(BuildReportRequest request) 
	{

		if (request == null) 
		{
			throw new IllegalArgumentException("Request body is required.");
		}

		if (request.getProjectName() == null || request.getProjectName().isBlank()) 
		{
			throw new IllegalArgumentException("projectName is required.");
		}

		if (request.getTestPlanName() == null || request.getTestPlanName().isBlank()) 
		{
			throw new IllegalArgumentException("testPlanName is required.");
		}

		if (request.getBuildName() == null || request.getBuildName().isBlank()) 
		{
			throw new IllegalArgumentException("buildName is required.");
		}
	}
	
	private ExecutionReportRow createRowFromTestCase(TestCase testCase,TestPlan testPlan,Build build) throws Exception 
	{

	    ExecutionReportRow row = new ExecutionReportRow();
	    row.setExecutionId(null);
	    row.setTestCaseId(testCase.getId());
	    row.setTestCaseExternalId(testCase.getExternalId());
	    row.setTestCaseExternalIdFull(testCase.getFullExternalId());

	    row.setTestCaseName(testCase.getName());
	    row.setTestSuite(client.getTestSuitePath(testCase.getTestSuiteId()));
	    row.setTestCaseVersion(testCase.getVersion());
	    row.setTestCaseVersionId(testCase.getVersionId());
	    row.setTestPlanId(testPlan.getId());
	    row.setBuildId(build.getId());
	    row.setBuildName(build.getName());

	    if (testCase.getExecutionStatus() != null) 
	    {
	        row.setStatus(testCase.getExecutionStatus().toString());
	    } else 
	    {
	        row.setStatus("NOT_RUN");
	    }

	    row.setExecutionType(testCase.getExecutionType() != null? testCase.getExecutionType().toString(): null);
	    row.setPlatform(testCase.getPlatform() != null? testCase.getPlatform().getName(): null);
	    row.setTestCaseStatus(testCase.getTestCaseStatus() != null? testCase.getTestCaseStatus().toString(): null);

	    row.setTestCaseSummary(testCase.getSummary());

	    row.setNotes("Execution details not available from TestLink API.");
	    return row;
	}
}