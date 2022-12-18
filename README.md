[![Application Tests](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/tests.yml/badge.svg)](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/tests.yml)
[![Docker Image Deploy](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/deploy.yml/badge.svg)](https://github.com/BranislavBeno/Implementation-Progress-Page/actions/workflows/deploy.yml)  
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_ImplementationProgressPage&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_ImplementationProgressPage)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_ImplementationProgressPage&metric=coverage)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_ImplementationProgressPage)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_ImplementationProgressPage&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_ImplementationProgressPage)  
[![](https://img.shields.io/badge/Java-19-blue)](/build.gradle)
[![](https://img.shields.io/badge/Spring%20Boot-3.0.0-blue)](/build.gradle)
[![](https://img.shields.io/badge/Testcontainers-1.17.6-blue)](/build.gradle)
[![](https://img.shields.io/badge/Gradle-7.6-blue)](/gradle/wrapper/gradle-wrapper.properties)
[![](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)  

## Backend application for static web page creation
This simple backend application creates static web page with fetched status of particular project related GitLab issues.  
Example results are available on GitLab pages under: [static web page](https://dashboard-tools.gitlab.io/Implementation-Progress-Page).  

Application works not as a web server, but simply fetches necessary data, creates web page and afterwards ends.  
In case you need to recreate the web page, you must run the application again.

### Installation
Preferred way of installation is to pull and run prepared docker image `docker pull beo1975/implementation-progress:1.4.0`.
Precondition is to have `docker` installed on the target system.

Alternatively is possible to build and run the application as a fat jar on any operating system with `Java 19` installed.

Application expects only running instance of Gitlab from which the issues will be imported. No other services (e.g. databases, message brokers,...) are required.