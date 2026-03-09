/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.Map;

import com.top_logic.layout.react.DefaultReactDisplayContext;
import com.top_logic.layout.react.ReactDisplayContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;

/**
 * Application shell that provides the standard page layout (header, content, footer) and a built-in
 * snackbar notification service.
 *
 * <p>
 * The shell renders a full-height flex column with the header at the top, the content filling the
 * remaining space, and the footer at the bottom. A singleton {@link ReactSnackbarControl} is
 * embedded and accessible to any code via {@link #showSnackbar(String, String)}.
 * </p>
 *
 * <p>
 * Dialogs, drawers, and menus are <b>not</b> part of the shell. They are managed by the content
 * components that need them and position themselves via CSS {@code position: fixed}.
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
 * </ul>
 */
public class ReactAppShellControl extends ReactControl {

	private static final String REACT_MODULE = "TLAppShell";

	private static final String HEADER = "header";

	private static final String CONTENT = "content";

	private static final String FOOTER = "footer";

	private static final String SNACKBAR = "snackbar";

	private final ReactControl _header;

	private final ReactControl _content;

	private final ReactControl _footer;

	private final ReactSnackbarControl _snackbar;

	private final ErrorSink _errorSink;

	/**
	 * Creates an application shell with all three slots.
	 *
	 * @param header
	 *        Optional header control (e.g. app bar), or {@code null}.
	 * @param content
	 *        The main content control (gets {@code flex:1}).
	 * @param footer
	 *        Optional footer control (e.g. bottom bar), or {@code null}.
	 */
	public ReactAppShellControl(ReactControl header, ReactControl content, ReactControl footer) {
		super(null, REACT_MODULE);
		_header = header;
		_content = content;
		_footer = footer;
		_snackbar = new ReactSnackbarControl("", "success", () -> { /* no-op, auto-hides */ });

		_errorSink = new ErrorSink() {
			@Override
			public void showError(String message) {
				showSnackbar(message, "error");
			}

			@Override
			public void showWarning(String message) {
				showSnackbar(message, "warning");
			}

			@Override
			public void showInfo(String message) {
				showSnackbar(message, "info");
			}
		};

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
	 * Shows a success snackbar notification.
	 *
	 * @param message
	 *        The notification message.
	 */
	public void showSnackbar(String message) {
		showSnackbar(message, "success");
	}

	/**
	 * Shows a snackbar notification.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        "info", "success", "warning", or "error".
	 */
	public void showSnackbar(String message, String variant) {
		_snackbar.patchReactState(Map.of("message", message, "variant", variant, "visible", Boolean.TRUE));
	}

	@Override
	protected void onBeforeWrite(ReactDisplayContext context) {
		if (context instanceof DefaultReactDisplayContext defaultContext) {
			defaultContext.setErrorSink(_errorSink);
		}
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
