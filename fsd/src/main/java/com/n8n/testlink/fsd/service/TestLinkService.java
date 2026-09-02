package com.n8n.testlink.fsd.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.ActionOnDuplicate;
import br.eti.kinoshita.testlinkjavaapi.constants.ExecutionType;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseStatus;
import br.eti.kinoshita.testlinkjavaapi.constants.TestImportance;
import br.eti.kinoshita.testlinkjavaapi.model.TestCaseStep;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;
import br.eti.kinoshita.testlinkjavaapi.model.TestSuite;

@Service
public class TestLinkService {

	@Value("${testlink.URL}")
	private String url;

	private static final Logger log = LoggerFactory.getLogger(TestLinkService.class);

	public Map<String, Object> uploadTestCasesFromXMLString(String userName, String devKey, String projectName,
			String testPlanName, String testSuiteName, String xmlContent) {

		try {

			log.info("============================📥 Upload In Progress ============================");

			// 1. Validate input
			if (projectName == null || projectName.isBlank()) 
			{
				throw new IllegalArgumentException("Project name is required.");
			}

			if (testSuiteName == null || testSuiteName.isBlank()) 
			{
				throw new IllegalArgumentException("Test suite name is required.");
			}

			if (xmlContent == null || xmlContent.isBlank()) 
			{
				throw new IllegalArgumentException("XML content is required.");
			}

			// 2. Connect to TestLink
			String testLinkUrl = url;
			log.info("Connecting to TestLink URL= {}", testLinkUrl);

			// 3. Parse XML
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlContent)));
			doc.getDocumentElement().normalize();
			NodeList testCaseNodes = doc.getElementsByTagName("testcase");
			int totalTestCases = testCaseNodes.getLength();
			log.info("📊 Total test cases received: {}", totalTestCases);

			if (totalTestCases == 0) 
			{
				throw new IllegalArgumentException("No testcase elements found in XML.");
			}

			// 4. Create TestLink API connection
			TestLinkAPI api = new TestLinkAPI(new URL(testLinkUrl), devKey);

			// 5. Get projects from TestLink
			log.info("📋 Fetching projects from TestLink...");

			TestProject[] projects = api.getProjects();

			if (projects == null) 
			{
				log.error("❌ TestLink returned NULL project list.");
				throw new RuntimeException("Unable to retrieve projects from TestLink.");
			}

			log.info("📋 Total projects returned by TestLink: {}", projects.length);

			// 7. Find requested project

			Integer projectId = null;
			log.info("🔍 Searching for project: {}", projectName);

			for (TestProject project : projects) 
			{

				if (project == null) 
				{
					continue;
				}

				String returnedProjectName = project.getName();
				log.debug("Comparing requested project [{}] with TestLink project [{}]", projectName,returnedProjectName);

				if (returnedProjectName != null && returnedProjectName.equalsIgnoreCase(projectName.trim())) 
				{

					projectId = project.getId();
					log.info("✅ Project found: {} | ID={}", returnedProjectName, projectId);
					break;
				}
			}

			// 8. Project not found

			if (projectId == null) 
			{

				log.error("❌ Project not found: {}", projectName);
				throw new IllegalArgumentException("Project not found: " + projectName + ". Please verify project name in TestLink.");
			}

			// 9. Get/Create test suite

			Integer suiteId = getOrCreateTestSuiteId(api, projectId, testSuiteName);

			log.info("📁 Using/Creating test suite: {} | ID={}", testSuiteName, suiteId);

			// 10. Upload test cases

			int successCount = 0;
			int failCount = 0;

			List<String> failedCases = new ArrayList<>();

			for (int i = 0; i < testCaseNodes.getLength(); i++) 
			{

				Element tcElement = (Element) testCaseNodes.item(i);
				String testCaseName = tcElement.getAttribute("name");
				String summary = getTagValue(tcElement, "summary");

				// Validate test case name

				if (testCaseName == null || testCaseName.isBlank()) 
				{
					log.warn("⚠️ Test case at index {} has no name. Skipping.", i);
					failCount++;
					failedCases.add("Unnamed test case at index " + i);
					continue;
				}

				// Build test steps

				List<TestCaseStep> steps = new ArrayList<>();

				NodeList stepNodes = tcElement.getElementsByTagName("step");

				log.debug("🧪 Preparing test case: {} | Steps={}", testCaseName, stepNodes.getLength());

				for (int j = 0; j < stepNodes.getLength(); j++) 
				{

					Element stepEl = (Element) stepNodes.item(j);
					TestCaseStep step = new TestCaseStep();
					step.setNumber(j + 1);
					step.setActions(getTagValue(stepEl, "actions"));
					step.setExpectedResults(getTagValue(stepEl, "expectedresults"));
					step.setExecutionType(ExecutionType.MANUAL);
					steps.add(step);
				}

				// Create test case in TestLink

				try {

				//	log.info("⬆️ Uploading test case: {}", testCaseName);

					api.createTestCase(testCaseName, suiteId, projectId, userName, summary, steps, null,
							TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.MANUAL, null, null, false,
							ActionOnDuplicate.BLOCK);

					successCount++;

					//log.info("✅ Created test case: {}", testCaseName);

				} catch (Exception e) {

					failCount++;

					failedCases.add(testCaseName);

					//log.error("❌ Failed to create test case: {}", testCaseName, e);
				}
			}

			// 11. Upload summary

			int total = testCaseNodes.getLength();

			log.info("📊 Upload Summary || Total: {} | Success: {} | Failed: {}", total, successCount, failCount);

			if (!failedCases.isEmpty()) {

				log.warn("❌ Failed test cases: {}", failedCases);
			}

			// 12. Build response

			Map<String, Object> response = new HashMap<>();

			response.put("PROJECT NAME", projectName);

			response.put("TEST SUITE NAME", testSuiteName);

			response.put("TOTAL TEST CASES", total);

			response.put("UPLOADED", successCount);

			response.put("FAILED", failCount);

			response.put("FAILED TEST CASES", failedCases);

			return response;

		}

		// GlobalExceptionHandler returns HTTP 400.

		catch (IllegalArgumentException e) {

			log.error("⚠️ Project error: {}", e.getMessage());

			throw e;
		}

		// Handle all other exceptions

		catch (Exception e) {

			log.error("🔥 FULL ERROR OCCURRED", e);

			Throwable rootCause = e;

			while (rootCause.getCause() != null) {
				rootCause = rootCause.getCause();
			}

			log.error("❌ ROOT CAUSE CLASS: {}", rootCause.getClass().getName());

			log.error("❌ ROOT CAUSE MESSAGE: {}", rootCause.getMessage());

			// Connection refused

			if (rootCause instanceof java.net.ConnectException) {

				log.error("🚫 CONNECTION REFUSED - Server cannot reach TestLink URL");

				throw new RuntimeException("Cannot connect to TestLink server. " + "Please check firewall/network.", e);
			}

			// DNS problem

			if (rootCause instanceof java.net.UnknownHostException) {

				log.error("🌐 DNS ISSUE - Unable to resolve hostname");

				throw new RuntimeException("Unable to resolve TestLink hostname.", e);
			}

			// SSL problem

			if (rootCause instanceof javax.net.ssl.SSLHandshakeException) {

				log.error("🔒 SSL HANDSHAKE FAILED");

				throw new RuntimeException("SSL certificate validation failed.", e);
			}

			// Developer key problem

			String errorMessage = rootCause.getMessage();

			if (errorMessage != null && errorMessage.toLowerCase().contains("developer key")) {

				log.error("🔑 INVALID DEVKEY");

				throw new RuntimeException("Invalid TestLink developer key.", e);
			}

			// Generic error

			log.error("🔥 GENERIC SYSTEM ERROR", e);

			throw new RuntimeException("Failed to upload test cases: " + rootCause.getMessage(), e);
		}
	}

	// GET OR CREATE TEST SUITE

	private Integer getOrCreateTestSuiteId(TestLinkAPI api, Integer projectId, String testSuiteName) throws Exception {

		log.info("🔍 Searching for test suite: {}", testSuiteName);

		TestSuite[] suites = api.getFirstLevelTestSuitesForTestProject(projectId);

		if (suites != null) {

			log.info("📋 TestLink returned {} test suites.", suites.length);

			for (TestSuite suite : suites) {

				if (suite == null) {
					continue;
				}

				log.debug("Available suite ID={} | Name={}", suite.getId(), suite.getName());

				if (suite.getName() != null && suite.getName().equalsIgnoreCase(testSuiteName.trim())) {

					log.info("✅ Test suite found: {} | ID={}", suite.getName(), suite.getId());

					return suite.getId();
				}
			}
		}

		// Suite doesn't exist -> create it

		log.info("📁 Test suite [{}] not found. Creating...", testSuiteName);

		TestSuite newSuite = api.createTestSuite(projectId, testSuiteName, "Created by Mjadhav", null, projectId, true,
				ActionOnDuplicate.BLOCK);

		if (newSuite == null) {

			throw new RuntimeException("Unable to create test suite: " + testSuiteName);
		}

		log.info("✅ Test suite created: {} | ID={}", newSuite.getName(), newSuite.getId());

		return newSuite.getId();
	}

	// GET XML TAG VALUE

	private String getTagValue(Element element, String tagName) {

		NodeList list = element.getElementsByTagName(tagName);

		if (list.getLength() > 0) {

			return list.item(0).getTextContent().trim();
		}

		return "";
	}
}