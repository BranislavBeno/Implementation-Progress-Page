package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.webclient.IssueWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record IssueTrackingService(IssueWebClient issueWebClient, String projectId, WorkflowData workflowData) {

    private static final Logger LOGGER = LoggerFactory.getLogger(IssueTrackingService.class);

    public List<Epic> getEpics() {
        try {
            int projId = Integer.parseInt(projectId);
            // fetch epics
            List<Epic> fetchedEpics = Arrays.stream(issueWebClient.fetchEpics())
                    .filter(e -> e.getProjectId() == projId)
                    .sorted(Comparator.comparing(Epic::getIid).reversed())
                    .toList();
            // fetch and import issues
            fetchedEpics.forEach(this::importIssues);

            return fetchedEpics;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new IssueFetchingException("No issues have been fetched.");
        }
    }

    private void importIssues(Epic epic) {
        Set<String> types = Set.copyOf(workflowData.getTypes());
        Issue[] issues = issueWebClient.fetchIssues(epic.getIid());
        for (Issue issue : issues) {
            String workflow = issue.getLabels().stream()
                    .filter(l -> l.startsWith(workflowData.getPrefix()))
                    .collect(Collectors.joining(","));
            issue.setWorkFlow(workflow);
            issue.getLabels().retainAll(types);
        }
        List<Issue> sortedIssues = Arrays.stream(issues).sorted(Comparator.comparing(Issue::getState).reversed()).toList();
        epic.setIssues(sortedIssues);
    }
}
