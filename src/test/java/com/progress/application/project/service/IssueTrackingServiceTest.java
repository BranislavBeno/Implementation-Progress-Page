package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.webclient.IssueWebClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueTrackingServiceTest {

    @Mock
    private WorkflowData data;
    @Mock
    private IssueWebClient webClient;
    @Mock
    private Issue issue;
    @InjectMocks
    private IssueTrackingService service;

    @Test
    void testFetchingEpics() {
        // given
        when(webClient.fetchEpics()).thenReturn(new Epic[]{new Epic()});
        when(webClient.fetchIssues(anyInt())).thenReturn(new Issue[]{issue, issue});
        when(data.getTypes()).thenReturn(List.of("CR"));
        when(issue.getState()).thenReturn("Open");
        // when
        List<Epic> epics = service.getEpics();
        // then
        verify(webClient).fetchEpics();
        verify(webClient).fetchIssues(anyInt());
        verify(data).getTypes();
        verify(issue, times(2)).getState();
        assertThat(epics).hasSize(1);
    }

    @Test
    void testFetchingEmptyEpics() {
        // given
        when(webClient.fetchEpics()).thenReturn(new Epic[]{});
        when(data.getTypes()).thenReturn(List.of("CR"));
        // when
        List<Epic> epics = service.getEpics();
        // then
        verify(webClient).fetchEpics();
        verify(data).getTypes();
        assertThat(epics).isEmpty();
    }

    @Test
    void testFetchingNullEpics() {
        // given
        when(webClient.fetchEpics()).thenReturn(null);
        // when
        List<Epic> epics = service.getEpics();
        // then
        verify(webClient).fetchEpics();
        assertThat(epics).isEmpty();
    }
}