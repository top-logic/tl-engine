/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.html;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A read-only control that displays an HTML fragment.
 *
 * <p>
 * Renders as a {@code TLHtml} React component. The control shows either the HTML fragment given to
 * {@link #setHtml(String)} or the message given to {@link #setError(String)} - whichever was set
 * last; setting one clears the other.
 * </p>
 *
 * <p>
 * The fragment reaches the browser as it stands. Whether its content may be shown is decided by the
 * caller: the fragment passed to {@link #setHtml(String)} is the one that is published, and a
 * fragment the caller rejects is reported through {@link #setError(String)} instead.
 * </p>
 *
 * <p>
 * The {@link #DISPLAY display mode} chosen at construction time decides how the fragment is shown -
 * {@link #DISPLAY_INLINE} inserts it into the page around it.
 * </p>
 */
public class ReactHtmlControl extends ReactControl {

	/** Name of the React component this control renders as. */
	private static final String COMPONENT = "TLHtml";

	/** State key holding the display mode, one of the {@code DISPLAY_}-prefixed constants. */
	public static final String DISPLAY = "display";

	/** State key holding the HTML fragment to display. */
	public static final String HTML = "html";

	/** State key holding the message shown instead of a fragment that cannot be displayed. */
	public static final String ERROR = "error";

	/** State key holding the additional CSS class of the rendered element. */
	public static final String CSS_CLASS = "cssClass";

	/** {@link #DISPLAY} mode inserting the fragment into the page around it. */
	public static final String DISPLAY_INLINE = "inline";

	/** {@link #DISPLAY} mode showing the fragment as a page of its own. */
	public static final String DISPLAY_DOCUMENT = "document";

	/** {@link #DISPLAY} mode showing a scaled-down preview of the fragment. */
	public static final String DISPLAY_THUMBNAIL = "thumbnail";

	/**
	 * Creates a {@link ReactHtmlControl} showing nothing.
	 *
	 * @param display
	 *        The display mode, one of the {@code DISPLAY_}-prefixed constants.
	 * @param cssClass
	 *        Additional CSS class to append to the default {@code tlHtml} class, or {@code null}.
	 */
	public ReactHtmlControl(ReactContext context, String display, String cssClass) {
		super(context, null, COMPONENT);
		putState(DISPLAY, display);
		putState(HTML, "");
		putState(ERROR, null);
		if (cssClass != null) {
			putState(CSS_CLASS, cssClass);
		}
	}

	/**
	 * Displays the given HTML fragment and clears the message set before.
	 *
	 * @param html
	 *        The fragment to display, or {@code null} to display nothing.
	 */
	public void setHtml(String html) {
		Object tx = beginUpdate();
		putState(HTML, html != null ? html : "");
		putState(ERROR, null);
		commitUpdate(tx);
	}

	/**
	 * Displays the given message in place of a fragment and clears the fragment set before.
	 *
	 * @param message
	 *        The message to display, or {@code null} to display nothing.
	 */
	public void setError(String message) {
		Object tx = beginUpdate();
		putState(HTML, "");
		putState(ERROR, message);
		commitUpdate(tx);
	}

}
