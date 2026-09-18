package com.n8n.testlink.fsd.model;

import java.util.ArrayList;
import java.util.List;

public class BuildReportResponse {

    private String projectName;
    private Integer projectId;

    private String testPlanName;
    private Integer testPlanId;

    private String buildName;
    private Integer buildId;
    private double passedPercentage;

    private int total;
    private int passed;
    private int failed;
    private int blocked;
    private int notRun;

    private List<ExecutionReportRow> executions = new ArrayList<>();

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getTestPlanName() {
        return testPlanName;
    }

    public void setTestPlanName(String testPlanName) {
        this.testPlanName = testPlanName;
    }

    public Integer getTestPlanId() {
        return testPlanId;
    }

    public void setTestPlanId(Integer testPlanId) {
        this.testPlanId = testPlanId;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public Integer getBuildId() {
        return buildId;
    }

    public void setBuildId(Integer buildId) {
        this.buildId = buildId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPassed() {
        return passed;
    }

    public void setPassed(int passed) {
        this.passed = passed;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getNotRun() {
        return notRun;
    }

    public void setNotRun(int notRun) {
        this.notRun = notRun;
    }

    public List<ExecutionReportRow> getExecutions() {
        return executions;
    }

    public void setExecutions(List<ExecutionReportRow> executions) {
        this.executions = executions;
    }
    
    public double getPassedPercentage() {
        return passedPercentage;
    }

    public void setPassedPercentage(double passedPercentage) {
        this.passedPercentage = passedPercentage;
    }
}