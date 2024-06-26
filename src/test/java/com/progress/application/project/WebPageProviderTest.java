package com.progress.application.project;

import com.progress.application.project.service.HtmlRenderingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
class WebPageProviderTest {

  @TempDir
  private Path testDestination;

  @Mock
  private HtmlRenderingService htmlRenderingService;
  @Mock
  private ResourceLoader resourceLoader;
  @Mock
  private Resource resource;
  @InjectMocks
  private WebPageProvider webPageProvider;

  @Test
  void testWebPageProviding() {
    // given
    Mockito.when(htmlRenderingService.provideHtml()).thenReturn("");
    Mockito.when(resourceLoader.getResource("classpath:static")).thenReturn(resource);
    // when
    webPageProvider.provideWebPage(testDestination.resolve("test"));
    // then
    Mockito.verify(htmlRenderingService).provideHtml();
    Mockito.verify(resourceLoader).getResource(Mockito.anyString());
  }
}