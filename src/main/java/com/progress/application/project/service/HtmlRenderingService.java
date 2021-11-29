package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import j2html.tags.DomContent;
import j2html.tags.Tag;
import j2html.tags.UnescapedText;
import j2html.tags.specialized.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static j2html.TagCreator.*;

public class HtmlRenderingService {

    private static final String STYLESHEET_ATTR = "stylesheet";
    private static final String COL_SPAN_ATTR = "colspan";

    private final String title;
    private final IssueTrackingService issueTrackingService;

    public HtmlRenderingService(String title, IssueTrackingService issueTrackingService) {
        this.title = title;
        this.issueTrackingService = Objects.requireNonNull(issueTrackingService);
    }

    private List<String> provideReleases(List<Epic> epics) {
        return epics.stream()
            .flatMap(e -> e.getIssues().stream())
            .map(Issue::getMilestone)
            .filter(Objects::nonNull)
            .map(Milestone::getTitle)
            .distinct()
            .sorted(Comparator.reverseOrder())
            .toList();
    }

    private String render(Tag<?>... tags) {
        return document(
            html(
                head(
                    title(title),
                    link()
                        .withRel(STYLESHEET_ATTR)
                        .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/css/bootstrap.min.css"),
                    link()
                        .withRel(STYLESHEET_ATTR)
                        .withHref("css/mdb.dark.min.css"),
                    link()
                        .withRel(STYLESHEET_ATTR)
                        .withHref("css/style.css")
                ),
                body(
                    div()
                        .withClass("wrapper")
                        .with(tags),
                    script()
                        .withSrc("https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"),
                    script()
                        .withSrc("https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/js/bootstrap.min.js"),
                    script(new UnescapedText("""
                        var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
                        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
                          return new bootstrap.Tooltip(tooltipTriggerEl)
                        })"""))
                )
            )
        );
    }

    public String provideHtml() {
        return render(
                div()
                        .withId("content")
                        .withClass("container-fluid")
                        .with(provideBlockquote(), provideTable(), provideFooter()));
    }

    private BlockquoteTag provideBlockquote() {
        return blockquote()
                .withClasses("blockquote", "text-center", "m-4")
                .with(h3(title));
    }

    private PTag provideFooter() {
        return p(String.format("Last update: %s", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))))
                .withClasses("text-muted", "text-center");
    }

    private DivTag provideTable() {
        List<Epic> epics = issueTrackingService.getEpics();
        return div()
            .withClasses("container", "table-responsive-sm")
            .with(table()
                .withId("epicsTable")
                .withClasses("table", "table-sm", "table-bordered", "table-striped", "table-hover",
                    "align-middle")
                .with(provideTableHead(epics), provideTableBody(epics)));
    }

    private TheadTag provideTableHead(List<Epic> epics) {
        List<String> releases = provideReleases(epics);
        return thead()
            .withClasses("bg-dark", "align-middle")
            .with(tr()
                    .with(th("Epic")
                            .attr(COL_SPAN_ATTR, 2),
                        th("Issue")
                            .attr(COL_SPAN_ATTR, 4),
                        th("Release")
                            .attr(COL_SPAN_ATTR, releases.size())
                    ),
                tr()
                    .with(th("#"),
                        th("Description"),
                        th("#"),
                        th("Description"),
                        th("Type"),
                        th("Status"))
                    .with(provideHeadRows(releases))
            );
    }

    private List<DomContent> provideHeadRows(List<String> releases) {
        List<DomContent> rows = new ArrayList<>();
        for (String release : releases) {
            rows.add(th(release));
        }
        return rows;
    }

    private TbodyTag provideTableBody(List<Epic> epics) {
        return tbody().with(provideBodyRows(epics));
    }

    private List<DomContent> provideBodyRows(List<Epic> epics) {
        List<String> releases = provideReleases(epics);
        List<DomContent> rows = new ArrayList<>();
        for (Epic epic : epics) {
            rows.addAll(provideRows(epic, releases));
        }
        return rows;
    }

    private List<TrTag> provideRows(Epic epic, List<String> releases) {
        List<Issue> issues = epic.getIssues();
        if (issues.isEmpty()) {
            return List.of(tr()
                .with(td(a(String.valueOf(epic.getIid()))
                        .withHref(epic.getWebUrl())),
                    td(epic.getTitle()))
                .with(provideEmptyRow(releases.size())));
        } else {
            List<TrTag> rows = new ArrayList<>();
            for (Issue issue : issues) {
                TrTag tag;
                if (issues.indexOf(issue) == 0) {
                    tag = tr()
                        .with(td(a(String.valueOf(epic.getIid()))
                                .withHref(epic.getWebUrl()))
                                .attr("rowspan", issues.size()),
                            td(epic.getTitle())
                                .attr("rowspan", issues.size()))
                        .with(provideFilledRow(issue, releases));
                } else {
                    tag = tr().with(provideFilledRow(issue, releases));
                }
                rows.add(tag);
            }
            return rows;
        }
    }

    private List<DomContent> provideEmptyRow(int releasesCount) {
        List<DomContent> cells = new ArrayList<>();
        for (var i = 0; i < 4 + releasesCount; i++) {
            cells.add(td());
        }
        return cells;
    }

    private List<DomContent> provideFilledRow(Issue issue, List<String> releases) {
        List<DomContent> cells = new ArrayList<>();
        cells.add(td().with(
            a(issue.getIid()).withHref(issue.getWebUrl())));
        cells.add(td(issue.getTitle()).withClass("text-nowrap"));
        cells.add(td(issue.printLabels()).withClass("text-nowrap"));
        String state = issue.getState();
        String bgState = state.equals("opened") ? "bg-info" : "bg-success";
        cells.add(td(state).withClass(bgState));
        String release = issue.getMilestone() != null ? issue.getMilestone().getTitle() : "";
        String workFlow = issue.getWorkFlow().replace("workflow::", "");
        String bgMilestone = switch (workFlow) {
            case "blocked" -> "bgRed";
            case "" -> state.equals("opened") ? "bg-info" : "bg-success";
            default -> "bg-warning";
        };
        for (String rel : releases) {
            cells.add(rel.equals(release) ?
                td().withClass(bgMilestone)
                    .attr("data-bs-toggle", "tooltip")
                    .attr("data-bs-placement", "right")
                    .attr("title", workFlow)
                : td());
        }
        return cells;
    }
}
