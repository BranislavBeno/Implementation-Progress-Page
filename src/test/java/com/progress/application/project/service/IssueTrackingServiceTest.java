package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.webclient.IssueWebClient;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class IssueTrackingServiceTest implements WithAssertions {

    private IssueTrackingService service;
    @Mock
    private WorkflowData data;
    @Mock
    private IssueWebClient webClient;
    @Mock
    private Issue issue;
    @Mock
    private Epic epic;

    @BeforeEach
    void setUp() {
        service = new IssueTrackingService(webClient, "1", data);
    }

    @Test
    void testFetchingMatchingEpics() {
        // given
        Mockito.when(webClient.fetchEpics()).thenReturn(new Epic[]{epic});
        Mockito.when(epic.getProjectId()).thenReturn(1);
        Mockito.when(webClient.fetchIssues(Mockito.anyInt())).thenReturn(new Issue[]{issue, issue});
        Mockito.when(data.getTypes()).thenReturn(List.of("CR"));
        Mockito.when(issue.getState()).thenReturn("Open");
        // when
        List<Epic> epics = service.getEpics();
        // then
        Mockito.verify(webClient).fetchEpics();
        Mockito.verify(epic).getProjectId();
        Mockito.verify(webClient).fetchIssues(Mockito.anyInt());
        Mockito.verify(data).getTypes();
        Mockito.verify(issue, Mockito.times(2)).getState();
        assertThat(epics).hasSize(1);
    }

    @Test
    void testFetchingNotMatchingEpics() {
        // given
        Mockito.when(webClient.fetchEpics()).thenReturn(new Epic[]{epic});
        Mockito.when(epic.getProjectId()).thenReturn(2);
        // when
        List<Epic> epics = service.getEpics();
        // then
        Mockito.verify(webClient).fetchEpics();
        Mockito.verify(epic).getProjectId();
        assertThat(epics).isEmpty();
    }

    @Test
    void testFetchingEmptyEpics() {
        // given
        Mockito.when(webClient.fetchEpics()).thenReturn(new Epic[]{});
        // when
        List<Epic> epics = service.getEpics();
        // then
        Mockito.verify(webClient).fetchEpics();
        assertThat(epics).isEmpty();
    }

    @Test
    void testFetchingNullEpics() {
        // given
        Mockito.when(webClient.fetchEpics()).thenReturn(null);
        // when, then
        assertFailingFetching();
    }

    @Test
    void testFetchingFailingEpics() {
        // given
        Mockito.when(webClient.fetchEpics()).thenThrow(IssueFetchingException.class);
        // when, then
        assertFailingFetching();
    }

    private void assertFailingFetching() {
        assertThatThrownBy(() -> service.getEpics()).hasMessage("No issues have been fetched.");
        Mockito.verify(webClient).fetchEpics();
    }
}