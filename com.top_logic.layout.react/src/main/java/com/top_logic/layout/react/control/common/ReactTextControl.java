/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.TooltipContent;
import com.top_logic.layout.react.TooltipProvider;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A simple read-only control that displays a text value as a {@code <span>}.
 *
 * <p>
 * Renders as a {@code TLText} React component. An optional CSS class can be appended to the
 * default {@code tlText} class for custom styling.
 * </p>
 *
 * <p>
 * The caller is responsible for converting application objects to display strings, e.g. by using a
 * {@link com.top_logic.layout.LabelProvider}.
 * </p>
 */
public class ReactTextControl extends ReactControl implements TooltipProvider {

	private static final String TEXT = "text";

	private static final String CSS_CLASS = "cssClass";

	private static final String HAS_TOOLTIP = "hasTooltip";

	/** Key expected by {@link #getTooltipContent(String)}. */
	private static final String TOOLTIP_KEY = "tooltip";

	private String _tooltipHtml;

	private String _tooltipCaption;

	private boolean _tooltipInteractive;

	/**
	 * Creates a {@link ReactTextControl} without an extra CSS class.
	 */
	public ReactTextControl(ReactContext context, String text) {
		this(context, text, null);
	}

	/**
	 * Creates a {@link ReactTextControl}.
	 *
	 * @param text
	 *        The text to display, or {@code null}.
	 * @param cssClass
	 *        Additional CSS class to append to the default {@code tlText} class, or {@code null}.
	 */
	public ReactTextControl(ReactContext context, String text, String cssClass) {
		super(context, null, "TLText");
		putState(TEXT, text != null ? text : "");
		if (cssClass != null) {
			putState(CSS_CLASS, cssClass);
		}
	}

	/**
	 * Updates the displayed text.
	 */
	public void setText(String text) {
		putState(TEXT, text != null ? text : "");
	}

	/**
	 * Updates the additional CSS class.
	 *
	 * @param cssClass
	 *        Additional CSS class to append, or {@code null} to clear.
	 */
	public void setCssClass(String cssClass) {
		putState(CSS_CLASS, cssClass != null ? cssClass : "");
	}

	/**
	 * Sets a rich tooltip shown on hover.
	 *
	 * @param html
	 *        Sanitized tooltip HTML, or {@code null} to clear.
	 * @param caption
	 *        Optional caption, or {@code null}.
	 * @param interactive
	 *        When {@code true}, the popover remains open while the pointer hovers over it so the
	 *        user can select and copy content.
	 */
	public void setTooltip(String html, String caption, boolean interactive) {
		_tooltipHtml = (html == null || html.isEmpty()) ? null : html;
		_tooltipCaption = caption;
		_tooltipInteractive = interactive;
		putState(HAS_TOOLTIP, _tooltipHtml != null);
	}

	@Override
	public TooltipContent getTooltipContent(String key) {
		if (!TOOLTIP_KEY.equals(key) || _tooltipHtml == null) {
			return null;
		}
		return new TooltipContent(_tooltipHtml, _tooltipCaption, _tooltipInteractive);
	}
}
