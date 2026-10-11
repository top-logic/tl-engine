/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tl.ai.core.ApplicationUrl;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.execution.model.ArchivedPage;
import com.top_logic.tl.ai.core.execution.model.ResearchRun;
import com.top_logic.tl.ai.core.execution.model.ResearchSource;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.tool.api.ReadSource;
import com.top_logic.util.Resources;

/**
 * Renders a {@link ResearchRun} as one self-contained HTML document.
 *
 * <p>
 * The document is what the chat's side pane shows of a research run and what is downloaded: the
 * report with numbered footnotes, each linked to its entry in the list of the sources it cites, with
 * their addresses and archived copies. What the run read and did not cite, and the findings, are
 * parts of the run itself, where the pane folds them away.
 * </p>
 *
 * <p>
 * <b>Rendered, never stored.</b> The report the lead wrote is the run's one source of truth. Its
 * links are validated again against the run's cited sources ({@link CitationValidator}), each
 * quoted passage against the archived text of its page, so the document always says what the run
 * holds now - a source whose object was deleted meanwhile is named as deleted, under the footnote
 * number it had.
 * </p>
 *
 * <p>
 * <b>Relative links into the application.</b> An archived copy is linked by its path below the
 * context path of the current request ({@link #contextPath()}), never with a host, so a downloaded
 * document opened from the same installation links back into it whichever address it was reached
 * at. The styles are inline and no script runs.
 * </p>
 *
 * <p>
 * <b>Only tags the application's HTML safety check allows</b> - {@code div}, not {@code main} or
 * {@code details} - because the side pane shows the document's body in place, where that check
 * applies.
 * </p>
 *
 * <p>
 * <b>Untrusted text.</b> The report is a model's text and the titles and quotes are pages' text, so
 * the report goes through {@link Converters#markdownToHtmlString(String)} - the one way this
 * application makes HTML of model text - and everything else is escaped. A source is linked only
 * for an http(s) address.
 * </p>
 */
public final class ResearchDocument {

	/** What the document is. */
	public static final String CONTENT_TYPE = "text/html; charset=utf-8";

	/** The name the document is downloaded under. */
	public static final String FILE_NAME = "research-report.html";

	/**
	 * Where a footnote marker stands in the report while it is rendered: private-use characters,
	 * which neither a report nor the Markdown renderer produces.
	 */
	private static final char NOTE_START = '';

	private static final char NOTE_END = '';

	private static final Pattern NOTE = Pattern.compile(NOTE_START + "(\\d{1,9})" + NOTE_END);

	/** A Markdown heading in the first line of a text. */
	private static final Pattern LEADING_HEADING = Pattern.compile("#{1,6}[ \\t]+([^\\n]*)\\n?");

	/** The opening or closing tag of a heading of level 1 to 5. */
	private static final Pattern HEADING_TAG = Pattern.compile("<(/?)h([1-5])(?=[\\s>])");

	private static final String STYLE = """
		:root { color-scheme: light; }
		body { font: 16px/1.6 -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
			color: #1d2430; background: #ffffff; margin: 0; }
		.report { max-width: 46rem; margin: 0 auto; padding: 2rem 1.5rem 3rem; }
		.title { font-size: 1.5rem; line-height: 1.3; margin: 0 0 .25rem; }
		h2 { font-size: 1.15rem; margin: 2rem 0 .75rem; padding-bottom: .25rem; border-bottom: 1px solid #dde2ea; }
		.meta { color: #5b6573; font-size: .875rem; margin: 0 0 1.5rem; }
		.status { background: #fff4d6; border-left: 3px solid #d39b00; padding: .5rem .75rem; font-size: .9rem; }
		a { color: #0b5cad; }
		sup a { text-decoration: none; padding: 0 .1em; }
		ol.sources { padding-left: 1.5rem; }
		ol.sources li { margin-bottom: .75rem; }
		.where { color: #5b6573; font-size: .8rem; overflow-wrap: anywhere; }
		blockquote { margin: .35rem 0 0; padding: .25rem .75rem; border-left: 3px solid #dde2ea; color: #3b4452; }
		table { border-collapse: collapse; } td, th { border: 1px solid #dde2ea; padding: .25rem .5rem; }
		@media print { .report { max-width: none; padding: 0; } }
		""";

	private ResearchDocument() {
		// Static utility.
	}

	/**
	 * The document of a run as the current user reads it: in their language and time zone, linked
	 * below the context path of the current request.
	 */
	public static BinaryData of(ResearchRun run) {
		String html = render(run, ThreadContext.getLocale(), ThreadContext.getTimeZone().toZoneId(), contextPath());
		return BinaryDataFactory.createBinaryData(html.getBytes(StandardCharsets.UTF_8), CONTENT_TYPE, FILE_NAME);
	}

	/**
	 * The context path the application is reached at: the current request's, otherwise the one the
	 * application is deployed under - an empty string at the root of the server and in a test.
	 */
	public static String contextPath() {
		if (DefaultDisplayContext.hasDisplayContext()) {
			HttpServletRequest request = DefaultDisplayContext.getDisplayContext().asRequest();
			if (request != null) {
				return request.getContextPath();
			}
		}
		return ApplicationUrl.contextPath();
	}

