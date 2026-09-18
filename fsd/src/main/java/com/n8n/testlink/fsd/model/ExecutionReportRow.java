package com.n8n.testlink.fsd.model;

public class ExecutionReportRow {

    private Integer executionId;
    private Integer testCaseId;
    private Integer testCaseExternalId;
    private String testCaseExternalIdFull;
    private String testCaseName;
    private String testSuite;
    private Integer testCaseVersion;
    private Integer testCaseVersionId;

    private Integer testPlanId;
    private Integer buildId;
    private String buildName;

    private String status;
    private Integer testerId;
    private String executionTimestamp;
    private String executionType;
    private String notes;

    private String platform;
    private String testCaseStatus;
    private String testCaseSummary;
    
    private String executedBy;

    public Integer getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Integer executionId) {
        this.executionId = executionId;
    }

    public Integer getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Integer testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Integer getTestCaseExternalId() {
        return testCaseExternalId;
    }

    public void setTestCaseExternalId(Integer testCaseExternalId) {
        this.testCaseExternalId = testCaseExternalId;
    }

    public String getTestCaseExternalIdFull() {
        return testCaseExternalIdFull;
    }

    public void setTestCaseExternalIdFull(String testCaseExternalIdFull) {
        this.testCaseExternalIdFull = testCaseExternalIdFull;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public String getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    public Integer getTestCaseVersion() {
        return testCaseVersion;
    }

    public void setTestCaseVersion(Integer testCaseVersion) {
        this.testCaseVersion = testCaseVersion;
    }

    public Integer getTestCaseVersionId() {
        return testCaseVersionId;
    }

    public void setTestCaseVersionId(Integer testCaseVersionId) {
        this.testCaseVersionId = testCaseVersionId;
    }

    public Integer getTestPlanId() {
        return testPlanId;
    }

    public void setTestPlanId(Integer testPlanId) {
        this.testPlanId = testPlanId;
    }

    public Integer getBuildId() {
        return buildId;
    }

    public void setBuildId(Integer buildId) {
        this.buildId = buildId;
    }

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTesterId() {
        return testerId;
    }

    public void setTesterId(Integer testerId) {
        this.testerId = testerId;
    }

    public String getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(String executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTestCaseStatus() {
        return testCaseStatus;
    }

    public void setTestCaseStatus(String testCaseStatus) {
        this.testCaseStatus = testCaseStatus;
    }

    public String getTestCaseSummary() {
        return testCaseSummary;
    }

    public void setTestCaseSummary(String testCaseSummary) {
        this.testCaseSummary = testCaseSummary;
    }
    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }
    
}