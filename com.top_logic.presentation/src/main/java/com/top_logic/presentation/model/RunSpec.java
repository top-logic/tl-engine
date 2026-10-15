/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * A part of a paragraph with uniform character formatting.
 *
 * @param text
 *        The text. A line break in the text breaks the line without starting a new paragraph.
 * @param style
 *        The character formatting.
 * @param hyperlink
 *        The URL the text links to, <code>null</code> for no link.
 */
public record RunSpec(String text, RunStyle style, String hyperlink) {

	/**
	 * Creates a run without formatting and link.
	 */
	public static RunSpec plain(String text) {
		return new RunSpec(text, RunStyle.INHERIT, null);
	}

}
