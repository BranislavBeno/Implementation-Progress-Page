package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.webclient.IssueWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueTrackingServiceTest {

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
        when(webClient.fetchEpics()).thenReturn(new Epic[]{epic});
        when(epic.getProjectId()).thenReturn(1);
        when(webClient.fetchIssues(anyInt())).thenReturn(new Issue[]{issue, issue});
        when(data.getTypes()).thenReturn(List.of("CR"));
        when(issue.getState()).thenReturn("Open");
        // when
        List<Epic> epics = service.getEpics();
        // then
        verify(webClient).fetchEpics();
        verify(epic).getProjectId();
        verify(webClient).fetchIssues(anyInt());
        verify(data).getTypes();
        verify(issue, times(2)).getState();
        assertThat(epics).hasSize(1);
    }

    @Test
    void testFetchingNotMatchingEpics() {
        // given
        when(webClient.fetchEpics()).thenReturn(new Epic[]{epic});
        when(epic.getProjectId()).thenReturn(2);
        // when
        List<Epic> epics = service.getEpics();
        // then
        verify(webClient).fetchEpics();
        verify(epic).getProjectId();
        assertThat(epics).isEmpty();
    }

    @Test
    void testFetchingEmptyEpics() {
        // given
        when(webClient.fetchEpics()).thenReturn(new Epic[]{});
        // when
        List<Epic> epics = service.getEpics();
        // then
        verify(webClient).fetchEpics();
        assertThat(epics).isEmpty();
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(classes = IssueFetchingException.class)
    void testFetchingNullAndFailingEpics(Class<? extends Throwable> clazz) {
        // given
        if (clazz != null) {
            when(webClient.fetchEpics()).thenThrow(clazz);
        } else {
            when(webClient.fetchEpics()).thenReturn(null);
        }
        // when
        assertThatThrownBy(() -> service.getEpics()).hasMessage("No issues have been fetched.");
        // then
        verify(webClient).fetchEpics();
    }
}