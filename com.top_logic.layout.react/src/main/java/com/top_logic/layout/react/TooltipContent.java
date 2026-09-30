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

	private final boolean _interactive;

	/**
	 * Creates a passive tooltip (closes when the pointer leaves the anchor; not focusable).
	 */
	public TooltipContent(String html, String caption) {
		this(html, caption, false);
	}

	/**
	 * Creates tooltip content.
	 *
	 * @param html
	 *        Sanitized HTML body.
	 * @param caption
	 *        Optional caption shown above the body.
	 * @param interactive
	 *        When {@code true}, the popover stays open while the pointer hovers over it,
	 *        enabling text selection, copy and link navigation (use for help pages with code
	 *        snippets). When {@code false}, it disappears as soon as the pointer leaves the
	 *        anchor (use for short labels).
	 */
	public TooltipContent(String html, String caption, boolean interactive) {
		_html = html;
		_caption = caption;
		_interactive = interactive;
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

	/**
	 * Whether the popover should remain open while the pointer is over it.
	 */
	public boolean isInteractive() {
		return _interactive;
	}
}
