package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;

import java.util.*;
import java.util.stream.Collectors;

public class IssueTrackingService {

    private final IssueWebClient issueWebClient;
    private final WorkflowData workflowData;
    private final List<Epic> epics;
    private final List<String> releases;
    private final int releaseCount;

    public IssueTrackingService(IssueWebClient issueWebClient, WorkflowData workflowData) {
        this.issueWebClient = Objects.requireNonNull(issueWebClient);
        this.workflowData = Objects.requireNonNull(workflowData);

        this.epics = provideEpics();
        this.releases = provideReleases();
        this.releaseCount = releases.size();
    }

    public List<Epic> provideEpics() {
        Epic[] fetchedEpics = issueWebClient.fetchEpics();

        if (fetchedEpics != null) {
            Set<String> types = Set.copyOf(workflowData.getTypes());
            for (Epic epic : fetchedEpics) {
                Issue[] issues = issueWebClient.fetchIssues(epic.getIid());
                for (Issue issue : issues) {
                    String workflow = issue.getLabels().stream()
                            .filter(l -> l.startsWith(workflowData.getPrefix()))
                            .collect(Collectors.joining(","));
                    issue.setWorkFlow(workflow);
                    issue.getLabels().retainAll(types);
                }
                List<Issue> sortedIssues = Arrays.stream(issues)
                        .sorted(Comparator.comparing(Issue::getState).reversed())
                        .toList();
                epic.setIssues(sortedIssues);
            }
            return Arrays.stream(fetchedEpics).sorted(Comparator.comparing(Epic::getIid).reversed()).toList();
        }
        return Collections.emptyList();
    }

    private List<String> provideReleases() {
        return epics.stream().flatMap(e ->
                        e.getIssues()
                                .stream())
                .map(Issue::getMilestone)
                .filter(Objects::nonNull)
                .map(Milestone::getTitle)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    public List<Epic> getEpics() {
        return epics;
    }

    public List<String> getReleases() {
        return releases;
    }

    public int getReleaseCount() {
        return releaseCount;
    }
}
