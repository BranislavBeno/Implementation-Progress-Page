package com.progress.application.project.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Epic {

    private int iid;
    @JsonProperty(value = "project_id")
    private int projectId;
    private String title;
    private String state;
    private List<Issue> issues;
    @JsonProperty(value = "web_url")
    private String webUrl;

    public int getIid() {
        return iid;
    }

    public void setIid(int iid) {
        this.iid = iid;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }
}