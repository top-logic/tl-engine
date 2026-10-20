/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import java.util.List;

/**
 * A way an answer points at its sources in the text.
 *
 * <p>
 * The framework's own syntax is the link, {@link LinkMarkers}: {@code [Sommerfest](tl:GenericObject:11047)},
 * {@code [laut Tagesschau](https://...)}. An application whose agents cite another way - a record
 * code in parentheses, {@code (FACT-001, DEC-002)} - registers a syntax of its own in
 * the marker syntaxes of {@link CitationKinds}, and the validator treats its markers exactly as
 * it treats links: a reference that names a source of the run's reading list becomes a footnote, any
 * other reference is dropped.
 * </p>
 *
 * <p>
 * A syntax only finds markers; it decides nothing about them. Which reference counts is the reading
 * list's answer, so an application's syntax cannot let through a source no tool read. Its tools
 * register what they read with {@code ToolExecutionContext.readSource}, under the same reference its
 * markers name.
 * </p>
 *
 * <p>
 * Implementations are instantiated from configuration and must be stateless and thread-safe.
 * </p>
 */
public interface CitationMarkerSyntax {

	/**
	 * The markers of this syntax in {@code text}, in order of their position.
	 *
	 * <p>
	 * The caller skips markers that start inside a code span or a fenced block, and markers that
	 * overlap one an earlier syntax already found, so an implementation need not know about either.
	 * </p>
	 *
	 * @param text
	 *        One text of the answer, as the model wrote it. Never <code>null</code>.
	 * @return The markers found; never <code>null</code>.
	 */
	List<Marker> find(String text);

	/**
	 * One place in the answer that points at sources.
	 *
	 * @param start
	 *        Index of the first character of the marker in the text.
	 * @param end
	 *        Index after its last character.
	 * @param keep
	 *        What stays in the text in place of the marker: a link's text, or nothing for a marker
	 *        that is only a reference. The footnotes of the references that hold are written after
	 *        it; when none holds, it stands alone.
	 * @param references
	 *        The sources the marker names, as written - an object identifier, a URL, a record code.
	 * @param passage
	 *        The sentence of the source the marker says the statement rests on, or empty. Kept only
	 *        when what the tool read contains it.
	 */
	record Marker(int start, int end, String keep, List<String> references, String passage) {

		/** Empty rather than <code>null</code> for the optional parts. */
		public Marker {
			keep = keep == null ? "" : keep;
			references = references == null ? List.of() : List.copyOf(references);
			passage = passage == null ? "" : passage;
		}
	}
}
