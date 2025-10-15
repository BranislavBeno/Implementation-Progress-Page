package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import com.progress.application.project.domain.Milestone;
import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.Tag;
import j2html.tags.UnescapedText;
import j2html.tags.specialized.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                .sorted(this::compareReleases)
                .toList();
    }

    private int compareReleases(String c1, String c2) {
        try {
            Integer v1 = parseRelease(c1);
            Integer v2 = parseRelease(c2);
            return v2.compareTo(v1);
        } catch (Exception _) {
            return c2.compareTo(c1);
        }
    }

    private int parseRelease(String c) throws NumberFormatException {
        String e = c.replaceAll("\\D", "");
        return Integer.parseInt(e);
    }

    private String render(Tag<?>... tags) {
        return TagCreator.document(
                TagCreator.html(
                        TagCreator.head(
                                TagCreator.title(title),
                                TagCreator.link()
                                        .withRel(STYLESHEET_ATTR)
                                        .withHref("https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/css/bootstrap.min.css"),
                                TagCreator.link()
                                        .withRel(STYLESHEET_ATTR)
                                        .withHref("css/mdb.dark.min.css"),
                                TagCreator.link()
                                        .withRel(STYLESHEET_ATTR)
                                        .withHref("css/style.css")
                        ),
                        TagCreator.body(
                                TagCreator.div()
                                        .withClass("wrapper")
                                        .with(tags),
                                TagCreator.script()
                                        .withSrc("https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"),
                                TagCreator.script()
                                        .withSrc("https://cdn.jsdelivr.net/npm/bootstrap@5.0.1/dist/js/bootstrap.min.js"),
                                TagCreator.script(new UnescapedText("""
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
                TagCreator.div()
                        .withId("content")
                        .withClass("container-fluid")
                        .with(provideBlockquote(), provideTable(), provideFooter()));
    }

    private BlockquoteTag provideBlockquote() {
        return TagCreator.blockquote()
                .withClasses("blockquote", "text-center", "m-4")
                .with(TagCreator.h3(title));
    }

    private PTag provideFooter() {
        return TagCreator.p("Last update: %s".formatted(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))))
                .withClasses("text-muted", "text-center");
    }

    private DivTag provideTable() {
        List<Epic> epics = issueTrackingService.getEpics();
        return TagCreator.div()
                .withClasses("container", "table-responsive-sm")
                .with(TagCreator.table()
                        .withId("epicsTable")
                        .withClasses("table", "table-sm", "table-bordered", "table-striped", "table-hover",
                                "align-middle")
                        .with(provideTableHead(epics), provideTableBody(epics)));
    }

    private TheadTag provideTableHead(List<Epic> epics) {
        List<String> releases = provideReleases(epics);
        return TagCreator.thead()
                .withClasses("bg-dark", "align-middle")
                .with(TagCreator.tr()
                                .with(TagCreator.th("Epic")
                                                .attr(COL_SPAN_ATTR, 2),
                                        TagCreator.th("Issue")
                                                .attr(COL_SPAN_ATTR, 4),
                                        TagCreator.th("Release")
                                                .attr(COL_SPAN_ATTR, releases.size())
                                ),
                        TagCreator.tr()
                                .with(TagCreator.th("#"),
                                        TagCreator.th("Description"),
                                        TagCreator.th("#"),
                                        TagCreator.th("Description"),
                                        TagCreator.th("Type"),
                                        TagCreator.th("Status"))
                                .with(provideHeadRows(releases))
                );
    }

    private List<DomContent> provideHeadRows(List<String> releases) {
        List<DomContent> rows = new ArrayList<>();
        for (String release : releases) {
            rows.add(TagCreator.th(release));
        }
        return rows;
    }

    private TbodyTag provideTableBody(List<Epic> epics) {
        return TagCreator.tbody().with(provideBodyRows(epics));
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
            return List.of(TagCreator.tr()
                    .with(TagCreator.td(TagCreator.a(String.valueOf(epic.getIid()))
                                    .withHref(epic.getWebUrl())),
                            TagCreator.td(epic.getTitle()))
                    .with(provideEmptyRow(releases.size())));
        } else {
            List<TrTag> rows = new ArrayList<>();
            for (Issue issue : issues) {
                TrTag tag;
                if (issues.indexOf(issue) == 0) {
                    tag = TagCreator.tr()
                            .with(TagCreator.td(TagCreator.a(String.valueOf(epic.getIid()))
                                            .withHref(epic.getWebUrl()))
                                            .attr("rowspan", issues.size()),
                                    TagCreator.td(epic.getTitle())
                                            .attr("rowspan", issues.size()))
                            .with(provideFilledRow(issue, releases));
                } else {
                    tag = TagCreator.tr().with(provideFilledRow(issue, releases));
                }
                rows.add(tag);
            }
            return rows;
        }
    }

    private List<DomContent> provideEmptyRow(int releasesCount) {
        List<DomContent> cells = new ArrayList<>();
        for (var i = 0; i < 4 + releasesCount; i++) {
            cells.add(TagCreator.td());
        }
        return cells;
    }

    private List<DomContent> provideFilledRow(Issue issue, List<String> releases) {
        List<DomContent> cells = new ArrayList<>();
        cells.add(TagCreator.td().with(
                TagCreator.a(issue.getIid()).withHref(issue.getWebUrl())));
        cells.add(TagCreator.td(issue.getTitle()).withClass("text-nowrap"));
        cells.add(TagCreator.td(issue.printLabels()).withClass("text-nowrap"));
        String state = issue.getState();
        String bgState = state.equals("opened") ? "bg-info" : "bg-success";
        cells.add(TagCreator.td(state).withClass(bgState));
        String release = issue.getMilestone() != null ? issue.getMilestone().getTitle() : "";
        String workFlow = issue.getWorkFlow().replace("workflow::", "");
        String bgMilestone = switch (workFlow) {
            case "blocked" -> "bgRed";
            case "" -> state.equals("opened") ? "bg-info" : "bg-success";
            default -> "bg-warning";
        };
        for (String rel : releases) {
            cells.add(rel.equals(release) ?
                    TagCreator.td().withClass(bgMilestone)
                            .attr("data-bs-toggle", "tooltip")
                            .attr("data-bs-placement", "right")
                            .attr("title", workFlow)
                    : TagCreator.td());
        }
        return cells;
    }
}
