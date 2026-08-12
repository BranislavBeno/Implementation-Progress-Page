# Copilot Instructions

## What this app does
A Spring Boot **command-line batch job**, not a web server. On startup it:
1. Fetches GitLab "Epic" issues (label `Epic`) and their linked issues via the GitLab REST API.
2. Renders a static HTML dashboard (`public/index.html`) showing epic/issue/release status using j2html.
3. Exits (`applicationContext.close()` in `ProjectApplication.main`).

`spring.main.web-application-type: none` — there is no HTTP endpoint. The generated `public/` folder is meant to be published as GitLab Pages.

## Build, test, lint
- Build: `./gradlew assemble` (or `gradlew.bat assemble` on Windows)
- Run all tests + coverage: `./gradlew jacocoTestReport` (auto-runs `test`, finalized by jacoco)
- Run a single test class: `./gradlew test --tests "com.progress.application.project.service.HtmlRenderingServiceTest"`
- Run a single test method: `./gradlew test --tests "com.progress.application.project.webclient.IssueWebClientTest.methodName"`
- Static analysis / formatting via OpenRewrite: `./gradlew rewriteRun` (active recipes defined in `rewrite.yml`: remove static imports for `j2html.TagCreator`/`Mockito`, remove unused imports, order imports, Java 25 migration, Spring Boot 4.0 migration). Run `rewriteRun` after larger refactors touching imports.
- SonarQube analysis (used in CI): `./gradlew jacocoTestReport sonar` (requires `SONAR_TOKEN`).
- Toolchain: Java 25, Gradle wrapper — always use `./gradlew`, not a system Gradle.

## Architecture (why multiple files matter together)
Flow for generating the page:
`ProjectApplication` → `WebPageProvider` → `HtmlRenderingService` → `IssueTrackingService` → `IssueWebClient` → GitLab API.

- **`ProjectConfiguration`**: the only place beans are wired (constructor injection is manual via `@Bean` methods, not `@Component`/`@Service` annotations on the classes themselves). When adding a new service/config value, add a `@Bean` method here and a matching key in `application.yml`.
- **`webclient` package** (`AccessData`, `EpicData`, `IssueData`, `IssueWebClient`): thin wrapper over GitLab's REST API using `WebClient` (blocking `.block()` calls — intentional, since this is a one-shot batch job, not reactive server code). `AccessData` bundles the GitLab connection info (`baseUrl`, `groupId`, `projectId`, `accessToken`) plus nested `EpicData`/`IssueData` request templates (URL, pagination, scope, state).
- **`service.IssueTrackingService`**: fetches epics, filters by `projectId`, then for each epic fetches its linked issues (`importIssues`). Issues get a `workFlow` label extracted (anything starting with `issue.tracker.workflow.prefix`, e.g. `workflow::blocked`) and labels are filtered down to `issue.tracker.workflow.types`.
- **`service.HtmlRenderingService`**: pure rendering logic using j2html `TagCreator`. Builds a table where columns are dynamic release/milestone names (derived from all issues' milestones, sorted numerically-descending by `parseRelease`). Cell coloring encodes workflow state (`bgRed` = blocked, `bg-warning` = other workflow type, `bg-info`/`bg-success` = open/closed with no workflow).
- **`domain`** package (`Epic`, `Issue`, `Milestone`): DTOs deserialized directly from GitLab API JSON responses.
- **`WebPageProvider`**: copies `src/main/resources/static/**` (CSS assets) into `public/`, then writes the rendered HTML to `public/index.html`. Failures during file I/O are logged, not thrown (app degrades gracefully rather than crashing on partial write failures).

## Configuration (`application.yml`)
All GitLab connection and workflow settings live under `issue.tracker.*` and `issue.workflow.*` (see README for full example). Key points:
- `issue.tracker.access-token` should come from the `ISSUE_TRACKER_ACCESS_TOKEN` env var in real deployments, never hardcoded.
- `issue.workflow.types` is the allowlist of labels shown as the issue "Type" column; unlisted labels are stripped.
- `spring.application.ui.title` sets the page title/header text.

## Testing conventions
- Tests use `@SpringBootTest` with narrow `classes = ...` slices (not full app context) plus `@Import` of `ProjectTestConfiguration` (a test-only mirror of `ProjectConfiguration`'s bean wiring).
- GitLab API calls are tested against a real containerized mock server: `@Testcontainers(disabledWithoutDocker = true)` + `MockServerContainer` (Testcontainers), with expectations set via `MockServerClient`, and `@DynamicPropertySource` used to point `issue.tracker.*` properties at the container's dynamic port. Docker must be available locally to run these tests.
- Static imports are disallowed for `j2html.TagCreator.*` and `org.mockito.Mockito.*` (enforced by the OpenRewrite recipe) — always call these fully-qualified (`TagCreator.div(...)`, `Mockito.mock(...)`).

## CI/CD
- `.github/workflows/tests.yml`: runs on every push/PR — `./gradlew jacocoTestReport sonar`.
- `.github/workflows/deploy.yml`: triggered after a successful test run on `main`; only rebuilds/pushes the multi-arch Docker image (`beo1975/implementation-progress:<version>` and `:latest`) if `src/main/**`, `build.gradle`, `Dockerfile`, or the deploy workflow itself changed. Bump the Docker tag in both `build.gradle` (`version`) and `deploy.yml` together when releasing.
- Docker image is a multi-stage build (Alpine Zulu JDK 25) that assembles a fat jar, then extracts Spring Boot layered jars for a leaner runtime image.
