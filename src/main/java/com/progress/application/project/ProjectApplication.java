package com.progress.application.project;

import com.progress.application.project.service.HtmlRenderingService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@SpringBootApplication
public class ProjectApplication {

    @Autowired
    private final HtmlRenderingService htmlRenderingService;
    @Autowired
    private final ResourceLoader resourceLoader;

    public ProjectApplication(HtmlRenderingService htmlRenderingService, ResourceLoader resourceLoader) {
        this.htmlRenderingService = Objects.requireNonNull(htmlRenderingService);
        this.resourceLoader = Objects.requireNonNull(resourceLoader);

        Path htmlFile = prepareDestination(Paths.get("public/index.html"));
        copyFromResources();
        writeHtml(htmlFile);
    }

    private void copyFromResources() {
        var resource = resourceLoader.getResource("classpath:static");
        if (resource.exists()) {
            try {
                FileUtils.copyDirectory(resource.getFile(), new File("public"));
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
        }
        return file;
    }

    private void writeHtml(Path file) {
        try (var writer = Files.newBufferedWriter(file)) {
            writer.append(htmlRenderingService.provideHtml());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext;
        applicationContext = new SpringApplicationBuilder(ProjectApplication.class).run();
        applicationContext.close();
    }
}