	/**
	 * The run as an HTML document.
	 *
	 * @param run
	 *        The run.
	 * @param locale
	 *        The language of the headings.
	 * @param zone
	 *        The time zone dates are written in.
	 * @param contextPath
	 *        The context path archived copies are linked below; empty at the root of the server.
	 */
	public static String render(ResearchRun run, Locale locale, ZoneId zone, String contextPath) {
		Resources resources = Resources.getInstance(locale);
		DateTimeFormatter dates =
			DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone);
		String question = nonNull(run.getQuestion());

		ReadingList cited = new ReadingList();
		Map<String, ResearchSource> byId = new HashMap<>();
		List<ResearchSource> sources = new ArrayList<>(run.getCitedSources());
		sources.sort(Comparator.comparing(ResearchSource::getFootnote, Comparator.nullsLast(Comparator.naturalOrder())));
		for (ResearchSource source : sources) {
			ReadSource read = readSource(source, contextPath);
			cited.add(read);
			byId.put(read.id(), source);
		}
		CitationValidator.Result validated = CitationValidator.validate(nonNull(run.getReport()), cited, List.of());

		StringBuilder out = new StringBuilder();
		out.append("<!DOCTYPE html>\n<html lang=\"").append(escape(locale.getLanguage())).append("\">\n<head>\n")
			.append("<meta charset=\"utf-8\">\n")
			.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
			.append("<title>").append(escape(question)).append("</title>\n")
			.append("<style>").append(STYLE).append("</style>\n</head>\n<body>\n<div class=\"report\">\n");
		// Title and date are for the reader of the download: the chat's side pane shows both as
		// fields of the run and leaves out what is marked "download-only".
		out.append("<p class=\"title download-only\"><strong>").append(escape(question)).append("</strong></p>\n");
		out.append("<p class=\"meta download-only\">");
		Date finished = run.getFinished();
		if (finished != null) {
			out.append(escape(resources.getString(I18NConstants.REPORT_META__DATE.fill(dates.format(finished.toInstant())))));
		}
		TLClassifier effort = run.getEffort();
		if (effort != null) {
			out.append(" · ").append(escape(resources.getString(TLModelNamingConvention.classifierKey(effort))));
		}
		out.append("</p>\n");
		if (run.getStatus() == TlAiExecutionFactory.getPARTIALResearchStatusClassifier()) {
			out.append("<p class=\"status\">").append(escape(resources.getString(I18NConstants.PARTIAL))).append("</p>\n");
		}

		int count = validated.citations().size();
		String text = CitationValidator.replaceFootnotes(withoutTitle(validated.text(), question), count,
			n -> NOTE_START + Integer.toString(n) + NOTE_END);
		out.append("<div class=\"text\">\n")
			.append(footnotes(belowTitle(Converters.markdownToHtmlString(text)), count))
			.append("\n</div>\n");

		if (count > 0) {
			out.append("<h2>").append(escape(resources.getString(I18NConstants.SOURCES))).append("</h2>\n");
			out.append("<ol class=\"sources\">\n");
			for (Citation citation : validated.citations()) {
				out.append("<li id=\"source-").append(citation.index()).append("\">");
				entry(out, citation, byId.get(citation.id()), resources, dates);
				if (citation.passage() != null && !citation.passage().isEmpty()) {
					out.append("<blockquote>").append(escape(citation.passage())).append("</blockquote>");
				}
				out.append("</li>\n");
			}
			out.append("</ol>\n");
		}

