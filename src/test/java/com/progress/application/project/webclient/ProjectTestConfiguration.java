package com.progress.application.project.webclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ProjectTestConfiguration {

    @Bean
    public EpicData epicData(@Value("${issue.tracker.epics.url}") String url,
                             @Value("${issue.tracker.epics.labels}") String labels,
                             @Value("${issue.tracker.epics.per-page-limit}") String perPageLimit,
                             @Value("${issue.tracker.epics.scope}") String scope,
                             @Value("${issue.tracker.epics.state}") String state) {
        return new EpicData(url, labels, perPageLimit, scope, state);
    }

    @Bean
    public IssueData issueData(@Value("${issue.tracker.issues.url}") String url,
                               @Value("${issue.tracker.issues.per-page-limit}") String perPageLimit,
                               @Value("${issue.tracker.issues.scope}") String scope,
                               @Value("${issue.tracker.issues.state}") String state) {
        return new IssueData(url, perPageLimit, scope, state);
    }

    @Bean
    public AccessData accessRecord(@Value("${issue.tracker.group-id}") String groupId,
                                   @Value("${issue.tracker.project-id}") String projectId,
                                   @Value("${issue.tracker.access-token}") String accessToken,
                                   @Value("${issue.tracker.uri}") String baseUrl,
                                   @Autowired EpicData epicData,
                                   @Autowired IssueData issueData) {
        return new AccessData(groupId, projectId, accessToken, baseUrl, epicData, issueData);
    }
}
