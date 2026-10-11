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
import java.util.function.Function;

import com.top_logic.basic.Logger;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.citation.CitationKinds;
import com.top_logic.tl.ai.core.citation.CitationValidator;
import com.top_logic.tl.ai.core.citation.Finding;
import com.top_logic.tl.ai.core.citation.PageArchive;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.tool.api.ReadSource;

/**
 * What is kept of a research run: the report, the sources the run tree read and its findings.
 *
 * <p>
 * Composed on the thread of the run that wrote the report, where its {@link ReadingList} - the
 * sources and findings of the whole run tree - is at hand and an object a source names can be
 * resolved under the run's read rights. Handed to {@link ResearchRuns} as plain data, which writes
 * it on a thread of its own.
 * </p>
 *
 * <p>
 * <b>Cited or not.</b> The report's links are validated against the reading list
 * ({@link CitationValidator}): a link to a source the run tree read becomes a footnote. The sources
 * behind the footnotes are the cited sources, in the order of the footnotes; every other source on
 * the list was read and not cited - "also read".
 * </p>
 *
 * @param question
 *        What the run answered.
 * @param effort
 *        {@link #QUICK}, {@link #STANDARD} or {@link #DEEP}; empty when not given.
 * @param plan
 *        The sub-questions; empty when not given.
 * @param status
 *        {@link #COMPLETE} or {@link #PARTIAL}.
 * @param markdown
 *        The report as the lead wrote it - what is kept, and what the document is rendered from.
 * @param cited
 *        The cited sources, footnote {@code n} at index {@code n - 1}.
 * @param alsoRead
 *        The sources read and not cited, in the order they were read.
 * @param findings
 *        The findings of the run tree, in the order they were recorded or received.
 * @param leadRunId
 *        The run id of the lead run, which the run is kept for once; empty outside a run.
 * @param started
 *        When the run tree read its first source.
 * @param finished
 *        When the report was kept.
 */
public record ResearchReport(String question, String effort, String plan, String status, String markdown,
		List<Source> cited, List<Source> alsoRead, List<Finding> findings, String leadRunId, Instant started,
		Instant finished) {

	/** The status of a report that answers its question. */
	public static final String COMPLETE = "complete";

	/** The status of a report of a run that stopped before it had answered everything. */
	public static final String PARTIAL = "partial";

	/** The effort of a quick research. */
	public static final String QUICK = "quick";

	/** The effort of a standard research. */
	public static final String STANDARD = "standard";

	/** The effort of a deep research. */
	public static final String DEEP = "deep";

	/** The statuses a research run may be kept with. */
	public static final Set<String> STATUSES = Set.of(COMPLETE, PARTIAL);

	/** The efforts a research run may be kept with. */
	public static final Set<String> EFFORTS = Set.of(QUICK, STANDARD, DEEP);

	/**
	 * One source of the run.
	 *
	 * <p>
	 * A web page refers to its archived copy ({@link #pageHandle()}), an object of the application
	 * to the object ({@link #object()}); {@link #title()} and {@link #url()} are what a source
	 * neither applies to is kept with, and are always given.
	 * </p>
	 *
	 * @param kind
	 *        The source kind of the reading list.
	 * @param reference
	 *        What the reading list knows the source by: its address, or the id of an object.
	 * @param title
	 *        Its title.
	 * @param url
	 *        The address of a web page; empty for any other source.
	 * @param pageHandle
	 *        The handle of the archived copy of a web page; empty for none.
	 * @param object
	 *        The object of the application the source is; {@code null} for none.
	 * @param readAt
	 *        When it was read; {@code null} when unknown.
	 * @param footnote
	 *        The number of its footnote; 0 for a source the report does not cite.
	 */
	public record Source(String kind, String reference, String title, String url, String pageHandle,
			TLObject object, Instant readAt, int footnote) {

		/** Whether the report cites this source. */
		public boolean isCited() {
			return footnote > 0;
		}
	}

	/**
	 * Composes what is kept of a research run.
	 *
	 * @param question
	 *        What the run answered.
	 * @param markdown
	 *        The report, with links to the sources it rests on.
	 * @param effort
	 *        One of {@link #EFFORTS}; may be {@code null}.
	 * @param plan
	 *        The sub-questions; may be {@code null}.
	 * @param status
	 *        One of {@link #STATUSES}; {@code null} for complete.
	 * @param read
	 *        The reading list of the run tree.
	 * @param objects
	 *        Resolves the id of a source that is no web page to the object, under the run's read
	 *        rights; throws for an id it cannot resolve.
	 * @param leadRunId
	 *        The run id of the lead run; {@code null} outside a run.
	 * @param now
	 *        The time the report is kept.
	 */
	public static ResearchReport compose(String question, String markdown, String effort, String plan, String status,
			ReadingList read, Function<String, TLObject> objects, String leadRunId, Instant now) {
		CitationValidator.Result validated = CitationValidator.validate(markdown, read, List.of());
		List<Source> cited = new ArrayList<>();
		Set<String> citedKeys = new HashSet<>();
		for (Citation citation : validated.citations()) {
			ReadSource source = read.find(citation.id());
			if (source == null) {
				continue;
			}
			cited.add(source(source, objects, citation.index()));
			citedKeys.add(key(source));
		}
		List<Source> alsoRead = new ArrayList<>();
		Instant started = now;
		for (ReadSource source : read.sources()) {
			if (source.readAt() != null && source.readAt().isBefore(started)) {
				started = source.readAt();
			}
			if (!citedKeys.contains(key(source))) {
				alsoRead.add(source(source, objects, 0));
			}
		}
		return new ResearchReport(question.strip(), lower(effort), nullToEmpty(plan).strip(),
			status == null || status.isBlank() ? COMPLETE : lower(status), markdown, cited, alsoRead,
			read.findings(), nullToEmpty(leadRunId), started, now);
	}

	private static Source source(ReadSource source, Function<String, TLObject> objects, int footnote) {
		String reference = reference(source);
		String title = source.title().isEmpty() ? reference : source.title();
		if (CitationKinds.WEB.equals(source.kind()) || !source.url().isEmpty()) {
			return new Source(source.kind(), reference, title, source.url(), PageArchive.handleOf(source.archiveUrl()),
				null, source.readAt(), footnote);
		}
		return new Source(source.kind(), reference, title, "", "", resolve(objects, source.id()), source.readAt(),
			footnote);
	}

	private static TLObject resolve(Function<String, TLObject> objects, String id) {
		if (objects == null || id.isEmpty()) {
			return null;
		}
		try {
			return objects.apply(id);
		} catch (RuntimeException ex) {
			// Not an object of this application, or not one the run may read now: the source is kept
			// with what the reading list says of it.
			Logger.debug("Research source " + id + " is kept without its object: " + ex.getMessage(),
				ResearchReport.class);
			return null;
		}
	}

	/** What the reading list knows a source by. */
	static String reference(ReadSource source) {
		return source.url().isEmpty() ? source.id() : source.url();
	}

	/** The key a source and a finding's source are matched by. */
	static String key(ReadSource source) {
		return key(reference(source));
	}

	/** The key a source and a finding's source are matched by. */
	static String key(String reference) {
		return reference == null ? "" : reference.strip().toLowerCase(Locale.ROOT);
	}

	private static String lower(String value) {
		return nullToEmpty(value).strip().toLowerCase(Locale.ROOT);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}
}
