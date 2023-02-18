[![Application Tests](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/tests.yml/badge.svg)](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/tests.yml)
[![Docker Image Deploy](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/deploy.yml/badge.svg)](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/deploy.yml)  
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_ImplementationProgressPage&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_ImplementationProgressPage)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_ImplementationProgressPage&metric=coverage)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_ImplementationProgressPage)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_ImplementationProgressPage&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_ImplementationProgressPage)  
[![](https://img.shields.io/badge/Java-19-blue)](/build.gradle)
[![](https://img.shields.io/badge/Spring%20Boot-3.0.2-blue)](/build.gradle)
[![](https://img.shields.io/badge/Testcontainers-1.17.6-blue)](/build.gradle)
[![](https://img.shields.io/badge/Gradle-8.0.1-blue)](/gradle/wrapper/gradle-wrapper.properties)
[![](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)  

## Backend application for static web page creation
This simple backend application creates static web page with fetched status of particular project related GitLab issues.  
[Example results](https://dashboard-tools.gitlab.io/Implementation-Progress-Page) are available on GitLab pages.  

Application works not as a web server, but simply fetches necessary data, creates web page and afterwards ends.  
In case you need to recreate the web page, you must run the application again.

### Usage
This application relies on Gitlab issues and is intended as an extension for project workflow.  
> Application expects only running instance of Gitlab from which the issues will be imported. No other services (e.g. databases, message brokers,...) are required.

Result of application run can be directly published as a [Gitlab Pages](https://docs.gitlab.com/ee/user/project/pages/),  
hence the preferred way of usage is to define docker image `beo1975/implementation-progress:1.4.0` as a basic image for Gitlab-CI:
```yaml
image: beo1975/implementation-progress:1.4.0

pages:
  stage: deploy
  script:
    - cp -r /app/* ./
    - cp ci/application.yml BOOT-INF/classes/
    - java org.springframework.boot.loader.JarLauncher
  artifacts:
    paths:
      - public
```

### Configuration
Application will create correct output only with necessary configuration, which is done within `application.yml` file, e.g.:

```yaml
issue:
    tracker:
        uri: https://gitlab.com
        group-id: 13289074
        project-id: 31643739
        access-token: <Provide token as CI/CD related environment variable named 'ISSUE_TRACKER_ACCESS_TOKEN'>
        epics:
            url: /api/v4/groups/{groupId}/issues
            labels: Epic
            per-page-limit: 50000
            scope: all
            state: all
        issues:
            url: /api/v4/projects/{projectId}/issues/{epicId}/links
            per-page-limit: 50000
            scope: all
            state: all
    workflow:
        prefix: "workflow::"
        types:
            - Hot-Fix
            - CR
            - Defect
            - Feature
            - Know How
            - Documentation

spring:
    main:
        web-application-type: none
    application:
        ui:
            title: <Project name>
```

- Application expects Gitlab issues labeled as `Epic`. Those represent features in developed project. Tasks (again in form of Gitlab issues) related to particular feature are [linked](https://docs.gitlab.com/ee/user/project/issues/related_issues.html) to respective epic. Only issues with label from `issue.workflow.types` list are processed for result.
- It's possible to adapt configuration to project specific workflow, driven by usage of [Gitlab labels](https://docs.gitlab.com/ee/user/project/labels.html).
- It's also possible to configure [pagination, scope and state of issues](https://docs.gitlab.com/ee/api/issues.html) for more precisely focused results.

> For access token obtaining, see: [Project access tokens](https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html).  
> For sake of safety, it's recommended to provide required project access token as an environment variable `ISSUE_TRACKER_ACCESS_TOKEN` on hosting OS.
