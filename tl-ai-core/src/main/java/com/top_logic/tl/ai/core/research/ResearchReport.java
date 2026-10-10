/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.research;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.citation.Finding;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * What is kept of a research run: the report, what it cites and what else the run tree read.
 *
 * <p>
 * Composed on the thread of the run that wrote the report, where its {@link ReadingList} - the
 * sources and findings of the whole run tree - is at hand, and handed to {@link ResearchRuns} as
 * plain data, which writes it on a thread of its own.
 * </p>
 *
 * <p>
 * <b>Cited or not.</b> The report's links are validated against the reading list
 * ({@link CitationValidator}): a link to a source the run tree read becomes a footnote, any other
 * link loses its link. The sources behind the footnotes are the cited sources, in the order of the
 * footnotes; every other source on the list was read and not cited - "also read". A finding belongs
 * with the cited ones when the report cites its source, and is listed apart otherwise.
 * </p>
 *
 * @param question
 *        What the run answered.
 * @param effort
 *        quick, standard or deep; empty when not given.
 * @param plan
 *        The sub-questions; empty when not given.
 * @param status
 *        complete or partial.
 * @param markdown
 *        The report as the lead wrote it.
 * @param text
 *        The report with its links turned into footnotes {@code [n]}.
 * @param cited
 *        The cited sources, footnote {@code n} at index {@code n - 1}.
 * @param alsoRead
 *        The sources read and not cited.
 * @param findings
 *        The findings whose source is cited.
 * @param otherFindings
 *        The findings whose source is not cited.
 * @param tokens
 *        What the run tree spent, or {@code null} when it was not counted.
 * @param started
 *        When the run tree read its first source.
 * @param finished
 *        When the report was kept.
 */
public record ResearchReport(String question, String effort, String plan, String status, String markdown,
		String text, List<Source> cited, List<Source> alsoRead, List<Finding> findings, List<Finding> otherFindings,
		Long tokens, Instant started, Instant finished) {

	/** The status of a report that answers its question. */
	public static final String COMPLETE = "complete";

	/** The status of a report of a run that stopped before it had answered everything. */
	public static final String PARTIAL = "partial";

	/**
	 * One source of the report.
	 *
	 * @param title
	 *        Its title.
	 * @param url
	 *        The address of a web page, or the id of an object.
	 * @param kind
	 *        The source kind of the reading list.
	 * @param archive
	 *        Where the copy the run read is kept; empty for none.
	 * @param readAt
	 *        When it was read; {@code null} when unknown.
	 * @param passage
	 *        The words of the source the report quotes in its citation; empty for none.
	 */
	public record Source(String title, String url, String kind, String archive, Instant readAt, String passage) {
		// Value type only.
	}

	/**
	 * Composes what is kept of a research run.
	 *
	 * @param question
	 *        What the run answered.
	 * @param markdown
	 *        The report, with links to the sources it rests on.
	 * @param effort
	 *        quick, standard or deep; may be {@code null}.
	 * @param plan
	 *        The sub-questions; may be {@code null}.
	 * @param status
	 *        complete or partial; {@code null} for complete.
	 * @param read
	 *        The reading list of the run tree.
	 * @param tokens
	 *        What the run tree spent, or {@code null} when it was not counted.
	 * @param now
	 *        The time the report is kept.
	 */
	public static ResearchReport compose(String question, String markdown, String effort, String plan, String status,
			ReadingList read, Long tokens, Instant now) {
		CitationValidator.Result validated = CitationValidator.validate(markdown, read, List.of());
		List<Source> cited = new ArrayList<>();
		Set<String> citedKeys = new HashSet<>();
		for (Citation citation : validated.citations()) {
			ReadSource source = read.find(citation.id());
			String reference = citation.url() == null || citation.url().isEmpty() ? citation.id() : citation.url();
			cited.add(new Source(nonEmpty(citation.label(), reference), reference, citation.kind(),
				nullToEmpty(citation.archiveUrl()), source == null ? null : source.readAt(),
				nullToEmpty(citation.passage())));
			if (source != null) {
				citedKeys.add(key(source));
			}
			citedKeys.add(key(reference));
		}
		List<Source> alsoRead = new ArrayList<>();
		Instant started = now;
		for (ReadSource source : read.sources()) {
			if (source.readAt() != null && source.readAt().isBefore(started)) {
				started = source.readAt();
			}
			if (citedKeys.contains(key(source))) {
				continue;
			}
			String reference = source.url().isEmpty() ? source.id() : source.url();
			alsoRead.add(new Source(nonEmpty(source.title(), reference), reference, source.kind(),
				source.archiveUrl(), source.readAt(), ""));
		}
		List<Finding> findings = new ArrayList<>();
		List<Finding> otherFindings = new ArrayList<>();
		for (Finding finding : read.findings()) {
			ReadSource source = read.find(finding.source());
			boolean isCited = citedKeys.contains(key(finding.source())) || (source != null && citedKeys.contains(key(source)));
			(isCited ? findings : otherFindings).add(finding);
		}
		return new ResearchReport(question.strip(), nullToEmpty(effort).strip(), nullToEmpty(plan).strip(),
			status == null || status.isBlank() ? COMPLETE : status.strip().toLowerCase(Locale.ROOT), markdown,
			validated.text(), cited, alsoRead, findings, otherFindings, tokens, started, now);
	}

	private static String key(ReadSource source) {
		return key(source.url().isEmpty() ? source.id() : source.url());
	}

	private static String key(String reference) {
		return reference == null ? "" : reference.strip().toLowerCase(Locale.ROOT);
	}

	private static String nonEmpty(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value.strip();
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
