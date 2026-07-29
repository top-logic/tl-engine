/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import com.top_logic.basic.Logger;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.responsive.DisplayClass;
import com.top_logic.layout.responsive.DisplayClassModel;

/**
 * Application shell that provides the standard page layout (header, content, footer) and a built-in
 * snackbar notification service.
 *
 * <p>
 * The shell renders a full-height flex column with the header at the top, the content filling the
 * remaining space, and the footer at the bottom. A singleton {@link ReactSnackbarControl} is
 * embedded and accessible to any code via {@link #showSnackbar(String, Variant)}.
 * </p>
 *
 * <p>
 * Dialogs, drawers, and menus are <b>not</b> part of the shell: they overlay the whole browser window
 * and are therefore mounted by it, see {@code ViewServlet}. Any view reaches them through its
 * context, whether or not it embeds a shell.
 * </p>
 *
 * <p>
 * State:
 * </p>
 * <ul>
 * <li>{@code header} - optional header slot control (e.g. an app bar)</li>
 * <li>{@code content} - the main content control (gets {@code flex:1})</li>
 * <li>{@code footer} - optional footer slot control (e.g. a bottom bar)</li>
 * <li>{@code snackbar} - built-in snackbar child descriptor (managed internally)</li>
 * <li>{@code dialogManager} - built-in dialog manager child descriptor (managed internally)</li>
 * </ul>
 */
public class ReactAppShellControl extends ReactControl {

	private static final String REACT_MODULE = "TLAppShell";

	private static final String HEADER = "header";

	private static final String CONTENT = "content";

	private static final String FOOTER = "footer";

	private static final String SNACKBAR = "snackbar";



	/** The {@link ReactCommandHandler} that records the client's responsive display class. */
	public static final String REPORT_DISPLAY_CLASS_COMMAND = "reportDisplayClass";

	private final ReactControl _header;

	private final ReactControl _content;

	private final ReactControl _footer;

	private final ReactSnackbarControl _snackbar;

	private final ErrorSink _errorSink;

	/**
	 * Creates an application shell with all three slots.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param header
	 *        Optional header control (e.g. app bar), or {@code null}.
	 * @param content
	 *        The main content control (gets {@code flex:1}).
	 * @param footer
	 *        Optional footer control (e.g. bottom bar), or {@code null}.
	 * @param snackbar
	 *        The snackbar control for notifications.
	 * @param errorSink
	 *        The error sink that routes messages to the snackbar.
	 */
	public ReactAppShellControl(ReactContext context, ReactControl header, ReactControl content, ReactControl footer,
			ReactSnackbarControl snackbar, ErrorSink errorSink) {
		super(context, null, REACT_MODULE);
		_header = header;
		_content = content;
		_footer = footer;
		_snackbar = snackbar;
		_errorSink = errorSink;

		if (header != null) {
			putState(HEADER, header);
		}
		putState(CONTENT, content);
		if (footer != null) {
			putState(FOOTER, footer);
		}
		putState(SNACKBAR, _snackbar);
	}

	/**
	 * The {@link ErrorSink} that routes messages to this shell's snackbar.
	 */
	public ErrorSink getErrorSink() {
		return _errorSink;
	}

	/**
	 * Shows a success snackbar notification.
	 *
	 * @param message
	 *        The notification message.
	 */
	public void showSnackbar(String message) {
		showSnackbar(message, ReactSnackbarControl.Variant.SUCCESS);
	}

	/**
	 * Shows a snackbar notification with HTML content.
	 *
	 * @param htmlContent
	 *        The notification content as rendered HTML.
	 * @param variant
	 *        The visual variant.
	 */
	public void showSnackbar(String htmlContent, ReactSnackbarControl.Variant variant) {
		_snackbar.showHtml(htmlContent, variant);
	}

	/**
	 * Records the {@link DisplayClass} reported by the client for the current browser tab.
	 *
	 * <p>
	 * Fired by the shell's React component whenever the viewport crosses the responsive breakpoint
	 * (and once on mount). The value is stored on the subsession's {@link DisplayClassModel}, from
	 * where adaptive controls observe it.
	 * </p>
	 *
	 * @param args
	 *        The reported {@link DisplayClass} name.
	 */
	@ReactCommandHandler(value = REPORT_DISPLAY_CLASS_COMMAND, technical = true)
	void handleReportDisplayClass(ReportDisplayClassArguments args) {
		String reported = args.getDisplayClass();
		DisplayClass displayClass = DisplayClass.DEFAULT;
		if (reported != null) {
			try {
				displayClass = DisplayClass.valueOf(reported);
			} catch (IllegalArgumentException ex) {
				Logger.warn("Ignoring unknown display class '" + reported + "'.", ReactAppShellControl.class);
			}
		}
		DisplayClassModel.forCurrentSubSession().setDisplayClass(displayClass);
	}

	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		if (_header != null) {
			_header.attach();
		}
		_content.attach();
		if (_footer != null) {
			_footer.attach();
		}
		_snackbar.attach();
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_header != null) {
			_header.detach();
		}
		_content.detach();
		if (_footer != null) {
			_footer.detach();
		}
		_snackbar.detach();
	}

	@Override
	protected void cleanupChildren() {
		if (_header != null) {
			_header.cleanupTree();
		}
		_content.cleanupTree();
		if (_footer != null) {
			_footer.cleanupTree();
		}
		_snackbar.cleanupTree();
	}

}
