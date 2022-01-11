package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.webclient.IssueWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public record IssueTrackingService(IssueWebClient issueWebClient, WorkflowData workflowData) {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueTrackingService.class);

    public List<Epic> getEpics() {
        try {
            Epic[] fetchedEpics = issueWebClient.fetchEpics();

            if (fetchedEpics != null) {
                Set<String> types = Set.copyOf(workflowData.getTypes());
                for (Epic epic : fetchedEpics) {
                    Issue[] issues = issueWebClient.fetchIssues(epic.getIid());
                    for (Issue issue : issues) {
                        String workflow = issue.getLabels().stream().filter(l -> l.startsWith(workflowData.getPrefix())).collect(Collectors.joining(","));
                        issue.setWorkFlow(workflow);
                        issue.getLabels().retainAll(types);
                    }
                    List<Issue> sortedIssues = Arrays.stream(issues).sorted(Comparator.comparing(Issue::getState).reversed()).toList();
                    epic.setIssues(sortedIssues);
                }
                return Arrays.stream(fetchedEpics).sorted(Comparator.comparing(Epic::getIid).reversed()).toList();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new IssueFetchingException("No issues have been fetched.");
        }
        return Collections.emptyList();
    }
}
