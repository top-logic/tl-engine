/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

/**
 * Content returned by a {@link TooltipProvider}.
 *
 * <p>
 * {@link #getHtml()} is sanitized tooltip HTML (see {@code SafeHTML}). {@link #getCaption()} is an
 * optional plain-text header shown above the content (may be {@code null}).
 * </p>
 */
public final class TooltipContent {

	private final String _html;

	private final String _caption;

	/**
	 * Creates content with the given HTML body and optional caption.
	 */
	public TooltipContent(String html, String caption) {
		_html = html;
		_caption = caption;
	}

	/**
	 * Sanitized HTML body.
	 */
	public String getHtml() {
		return _html;
	}

	/**
	 * Optional caption, or {@code null}.
	 */
	public String getCaption() {
		return _caption;
	}
}
