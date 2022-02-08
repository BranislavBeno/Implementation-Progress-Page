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
        String uri = accessData.epicsUrl().replace("{groupId}", accessData.groupId());

        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(Epic[].class)
                .block();
    }

    public Issue[] fetchIssues(int epicId) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(accessData.issuesUrl())
                        .build(accessData.projectId(), epicId))
                .retrieve()
                .bodyToMono(Issue[].class)
                .block();
    }
}
