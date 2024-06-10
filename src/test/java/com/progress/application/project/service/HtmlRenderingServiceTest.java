package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class HtmlRenderingServiceTest implements WithAssertions {

  @Mock
  private IssueTrackingService issueService;
  @Mock
  private Epic epic;
  @Mock
  private Issue issue1;
  @Mock
  private Issue issue2;
  @Mock
  private Issue issue3;
  @Mock
  private Milestone milestone;
  @InjectMocks
  private HtmlRenderingService htmlService;

  @Test
  void testFullHtmlRendering() {
    // given
    Mockito.when(issueService.getEpics()).thenReturn(List.of(epic));
    Mockito.when(epic.getIssues()).thenReturn(List.of(issue1, issue2, issue3));
    Mockito.when(issue1.printLabels()).thenReturn("");
    Mockito.when(issue1.getWorkFlow()).thenReturn("workflow::blocked");
    Mockito.when(issue1.getState()).thenReturn("closed");
    Mockito.when(issue1.getMilestone()).thenReturn(milestone);
    Mockito.when(milestone.getTitle()).thenReturn("");
    Mockito.when(issue2.getWorkFlow()).thenReturn("");
    Mockito.when(issue2.getState()).thenReturn("opened");
    Mockito.when(issue3.getWorkFlow()).thenReturn("workflow::development");
    Mockito.when(issue3.getState()).thenReturn("");
    //when
    String content = htmlService.provideHtml();
    // then
    Mockito.verify(issueService).getEpics();
    Mockito.verify(epic, Mockito.times(3)).getIssues();
    Mockito.verify(issue1).printLabels();
    Mockito.verify(issue1).getWorkFlow();
    Mockito.verify(issue1).getState();
    Mockito.verify(issue1, Mockito.times(4)).getMilestone();
    Mockito.verify(milestone, Mockito.times(3)).getTitle();
    Mockito.verify(issue2).getWorkFlow();
    Mockito.verify(issue2).getState();
    Mockito.verify(issue3).getWorkFlow();
    Mockito.verify(issue3).getState();
    assertThat(content).isNotBlank();
  }

  @Test
  void testHtmlRenderingWithNoIssues() {
    // given
    Mockito.when(issueService.getEpics()).thenReturn(List.of(epic));
    Mockito.when(epic.getIssues()).thenReturn(List.of());
    // when
    String content = htmlService.provideHtml();
    // then
    Mockito.verify(issueService).getEpics();
    Mockito.verify(epic, Mockito.times(3)).getIssues();
    assertThat(content).isNotBlank();
  }
}