		out.append("</div>\n</body>\n</html>\n");
		return out.toString();
	}

	/**
	 * A stored source as the reading list knew it when the report was validated, so that the
	 * report's links point at it again: a page by its address, an object by its id.
	 */
	private static ReadSource readSource(ResearchSource source, String contextPath) {
		Instant readAt = source.getReadAt() == null ? null : source.getReadAt().toInstant();
		String name = nonNull(source.getDisplayName());
		ArchivedPage page = source.getPage();
		if (page != null) {
			String archive = contextPath + PageArchive.URL_PREFIX + page.getHandle();
			return new ReadSource(kind(source, CitationKinds.WEB), page.getUrl(), name, page.getUrl(),
				PageArchive.text(page), archive, readAt);
		}
		TLObject object = source.getObject();
		if (object != null) {
			return new ReadSource(kind(source, CitationKinds.OBJECT), ReadFilter.idOf(object), name, "", "", "", readAt);
		}
		String reference = nonNull(source.getUrl());
		return new ReadSource(kind(source, ""), reference, name, isWebAddress(reference) ? reference : "", "", "",
			readAt);
	}

	private static String kind(ResearchSource source, String fallback) {
		String kind = source.getKind();
		return kind == null || kind.isEmpty() ? fallback : kind;
	}

	private static void entry(StringBuilder out, Citation citation, ResearchSource source, Resources resources,
			DateTimeFormatter dates) {
		String url = nonNull(citation.url());
		link(out, url, nonNull(citation.label()));
		out.append("<div class=\"where\">");
		String separator = "";
		if (!url.isEmpty()) {
			out.append(escape(url));
			separator = " · ";
		}
		Date readAt = source == null ? null : source.getReadAt();
		if (readAt != null) {
			out.append(separator)
				.append(escape(resources.getString(I18NConstants.READ_AT__DATE.fill(dates.format(readAt.toInstant())))));
			separator = " · ";
		}
		String archive = nonNull(citation.archiveUrl());
		if (!archive.isEmpty()) {
			out.append(separator).append("<a href=\"").append(escape(archive))
				.append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
				.append(escape(resources.getString(I18NConstants.ARCHIVED_COPY))).append("</a>");
		}
		out.append("</div>");
	}

	/**
	 * The report text without a first heading that only repeats the question, which the document
	 * already shows as its title.
	 */
	static String withoutTitle(String report, String question) {
		String text = report.stripLeading();
		Matcher heading = LEADING_HEADING.matcher(text);
		if (heading.lookingAt() && squeeze(heading.group(1)).equalsIgnoreCase(squeeze(question))) {
			return text.substring(heading.end()).stripLeading();
		}
		return report;
	}

	/**
	 * The report's headings one level lower: the question is the document's title, so the report's
	 * own {@code #} is a section of it, not a second title (#800).
	 */
	static String belowTitle(String html) {
		Matcher matcher = HEADING_TAG.matcher(html);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			int level = Integer.parseInt(matcher.group(2)) + 1;
			matcher.appendReplacement(out, "<" + matcher.group(1) + "h" + level);
		}
		matcher.appendTail(out);
		return out.toString();
	}

	private static String squeeze(String text) {
		return text.replaceAll("[\\s*_]+", " ").strip();
	}

	/** Turns the footnote markers left in the rendered report into links to their entries. */
	private static String footnotes(String html, int count) {
		Matcher matcher = NOTE.matcher(html);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			int n = Integer.parseInt(matcher.group(1));
			String replacement = n >= 1 && n <= count
				? "<sup><a href=\"#source-" + n + "\">[" + n + "]</a></sup>"
				: "[" + n + "]";
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	/** A link for an http(s) address; the text alone for anything else. */
	private static void link(StringBuilder out, String url, String text) {
		if (isWebAddress(url)) {
			// A new tab: shown in place inside the application, a link must not take the page away.
			out.append("<a href=\"").append(escape(url)).append("\" target=\"_blank\" rel=\"noopener noreferrer\">")
				.append(escape(text)).append("</a>");
		} else {
			out.append(escape(text));
		}
	}

	private static boolean isWebAddress(String url) {
		if (url == null || url.isBlank()) {
			return false;
		}
		try {
			String scheme = URI.create(url.strip()).getScheme();
			return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
		} catch (IllegalArgumentException ex) {
			return false;
		}
	}

	/**
	 * What a source is called: the page's title, the object's label, or the title it was kept with
	 * - marked as deleted when its page or its object was deleted.
	 *
	 * @param source
	 *        The source.
	 * @param resources
	 *        The language of the mark.
	 */
	public static String name(ResearchSource source, Resources resources) {
		ArchivedPage page = source.getPage();
		if (page != null) {
			return nonBlank(page.getTitle(), page.getUrl());
		}
		TLObject object = source.getObject();
		if (object != null) {
			return MetaLabelProvider.INSTANCE.getLabel(object);
		}
		String title = nonBlank(source.getTitle(), source.getUrl());
		if (title.isEmpty()) {
			// A web source whose archived page an administrator deleted: nothing of it is left.
			return resources.getString(I18NConstants.DELETED_PAGE);
		}
		return isObjectId(source.getUrl()) ? deleted(title, resources) : title;
	}

	/**
	 * Whether {@code reference} is the id of an object of this application - which a source without
	 * its object keeps only when the object was deleted. An application's own record is cited by an
	 * id of its own, which names no table here.
	 */
	static boolean isObjectId(String reference) {
		if (reference == null) {
			return false;
		}
		int colon = reference.indexOf(':');
		if (colon <= 0 || colon == reference.length() - 1) {
			return false;
		}
		try {
			if (PersistencyLayer.getKnowledgeBase().getMORepository().getTypeOrNull(reference.substring(0, colon)) == null) {
				return false;
			}
			return IdentifierUtil.fromExternalForm(reference.substring(colon + 1)) != null;
		} catch (RuntimeException ex) {
			return false;
		}
	}

	private static String deleted(String name, Resources resources) {
		return resources.getString(I18NConstants.DELETED__NAME.fill(name));
	}

	private static String nonBlank(String value, String fallback) {
		return value == null || value.isBlank() ? nonNull(fallback) : value;
	}

	private static String nonNull(String value) {
		return value == null ? "" : value;
	}

	/** Escapes text for HTML content and attribute values. */
	static String escape(String text) {
		if (text == null) {
			return "";
		}
		StringBuilder out = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			switch (c) {
				case '<' -> out.append("&lt;");
				case '>' -> out.append("&gt;");
				case '&' -> out.append("&amp;");
				case '"' -> out.append("&quot;");
				case '\'' -> out.append("&#39;");
				default -> out.append(c);
			}
		}
		return out.toString();
	}
}
