package com.progress.application.project.webclient;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

public class IssueWebClient {

    private final AccessData accessData;
    private final WebClient webClient;

    public IssueWebClient(AccessData accessData) {
        this.accessData = Objects.requireNonNull(accessData);

        this.webClient = WebClient.builder()
                .baseUrl(accessData.baseUrl())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                    httpHeaders.add("PRIVATE-TOKEN", accessData.accessToken());
                })
                .build();
    }

    public Epic[] fetchEpics() {
        EpicData epicData = accessData.epicData();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(epicData.url())
                        .queryParam("labels", epicData.labels())
                        .queryParam("per_page", epicData.perPageLimit())
                        .queryParam("scope", epicData.scope())
                        .queryParam("state", epicData.state())
                        .build(accessData.groupId()))
                .retrieve()
                .bodyToMono(Epic[].class)
                .block();
    }

    public Issue[] fetchIssues(int epicId) {
        IssueData issueData = accessData.issueData();

        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(issueData.url())
                        .queryParam("per_page", issueData.perPageLimit())
                        .queryParam("scope", issueData.scope())
                        .queryParam("state", issueData.state())
                        .build(accessData.projectId(), epicId))
                .retrieve()
                .bodyToMono(Issue[].class)
                .block();
    }
}
