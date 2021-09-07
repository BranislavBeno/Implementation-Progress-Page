package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class IssueTrackingService {

    private final String accessToken;
    private final String epicsUrl;
    private final String issuesUrl;
    private final RestTemplate restTemplate;
    private final List<Epic> epics;
    private final List<String> releases;
    private final int releaseCount;

    public IssueTrackingService(@Value("${issue.tracker.uri}") String uri,
                                @Value("${issue.tracker.access-token}") String accessToken,
                                @Value("${issue.tracker.epics-url}") String epicsUrl,
                                @Value("${issue.tracker.issues-url}") String issuesUrl,
                                @Autowired RestTemplateBuilder restTemplateBuilder) {
        this.accessToken = accessToken;
        this.epicsUrl = epicsUrl;
        this.issuesUrl = issuesUrl;

        this.restTemplate = restTemplateBuilder
                .rootUri(uri)
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();

        this.epics = provideEpics();
        this.releases = provideReleases();
        this.releaseCount = releases.size();
    }

    private HttpEntity<Void> provideHttpEntity() {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("PRIVATE-TOKEN", accessToken);

        return new HttpEntity<>(headers);
    }

    private Epic[] fetchEpics(HttpEntity<Void> entity) {
        return restTemplate.exchange(epicsUrl, HttpMethod.GET, entity, Epic[].class).getBody();
    }

    private Issue[] fetchIssues(HttpEntity<Void> entity, int epicId) {
        return restTemplate.exchange(issuesUrl, HttpMethod.GET, entity, Issue[].class, epicId).getBody();
    }

    private List<Epic> provideEpics() {
        HttpEntity<Void> entity = provideHttpEntity();
        Epic[] fetchedEpics = fetchEpics(entity);

        if (fetchedEpics != null) {
            Set<String> types = Set.of("Hot-Fix", "CR", "Defect", "Feature");
            for (Epic epic : fetchedEpics) {
                Issue[] issues = fetchIssues(entity, epic.getIid());
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
