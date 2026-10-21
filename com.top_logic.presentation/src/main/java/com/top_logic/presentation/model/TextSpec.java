/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

import java.util.List;

/**
 * A text in a shape, a text box, a placeholder or a table cell.
 *
 * @param paragraphs
 *        The paragraphs.
 * @param style
 *        The character formatting of all paragraphs, unless they define their own.
 * @param align
 *        The horizontal alignment of all paragraphs, unless they define their own,
 *        <code>null</code> if inherited.
 * @param anchor
 *        The vertical alignment in the shape, <code>null</code> if inherited.
 * @param insets
 *        The inner margins, <code>null</code> if inherited.
 * @param overflow
 *        The handling of a text that does not fit into its shape or table cell, <code>null</code>
 *        for {@link Overflow#SHRINK}. Placeholder texts are not fitted.
 * @param split
 *        Whether paragraphs that do not fit into the shape continue on further slides.
 * @param wrap
 *        Whether lines are wrapped at the shape border, <code>null</code> if inherited.
 */
public record TextSpec(List<ParagraphSpec> paragraphs, RunStyle style, TextAlign align, TextAnchor anchor,
		InsetsSpec insets, Overflow overflow, boolean split, Boolean wrap) {

	/**
	 * Creates a text consisting of the given paragraphs without further formatting.
	 */
	public static TextSpec of(List<ParagraphSpec> paragraphs) {
		return new TextSpec(paragraphs, RunStyle.INHERIT, null, null, null, null, false, null);
	}

	/**
	 * This text with other paragraphs.
	 */
	public TextSpec withParagraphs(List<ParagraphSpec> newParagraphs) {
		return new TextSpec(newParagraphs, style, align, anchor, insets, overflow, split, wrap);
	}

	/**
	 * This text with another character formatting.
	 */
	public TextSpec withStyle(RunStyle newStyle) {
		return new TextSpec(paragraphs, newStyle, align, anchor, insets, overflow, split, wrap);
	}

	/**
	 * This text with the given alignment, where it has none of its own.
	 */
	public TextSpec withDefaultAlignment(TextAlign defaultAlign, TextAnchor defaultAnchor) {
		return new TextSpec(paragraphs, style, align != null ? align : defaultAlign,
			anchor != null ? anchor : defaultAnchor, insets, overflow, split, wrap);
	}

}
