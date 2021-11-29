package com.progress.application.project;

import com.progress.application.project.service.HtmlRenderingService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

class WebPageProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebPageProvider.class);

  private final HtmlRenderingService htmlRenderingService;
  private final ResourceLoader resourceLoader;

  public WebPageProvider(HtmlRenderingService htmlRenderingService, ResourceLoader resourceLoader) {
    this.htmlRenderingService = Objects.requireNonNull(htmlRenderingService);
    this.resourceLoader = Objects.requireNonNull(resourceLoader);
  }

  public void provideWebPage(Path destination) {
    Path htmlFile = prepareDestination(destination);
    copyFromResources();
    writeHtml(htmlFile);
  }

  private void copyFromResources() {
    var resource = resourceLoader.getResource("classpath:static");
    if (resource.exists()) {
      try {
        FileUtils.copyDirectory(resource.getFile(), new File("public"));
      } catch (IOException e) {
        LOGGER.info("""
            Resources copying failed:
            %s""".formatted(e.getCause().getMessage()));
      }
    }
  }

  private Path prepareDestination(Path file) {
    Path destination = file.getParent();
    try {
      if (!Files.exists(destination)) {
        Files.createDirectory(destination);
      }
      if (!Files.exists(file)) {
        Files.createFile(file);
      }
    } catch (IOException e) {
      LOGGER.info("""
          Destination creation failed:
          %s""".formatted(e.getCause().getMessage()));
    }
    return file;
  }

  private void writeHtml(Path file) {
    try (var writer = Files.newBufferedWriter(file)) {
      writer.append(htmlRenderingService.provideHtml());
    } catch (IOException e) {
      LOGGER.info("""
          HTML content writing failed:
          %s""".formatted(e.getCause().getMessage()));
    }
  }
}
