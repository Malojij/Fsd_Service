package com.n8n.testlink.fsd.model;

public class BuildReportRequest {

    private String projectName;
    private String testPlanName;
    private String buildName;

    public BuildReportRequest() 
    {
    }

    public BuildReportRequest(String projectName, String testPlanName, String buildName) 
    {
        this.projectName = projectName;
        this.testPlanName = testPlanName;
        this.buildName = buildName;
    }

    public String getProjectName() 
    {
        return projectName;
    }

    public void setProjectName(String projectName) 
    {
        this.projectName = projectName;
    }

    public String getTestPlanName() 
    {
        return testPlanName;
    }

    public void setTestPlanName(String testPlanName) 
    {
        this.testPlanName = testPlanName;
    }

    public String getBuildName() 
    {
        return buildName;
    }

    public void setBuildName(String buildName) 
    {
        this.buildName = buildName;
    }
}