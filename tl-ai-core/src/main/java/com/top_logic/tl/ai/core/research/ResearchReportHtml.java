/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.util.Resources;

/**
 * Renders a {@link ResearchReport} as one self-contained HTML document.
 *
 * <p>
 * The document is what the chat's side pane shows of a research run and what is downloaded: the
 * report with numbered footnotes, each linked to its entry in the list of the sources it cites, with
 * their addresses and archived copies. What the run read and did not cite, and the findings, are
 * parts of the run itself, where the pane folds them away. The document needs nothing but itself:
 * the styles are inline, no script runs, every link is an absolute address.
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
 * application makes HTML of model text - and everything else is escaped. A link is written only for
 * an http(s) address.
 * </p>
 */
public final class ResearchReportHtml {

	/** A Markdown heading in the first line of a text. */
	private static final Pattern LEADING_HEADING = Pattern.compile("#{1,6}[ \\t]+([^\\n]*)\\n?");

	/** A footnote marker the citation validator left in the text. */
	private static final Pattern FOOTNOTE = Pattern.compile("\\[(\\d{1,3})\\]");

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

	private ResearchReportHtml() {
		// Static utility.
	}

	/**
	 * The report as an HTML document.
	 *
	 * @param report
	 *        What is kept of the run.
	 * @param locale
	 *        The language of the headings - the language of the user the run worked for.
	 * @param zone
	 *        The time zone dates are written in.
	 */
	public static String render(ResearchReport report, Locale locale, ZoneId zone) {
		Resources resources = Resources.getInstance(locale);
		DateTimeFormatter dates = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(zone);
		StringBuilder out = new StringBuilder();
		out.append("<!DOCTYPE html>\n<html lang=\"").append(escape(locale.getLanguage())).append("\">\n<head>\n")
			.append("<meta charset=\"utf-8\">\n")
			.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
			.append("<title>").append(escape(report.question())).append("</title>\n")
			.append("<style>").append(STYLE).append("</style>\n</head>\n<body>\n<div class=\"report\">\n");
		// Title and date are for the reader of the download: the chat's side pane shows both as
		// fields of the run and leaves out what is marked "download-only".
		out.append("<p class=\"title download-only\"><strong>").append(escape(report.question())).append("</strong></p>\n");
		out.append("<p class=\"meta download-only\">")
			.append(escape(resources.getString(I18NConstants.REPORT_META__DATE.fill(dates.format(report.finished())))));
		if (!report.effort().isEmpty()) {
			out.append(" · ").append(escape(report.effort()));
		}
		out.append("</p>\n");
		if (ResearchReport.PARTIAL.equals(report.status())) {
			out.append("<p class=\"status\">").append(escape(resources.getString(I18NConstants.PARTIAL))).append("</p>\n");
		}

		out.append("<div class=\"text\">\n").append(footnotes(Converters.markdownToHtmlString(withoutTitle(report)),
			report.cited().size())).append("\n</div>\n");

		if (!report.cited().isEmpty()) {
			out.append("<h2>").append(escape(resources.getString(I18NConstants.SOURCES))).append("</h2>\n");
			out.append("<ol class=\"sources\">\n");
			int n = 1;
			for (ResearchReport.Source source : report.cited()) {
				out.append("<li id=\"source-").append(n++).append("\">");
				source(out, source, resources, dates);
				if (!source.passage().isEmpty()) {
					out.append("<blockquote>").append(escape(source.passage())).append("</blockquote>");
				}
				out.append("</li>\n");
			}
			out.append("</ol>\n");
		}

		out.append("</div>\n</body>\n</html>\n");
		return out.toString();
	}

	/**
	 * The report text without a first heading that only repeats the question, which the document
	 * already shows as its title.
	 */
	static String withoutTitle(ResearchReport report) {
		String text = report.text().stripLeading();
		Matcher heading = LEADING_HEADING.matcher(text);
		if (heading.lookingAt() && squeeze(heading.group(1)).equalsIgnoreCase(squeeze(report.question()))) {
			return text.substring(heading.end()).stripLeading();
		}
		return report.text();
	}

	private static String squeeze(String text) {
		return text.replaceAll("[\\s*_]+", " ").strip();
	}

	/** Turns every footnote marker {@code [n]} of a cited source into a link to its entry. */
	static String footnotes(String html, int count) {
		Matcher matcher = FOOTNOTE.matcher(html);
		StringBuilder out = new StringBuilder();
		while (matcher.find()) {
			int n = Integer.parseInt(matcher.group(1));
			String replacement = n >= 1 && n <= count
				? "<sup><a href=\"#source-" + n + "\">[" + n + "]</a></sup>"
				: matcher.group();
			matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(out);
		return out.toString();
	}

	private static void source(StringBuilder out, ResearchReport.Source source, Resources resources,
			DateTimeFormatter dates) {
		link(out, source.url(), source.title());
		out.append("<div class=\"where\">").append(escape(source.url()));
		Instant readAt = source.readAt();
		if (readAt != null) {
			out.append(" · ").append(escape(resources.getString(I18NConstants.READ_AT__DATE.fill(dates.format(readAt)))));
		}
		if (isWebAddress(source.archive())) {
			out.append(" · ");
			link(out, source.archive(), resources.getString(I18NConstants.ARCHIVED_COPY));
		}
		out.append("</div>");
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
