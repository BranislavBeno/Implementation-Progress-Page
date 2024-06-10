package com.progress.application.project.webclient;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import org.assertj.core.api.WithAssertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(classes = IssueWebClient.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IssueWebClientTest.Initializer.class)
@Import(ProjectTestConfiguration.class)
class IssueWebClientTest implements WithAssertions {

    private static final MockServerContainer MOCK_SERVER = new MockServerContainer(
            DockerImageName.parse("mockserver/mockserver:mockserver-5.15.0"));
    private static final String BASE_URL;

    static {
        MOCK_SERVER.withReuse(true).start();
        BASE_URL = "http://%s:%s".formatted(MOCK_SERVER.getHost(), MOCK_SERVER.getServerPort());
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("issue.tracker.uri", () -> BASE_URL);
        registry.add("issue.tracker.epics.url", () -> "/epics");
        registry.add("issue.tracker.epics.labels", () -> "Epic");
        registry.add("issue.tracker.epics.per-page-limit", () -> "100");
        registry.add("issue.tracker.epics.scope", () -> "all");
        registry.add("issue.tracker.epics.state", () -> "all");
        registry.add("issue.tracker.issues.url", () -> "issues/2");
        registry.add("issue.tracker.issues.per-page-limit", () -> "100");
        registry.add("issue.tracker.issues.scope", () -> "all");
        registry.add("issue.tracker.issues.state", () -> "all");
    }

    @Autowired
    private IssueWebClient webClient;

    @Test
    void testEpicsFetching() throws IOException {
        String json = readResourceFile("/epics.json");
        MockServerClient mockServerClient = provideMockServer();
        mockResponse("/epics", json, mockServerClient);
        Epic[] epics = webClient.fetchEpics();

        assertThat(epics).hasSize(7);

        Epic epic = Arrays.stream(epics).min(Comparator.comparingInt(Epic::getIid)).orElse(null);
        assertThat(epic).isNotNull().satisfies(e -> {
            assertThat(e.getIid()).isEqualTo(1);
            assertThat(e.getIssues()).isNull();
            assertThat(e.getState()).isEqualTo("opened");
            assertThat(e.getTitle()).isEqualTo("Support and maintenance");
            assertThat(e.getWebUrl()).isEqualTo("https://gitlab.com/dashboard-tools/Implementation-Progress-Page/-/issues/1");
        });
    }

    @Test
    void testIssuesFetching() throws IOException {
        String json = readResourceFile("/issues.json");
        MockServerClient mockServerClient = provideMockServer();
        mockResponse("/issues/2", json, mockServerClient);
        Issue[] issues = webClient.fetchIssues(2);

        assertThat(issues).hasSize(3);

        Issue issue = Arrays.stream(issues).min(Comparator.comparing(Issue::getIid)).orElse(null);
        assertThat(issue).isNotNull().satisfies(i -> {
            assertThat(i.getIid()).isEqualTo("10");
            assertThat(i.getMilestone().getTitle()).isEqualTo("v0.4.0");
            assertThat(i.getState()).isEqualTo("closed");
            assertThat(i.getTitle()).isEqualTo("Rework DTO projection on Ronja server");
            assertThat(i.getWebUrl()).isEqualTo("https://gitlab.com/dashboard-tools/Implementation-Progress-Page/-/issues/10");
            assertThat(i.getWorkFlow()).isNull();
            assertThat(i.getLabels()).hasSize(2);
            assertThat(i.printLabels()).hasSize(20);
        });
    }

    private void mockResponse(String url, String json, MockServerClient mockServerClient) {
        mockServerClient
                .when(HttpRequest.request()
                        .withMethod("GET")
                        .withPath(url))
                .respond(new HttpResponse()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(json)
                );
    }

    private MockServerClient provideMockServer() {
        return new MockServerClient(MOCK_SERVER.getHost(), MOCK_SERVER.getServerPort());
    }

    private String readResourceFile(String filePath) throws IOException {
        File epicsFile = new ClassPathResource(filePath).getFile();
        return FileUtils.readFileToString(epicsFile, StandardCharsets.UTF_8);
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        }
    }
}