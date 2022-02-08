package com.progress.application.project.webclient;

public record AccessData(String groupId, String projectId, String accessToken, String baseUrl, String epicsUrl,
                         String issuesUrl) {
}
