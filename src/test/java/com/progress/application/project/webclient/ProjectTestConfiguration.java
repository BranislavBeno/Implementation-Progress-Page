package com.progress.application.project.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ProjectTestConfiguration {

  @Bean
  public AccessData accessRecord(@Value("${issue.tracker.uri}") String baseUrl,
                                 @Value("${issue.tracker.access-token}") String accessToken,
                                 @Value("${issue.tracker.epics-url}") String epicsUrl,
                                 @Value("${issue.tracker.issues-url}") String issuesUrl) {
    return new AccessData(baseUrl, accessToken, epicsUrl, issuesUrl);
  }
}
