package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IssueTrackingService {

    private final String epicsUrl;
    private final String issuesUrl;
    private final WebClient webClient;
    private final List<Epic> epics;
    private final List<String> releases;
    private final int releaseCount;

    public IssueTrackingService(@Value("${issue.tracker.uri}") String uri,
                                @Value("${issue.tracker.access-token}") String accessToken,
                                @Value("${issue.tracker.epics-url}") String epicsUrl,
                                @Value("${issue.tracker.issues-url}") String issuesUrl) {
        this.epicsUrl = epicsUrl;
        this.issuesUrl = issuesUrl;

        this.webClient = WebClient.builder()
                .baseUrl(uri)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.add("PRIVATE-TOKEN", accessToken);
                })
                .build();

        this.epics = provideEpics();
        this.releases = provideReleases();
        this.releaseCount = releases.size();
    }

    private Epic[] fetchEpics() {
        return webClient
                .get()
                .uri(epicsUrl)
                .retrieve()
                .bodyToMono(Epic[].class)
                .block();
    }

    private Issue[] fetchIssues(int epicId) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(issuesUrl)
                        .build(epicId))
                .retrieve()
                .bodyToMono(Issue[].class)
                .block();
    }

    private List<Epic> provideEpics() {
        Epic[] fetchedEpics = fetchEpics();

        if (fetchedEpics != null) {
            Set<String> types = Set.of("Hot-Fix", "CR", "Defect", "Feature");
            for (Epic epic : fetchedEpics) {
                Issue[] issues = fetchIssues(epic.getIid());
                for (Issue issue : issues) {
                    String workflow = issue.getLabels().stream()
                            .filter(l -> l.startsWith("workflow::"))
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
