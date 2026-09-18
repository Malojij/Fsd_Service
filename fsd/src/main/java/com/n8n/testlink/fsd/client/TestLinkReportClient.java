package com.n8n.testlink.fsd.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.eti.kinoshita.testlinkjavaapi.TestLinkAPI;
import br.eti.kinoshita.testlinkjavaapi.constants.TestCaseDetails;
import br.eti.kinoshita.testlinkjavaapi.model.Build;
import br.eti.kinoshita.testlinkjavaapi.model.TestCase;
import br.eti.kinoshita.testlinkjavaapi.model.TestPlan;
import br.eti.kinoshita.testlinkjavaapi.model.TestProject;

@Component
public class TestLinkReportClient {

    private final TestLinkAPI testLinkAPI;

    public TestLinkReportClient(TestLinkAPI testLinkAPI) 
    {
        this.testLinkAPI = testLinkAPI;
    }

    public TestProject getProject(String projectName) throws Exception 
    {

        if (projectName == null || projectName.isBlank()) 
        {
            throw new IllegalArgumentException("Project name is required.");
        }

        TestProject project = testLinkAPI.getTestProjectByName(projectName.trim());

        if (project == null) 
        {
            throw new IllegalArgumentException("TestLink project not found: " + projectName);
        }

        return project;
    }

    public TestPlan getTestPlan(String projectName, String testPlanName) throws Exception 
    {

        if (testPlanName == null || testPlanName.isBlank()) 
        {
            throw new IllegalArgumentException("Test plan name is required.");
        }

        TestPlan testPlan =testLinkAPI.getTestPlanByName(testPlanName.trim(),projectName.trim());

        if (testPlan == null) 
        {
            throw new IllegalArgumentException("TestLink test plan not found: " + testPlanName);
        }

        return testPlan;
    }

    public Build getBuild(Integer testPlanId, String buildName) throws Exception 
    {

        if (testPlanId == null) 
        {
            throw new IllegalArgumentException("Test plan ID is required.");
        }

        if (buildName == null || buildName.isBlank()) 
        {
            throw new IllegalArgumentException("Build name is required.");
        }

        Build[] builds = testLinkAPI.getBuildsForTestPlan(testPlanId);

        if (builds == null || builds.length == 0)
        {
            throw new IllegalArgumentException("No builds found for test plan ID: " + testPlanId);
        }

        return Arrays.stream(builds)
                .filter(build -> build != null)
                .filter(build ->build.getName() != null && build.getName().trim().equalsIgnoreCase(buildName.trim()))
                .findFirst()
                .orElseThrow(() ->new IllegalArgumentException("Build not found: " + buildName));
    }

    public List<TestCase> getTestCases(Integer testPlanId, Integer buildId)throws Exception 
    {

        if (testPlanId == null) 
        {
            throw new IllegalArgumentException("Test plan ID is required.");
        }

        if (buildId == null) 
        {
            throw new IllegalArgumentException("Build ID is required.");
        }

        TestCase[] testCases = testLinkAPI.getTestCasesForTestPlan(
                testPlanId,
                null,
                buildId,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                TestCaseDetails.SUMMARY
        );

        if (testCases == null) 
        {
            return Collections.emptyList();
        }

        return Arrays.stream(testCases).filter(testCase -> testCase != null).collect(Collectors.toList());
    }


    public List<Map<String, Object>> getExecutionsForBuild(Integer testPlanId,Integer testCaseId,Integer buildId) throws Exception 
    {

        if (testPlanId == null) 
        {
            throw new IllegalArgumentException("Test plan ID is required.");
        }

        if (testCaseId == null) 
        {
            throw new IllegalArgumentException("Test case ID is required.");
        }

        if (buildId == null) 
        {
            throw new IllegalArgumentException("Build ID is required.");
        }

        Map<String, Object> params = new HashMap<>();

        params.put("devKey", testLinkAPI.getDevKey());
        params.put("testplanid", testPlanId);
        params.put("testcaseid", testCaseId);
        params.put("buildid", buildId);

        Map<String, Object> options = new HashMap<>();
        options.put("getBugs", 1);

        params.put("options", options);

        Object response = testLinkAPI.getXmlRpcClient().execute("tl.getAllExecutionsResults",new Object[] { params });

        List<Map<String, Object>> result = new ArrayList<>();

        if (response == null) 
        {
            return result;
        }

        if (response instanceof Object[]) 
        {
            Object[] executions = (Object[]) response;
            for (Object execution : executions) 
            {
                if (execution instanceof Map<?, ?>) 
                {
                    Map<?, ?> rawMap = (Map<?, ?>) execution;
                    Map<String, Object> executionMap = new HashMap<>();
                    for (Map.Entry<?, ?> entry : rawMap.entrySet()) 
                    {
                        if (entry.getKey() != null) 
                        {
                            executionMap.put(String.valueOf(entry.getKey()),entry.getValue());
                        }
                    }

                    result.add(executionMap);
                }
            }
        }

        return result;
    }
    

    public String getTestSuitePath(Integer testSuiteId) throws Exception 
    {

        if (testSuiteId == null) 
        {
            return "";
        }

        String[] path = testLinkAPI.getFullPath(testSuiteId);

        if (path == null || path.length == 0) 
        {
            return "";
        }

        return String.join(" / ", path);
    }
}