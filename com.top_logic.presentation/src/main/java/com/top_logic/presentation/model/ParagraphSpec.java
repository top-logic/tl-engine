/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

/**
 * A paragraph of a text in a presentation description.
 *
 * @param runs
 *        The parts of the paragraph.
 * @param style
 *        The character formatting of all runs, unless they define their own.
 * @param align
 *        The horizontal alignment, <code>null</code> if inherited.
 * @param level
 *        The indentation level from 0 to 8, <code>null</code> for the first level.
 * @param bullet
 *        The bullet, <code>null</code> if inherited.
 * @param spaceBefore
 *        The space before the paragraph in points, <code>null</code> if inherited.
 * @param spaceAfter
 *        The space after the paragraph in points, <code>null</code> if inherited.
 * @param lineSpacing
 *        The line spacing as multiple of the single line height, <code>null</code> if inherited.
 * @param keepWithNext
 *        Whether the paragraph stays on the slide of the following paragraph or element when its
 *        text, or its split column, continues on a further slide, e.g. a heading.
 */
public record ParagraphSpec(List<RunSpec> runs, RunStyle style, TextAlign align, Integer level, BulletSpec bullet,
		Double spaceBefore, Double spaceAfter, Double lineSpacing, boolean keepWithNext) {

	/**
	 * Creates a paragraph that is not kept with the next one.
	 */
	public ParagraphSpec(List<RunSpec> runs, RunStyle style, TextAlign align, Integer level, BulletSpec bullet,
			Double spaceBefore, Double spaceAfter, Double lineSpacing) {
		this(runs, style, align, level, bullet, spaceBefore, spaceAfter, lineSpacing, false);
	}

	/**
	 * The highest {@link #level()}.
	 */
	public static final int MAX_LEVEL = 8;

	/**
	 * Creates a paragraph consisting of the given unformatted text.
	 */
	public static ParagraphSpec plain(String text) {
		return of(List.of(RunSpec.plain(text)));
	}

	/**
	 * Creates a paragraph of the given runs without paragraph formatting.
	 */
	public static ParagraphSpec of(List<RunSpec> runs) {
		return new ParagraphSpec(runs, RunStyle.INHERIT, null, null, null, null, null, null);
	}

}
