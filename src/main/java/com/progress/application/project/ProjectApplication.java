package com.progress.application.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ProjectApplication {

    public ProjectApplication(@Autowired WebPageProvider pageProvider) {
        Path destination = Paths.get("public/index.html");
        pageProvider.provideWebPage(destination);
    }

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext;
        applicationContext = new SpringApplicationBuilder(ProjectApplication.class).run();
        applicationContext.close();
    }
}
