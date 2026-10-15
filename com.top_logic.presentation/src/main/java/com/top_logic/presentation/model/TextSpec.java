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
 * @param autofit
 *        How the text is fitted into its shape, <code>null</code> if inherited.
 * @param wrap
 *        Whether lines are wrapped at the shape border, <code>null</code> if inherited.
 */
public record TextSpec(List<ParagraphSpec> paragraphs, RunStyle style, TextAlign align, TextAnchor anchor,
		InsetsSpec insets, Autofit autofit, Boolean wrap) {

	/**
	 * Creates a text consisting of the given paragraphs without further formatting.
	 */
	public static TextSpec of(List<ParagraphSpec> paragraphs) {
		return new TextSpec(paragraphs, RunStyle.INHERIT, null, null, null, null, null);
	}

}
