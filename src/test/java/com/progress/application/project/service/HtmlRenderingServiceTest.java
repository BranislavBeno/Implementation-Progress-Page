package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HtmlRenderingServiceTest {

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
    when(issueService.getEpics()).thenReturn(List.of(epic));
    when(epic.getIssues()).thenReturn(List.of(issue1, issue2, issue3));
    when(issue1.printLabels()).thenReturn("");
    when(issue1.getWorkFlow()).thenReturn("workflow::blocked");
    when(issue1.getState()).thenReturn("closed");
    when(issue1.getMilestone()).thenReturn(milestone);
    when(milestone.getTitle()).thenReturn("");
    when(issue2.getWorkFlow()).thenReturn("");
    when(issue2.getState()).thenReturn("opened");
    when(issue3.getWorkFlow()).thenReturn("workflow::development");
    when(issue3.getState()).thenReturn("");
    //when
    String content = htmlService.provideHtml();
    // then
    verify(issueService).getEpics();
    verify(epic, times(3)).getIssues();
    verify(issue1).printLabels();
    verify(issue1).getWorkFlow();
    verify(issue1).getState();
    verify(issue1, times(4)).getMilestone();
    verify(milestone, times(3)).getTitle();
    verify(issue2).getWorkFlow();
    verify(issue2).getState();
    verify(issue3).getWorkFlow();
    verify(issue3).getState();
    assertThat(content).isNotBlank();
  }

  @Test
  void testHtmlRenderingWithNoIssues() {
    // given
    when(issueService.getEpics()).thenReturn(List.of(epic));
    when(epic.getIssues()).thenReturn(List.of());
    // when
    String content = htmlService.provideHtml();
    // then
    verify(issueService).getEpics();
    verify(epic, times(3)).getIssues();
    assertThat(content).isNotBlank();
  }
}