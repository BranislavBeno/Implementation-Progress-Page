package com.progress.application.project.service;

import com.progress.application.project.domain.Epic;
import com.progress.application.project.domain.Issue;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.Tag;
import j2html.tags.UnescapedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static j2html.TagCreator.*;

@Service
public class HtmlRenderingService {

  private static final String STYLESHEET_ATTR = "stylesheet";
  private static final String COL_SPAN_ATTR = "colspan";

  @Autowired
  private final IssueTrackingService issueTrackingService;

  public HtmlRenderingService(IssueTrackingService issueTrackingService) {
    this.issueTrackingService = Objects.requireNonNull(issueTrackingService);
  }

  private String render(Tag<?>... tags) {
    return document(
        html(
            head(
                title("Ronja CRM progress"),
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

  private Tag<ContainerTag> provideBlockquote() {
    return blockquote()
        .withClasses("blockquote", "text-center", "m-4")
        .with(h3("Ronja CRM progress"));
  }

  private Tag<ContainerTag> provideFooter() {
    return p(String.format("Last update: %s", LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"))))
        .withClasses("text-muted", "text-center");
  }

  private Tag<ContainerTag> provideTable() {
    return div()
        .withClasses("container", "table-responsive-sm")
        .with(table()
            .withId("epicsTable")
            .withClasses("table", "table-sm", "table-bordered", "table-striped", "table-hover",
                "align-middle")
            .with(provideTableHead(), provideTableBody()));
  }

  private Tag<ContainerTag> provideTableHead() {
    return thead()
        .withClasses("bg-dark", "align-middle")
        .with(tr()
                .with(th("Epic")
                        .attr(COL_SPAN_ATTR, 2),
                    th("Issue")
                        .attr(COL_SPAN_ATTR, 4),
                    th("Release")
                        .attr(COL_SPAN_ATTR, issueTrackingService.getReleaseCount())
                ),
            tr()
                .with(th("#"),
                    th("Description"),
                    th("#"),
                    th("Description"),
                    th("Type"),
                    th("Status"))
                .with(provideHeadRows(issueTrackingService.getReleases()))
        );
  }

  private List<DomContent> provideHeadRows(List<String> releases) {
    List<DomContent> rows = new ArrayList<>();
    for (String release : releases) {
      rows.add(th(release));
    }
    return rows;
  }

  private Tag<ContainerTag> provideTableBody() {
    return tbody().with(provideBodyRows());
  }

  private List<DomContent> provideBodyRows() {
    List<DomContent> rows = new ArrayList<>();
    for (Epic epic : issueTrackingService.getEpics()) {
      rows.addAll(provideRows(epic));
    }
    return rows;
  }

  private List<ContainerTag> provideRows(Epic epic) {
    List<Issue> issues = epic.getIssues();
    if (issues.isEmpty()) {
      return List.of(tr()
          .with(td(a(String.valueOf(epic.getIid()))
                  .withHref(epic.getWebUrl())),
              td(epic.getTitle()))
          .with(provideEmptyRew()));
    } else {
      List<ContainerTag> rows = new ArrayList<>();
      for (Issue issue : issues) {
        ContainerTag tag;
        if (issues.indexOf(issue) == 0) {
          tag = tr()
              .with(td(a(String.valueOf(epic.getIid()))
                      .withHref(epic.getWebUrl()))
                      .attr("rowspan", issues.size()),
                  td(epic.getTitle())
                      .attr("rowspan", issues.size()))
              .with(provideFilledRow(issue));
        } else {
          tag = tr().with(provideFilledRow(issue));
        }
        rows.add(tag);
      }
      return rows;
    }
  }

  private List<DomContent> provideEmptyRew() {
    List<DomContent> cells = new ArrayList<>();
    for (var i = 0; i < 4 + issueTrackingService.getReleaseCount(); i++) {
      cells.add(td());
    }
    return cells;
  }

  private List<DomContent> provideFilledRow(Issue issue) {
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
    for (String rel : issueTrackingService.getReleases()) {
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
