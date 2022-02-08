package com.progress.application.project;

import com.progress.application.project.service.HtmlRenderingService;
import com.progress.application.project.service.IssueTrackingService;
import com.progress.application.project.service.WorkflowData;
import com.progress.application.project.webclient.AccessData;
import com.progress.application.project.webclient.IssueWebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@Configuration
class ProjectConfiguration {

    @Bean
    public AccessData accessRecord(@Value("${issue.tracker.group-id}") String groupId,
                                   @Value("${issue.tracker.project-id}") String projectId,
                                   @Value("${issue.tracker.access-token}") String accessToken,
                                   @Value("${issue.tracker.uri}") String baseUrl,
                                   @Value("${issue.tracker.epics-url}") String epicsUrl,
                                   @Value("${issue.tracker.issues-url}") String issuesUrl) {
        return new AccessData(groupId, projectId, accessToken, baseUrl, epicsUrl, issuesUrl);
    }

    @Bean
    public IssueWebClient issueWebClient(@Autowired AccessData accessData) {
        return new IssueWebClient(accessData);
    }

    @Bean
    public IssueTrackingService issueTrackingService(@Autowired IssueWebClient issueWebClient,
                                                     @Value("${issue.tracker.project-id}") String projectId,
                                                     @Autowired WorkflowData workflowData) {
        return new IssueTrackingService(issueWebClient, projectId, workflowData);
    }

    @Bean
    public HtmlRenderingService htmlRenderingService(@Value(value = "${spring.application.ui.title:Progress}") String title,
                                                     @Autowired IssueTrackingService issueTrackingService) {
        return new HtmlRenderingService(title, issueTrackingService);
    }

    @Bean
    public WebPageProvider webPageProvider(@Autowired HtmlRenderingService renderingService,
                                           @Autowired ResourceLoader resourceLoader) {
        return new WebPageProvider(renderingService, resourceLoader);
    }
}
