package com.progress.application.project;

import com.progress.application.project.service.IssueFetchingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ProjectApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectApplication.class);

    ProjectApplication(@Autowired WebPageProvider pageProvider) {
        try {
            Path destination = Paths.get("public/index.html");
            pageProvider.provideWebPage(destination);
        } catch (IssueFetchingException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext;
        applicationContext = new SpringApplicationBuilder(ProjectApplication.class).run();
        applicationContext.close();
    }
}
