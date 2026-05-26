package com.n8n.testlink.fsd.service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	private static final Logger log = LoggerFactory.getLogger(TestLinkService.class);
	
	public String uploadTestCasesFromXMLString(String userName, String devKey, String projectName, String testPlanName,
			String testSuiteName, String xmlContent) {
		
		try {
			
			log.info("============================📥 Upload In Progress ============================");
				
			String testLinkUrl = "https://ynt01.auruspay.com/lib/api/xmlrpc/v1/xmlrpc.php";
			log.info("Connecting to TestLink URL= {}", testLinkUrl);
			
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xmlContent)));
			doc.getDocumentElement().normalize();
			
			NodeList testCaseNodes = doc.getElementsByTagName("testcase");
			log.info("📊 Total test cases received: {}", testCaseNodes.getLength());
			
			TestLinkAPI api = new TestLinkAPI(new URL(testLinkUrl), devKey);
			
			
			TestProject[] projects = api.getProjects();
			Integer projectId = null;
			log.info("🔍 Searching for project: {}", projectName);
			for (TestProject p : projects) 
			{
				if (p.getName().equalsIgnoreCase(projectName)) 
				{
					projectId = p.getId();
					break;
				}
			}
			
			
			if (projectId == null) {
			    log.error("❌ Project not found: {}", projectName);
			    throw new IllegalArgumentException(
			    	    "Project not found: " + projectName + ". Please verify project name in TestLink."
			    	);
			}
			else
			{
				log.info("📥 Project found");
			}
			
			
			
			Integer suiteId = getOrCreateTestSuiteId(api, projectId, testSuiteName);
			int successCount = 0;
			int failCount = 0;
			List<String> failedCases = new ArrayList<>();
			
			log.info("📁 Using/Creating test suite: {}", testSuiteName);
			
			for (int i = 0; i < testCaseNodes.getLength(); i++) 
			{
				Element tcElement = (Element) testCaseNodes.item(i);
				String testCaseName = tcElement.getAttribute("name");
				String summary = getTagValue(tcElement, "summary");
				List<TestCaseStep> steps = new ArrayList<>();
				NodeList stepNodes = tcElement.getElementsByTagName("step");
				log.debug("✅ Created test case: {}", testCaseName);
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
				
				try {
					api.createTestCase(testCaseName, suiteId, projectId, userName, summary, steps, null,
							TestCaseStatus.FINAL, TestImportance.MEDIUM, ExecutionType.MANUAL, null, null, false,
							ActionOnDuplicate.BLOCK);
					successCount++;
				} 
				catch (Exception e) 
				{
				    failCount++;
				    failedCases.add(testCaseName);
				    log.error("❌ Failed to create test case: {}", testCaseName, e);
				}
			}
			
						
			int total = testCaseNodes.getLength();
			
			log.info("📊 Upload Summary | Total: {} Success: {} Failed: {}", total, successCount, failCount);
			
			return String.format(
			        "PROJECT NAME: %s" +
			        "TEST SUITE NAME: %s" +
			        "TOTAL TEST CASES: %d" +
			        "SUCCESSFULLY UPLOADED: %d" +
			        "FAILED COUNT: %d",
			        projectName,
			        testSuiteName,
			        total,
			        successCount,
			        failCount
			);
		} 
		
		catch (Exception e)
		{
		    log.error("🔥 FULL ERROR OCCURRED", e);

		    Throwable rootCause = e;

		    while (rootCause.getCause() != null)
		    {
		        rootCause = rootCause.getCause();
		    }

		    log.error("❌ ROOT CAUSE CLASS: {}", rootCause.getClass().getName());
		    log.error("❌ ROOT CAUSE MESSAGE: {}", rootCause.getMessage());

		    if (rootCause instanceof java.net.ConnectException)
		    {
		        log.error("🚫 CONNECTION REFUSED - Server cannot reach TestLink URL");
		        throw new RuntimeException(
		            "Cannot connect to TestLink server. Please check firewall/network."
		        );
		    }

		    if (rootCause instanceof java.net.UnknownHostException)
		    {
		        log.error("🌐 DNS ISSUE - Unable to resolve hostname");
		        throw new RuntimeException(
		            "Unable to resolve TestLink hostname."
		        );
		    }

		    if (rootCause instanceof javax.net.ssl.SSLHandshakeException)
		    {
		        log.error("🔒 SSL HANDSHAKE FAILED");
		        throw new RuntimeException(
		            "SSL certificate validation failed."
		        );
		    }

//		    if (e.getMessage() != null &&
//		        e.getMessage().toLowerCase().contains("developer key"))
//		    {
//		        log.error("🔑 INVALID DEVKEY");
//		        throw new RuntimeException("Invalid TestLink developer key.");
//		    }

		    log.error("🔥 GENERIC SYSTEM ERROR", e);

		    throw new RuntimeException("Failed to upload test cases: " + rootCause.getMessage(), e);
		        
		}
		

	}

	private Integer getOrCreateTestSuiteId(TestLinkAPI api, Integer projectId, String testSuiteName) throws Exception {
		TestSuite[] suites = api.getFirstLevelTestSuitesForTestProject(projectId);
		if (suites != null) {
			for (TestSuite s : suites) {
				if (s.getName().equalsIgnoreCase(testSuiteName)) {
					return s.getId();
				}
			}
		}
		TestSuite newSuite = api.createTestSuite(projectId, testSuiteName, "Created by Mjadhav", null, projectId, true, ActionOnDuplicate.BLOCK);
		return newSuite.getId();
	}

	private String getTagValue(Element element, String tagName) 
	{
		NodeList list = element.getElementsByTagName(tagName);
		if (list.getLength() > 0) 
		{
			return list.item(0).getTextContent();
		}
		return "";
	}
}
