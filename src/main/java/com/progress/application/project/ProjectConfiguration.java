package com.progress.application.project;

import com.progress.application.project.service.HtmlRenderingService;
import com.progress.application.project.service.IssueTrackingService;
import com.progress.application.project.service.WorkflowData;
import com.progress.application.project.webclient.AccessData;
import com.progress.application.project.webclient.EpicData;
import com.progress.application.project.webclient.IssueData;
import com.progress.application.project.webclient.IssueWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
class ProjectConfiguration {

    @Bean
    EpicData epicData(@Value("${issue.tracker.epics.url}") String url,
            @Value("${issue.tracker.epics.labels}") String labels,
            @Value("${issue.tracker.epics.per-page-limit}") String perPageLimit,
            @Value("${issue.tracker.epics.scope}") String scope,
            @Value("${issue.tracker.epics.state}") String state) {
        return new EpicData(url, labels, perPageLimit, scope, state);
    }

    @Bean
    IssueData issueData(@Value("${issue.tracker.issues.url}") String url,
            @Value("${issue.tracker.issues.per-page-limit}") String perPageLimit,
            @Value("${issue.tracker.issues.scope}") String scope,
            @Value("${issue.tracker.issues.state}") String state) {
        return new IssueData(url, perPageLimit, scope, state);
    }

    @Bean
    AccessData accessRecord(@Value("${issue.tracker.group-id}") String groupId,
            @Value("${issue.tracker.project-id}") String projectId,
            @Value("${issue.tracker.access-token}") String accessToken,
            @Value("${issue.tracker.uri}") String baseUrl,
            @Autowired EpicData epicData,
            @Autowired IssueData issueData) {
        return new AccessData(groupId, projectId, accessToken, baseUrl, epicData, issueData);
    }

    @Bean
    IssueWebClient issueWebClient(@Autowired AccessData accessData) {
        return new IssueWebClient(accessData);
    }

    @Bean
    IssueTrackingService issueTrackingService(@Autowired IssueWebClient issueWebClient,
            @Value("${issue.tracker.project-id}") String projectId,
            @Autowired WorkflowData workflowData) {
        return new IssueTrackingService(issueWebClient, projectId, workflowData);
    }

    @Bean
    HtmlRenderingService htmlRenderingService(@Value(value = "${spring.application.ui.title:Progress}") String title,
            @Autowired IssueTrackingService issueTrackingService) {
        return new HtmlRenderingService(title, issueTrackingService);
    }

    @Bean
    WebPageProvider webPageProvider(@Autowired HtmlRenderingService renderingService,
            @Autowired ResourceLoader resourceLoader) {
        return new WebPageProvider(renderingService, resourceLoader);
    }
}
