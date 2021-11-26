package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

public class IssueTrackingService {

    private final AccessData accessData;
    private final WorkflowData workflowData;
    private final WebClient webClient;
    private final List<Epic> epics;
    private final List<String> releases;
    private final int releaseCount;

    public IssueTrackingService(AccessData accessData, WorkflowData workflowData) {
        this.accessData = Objects.requireNonNull(accessData);
        this.workflowData = Objects.requireNonNull(workflowData);

        this.webClient = WebClient.builder()
                .baseUrl(accessData.baseUrl())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.add("PRIVATE-TOKEN", accessData.accessToken());
                })
                .build();

        this.epics = provideEpics();
        this.releases = provideReleases();
        this.releaseCount = releases.size();
    }

    private Epic[] fetchEpics() {
        return webClient
                .get()
                .uri(accessData.epicsUrl())
                .retrieve()
                .bodyToMono(Epic[].class)
                .block();
    }

    private Issue[] fetchIssues(int epicId) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(accessData.issuesUrl())
                        .build(epicId))
                .retrieve()
                .bodyToMono(Issue[].class)
                .block();
    }

    private List<Epic> provideEpics() {
        Epic[] fetchedEpics = fetchEpics();

        if (fetchedEpics != null) {
            Set<String> types = Set.copyOf(workflowData.getTypes());
            for (Epic epic : fetchedEpics) {
                Issue[] issues = fetchIssues(epic.getIid());
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
