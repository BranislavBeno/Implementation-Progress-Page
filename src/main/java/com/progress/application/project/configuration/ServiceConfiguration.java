package com.progress.application.project.configuration;

import com.progress.application.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfiguration {

    @Bean
    public AccessData accessRecord(@Value("${issue.tracker.uri}") String baseUrl,
                                   @Value("${issue.tracker.access-token}") String accessToken,
                                   @Value("${issue.tracker.epics-url}") String epicsUrl,
                                   @Value("${issue.tracker.issues-url}") String issuesUrl) {
        return new AccessData(baseUrl, accessToken, epicsUrl, issuesUrl);
    }

    @Bean
    public IssueWebClient issueWebClient(@Autowired AccessData accessData) {
        return new IssueWebClient(accessData);
    }

    @Bean
    public IssueTrackingService issueTrackingService(@Autowired IssueWebClient issueWebClient,
                                                     @Autowired WorkflowData workflowData) {
        return new IssueTrackingService(issueWebClient, workflowData);
    }

    @Bean
    public HtmlRenderingService htmlRenderingService(@Value(value = "${spring.application.ui.title:Progress}") String title,
                                                     @Autowired IssueTrackingService issueTrackingService) {
        return new HtmlRenderingService(title, issueTrackingService);
    }
}
