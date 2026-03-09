/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
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
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param header
	 *        Optional header control (e.g. app bar), or {@code null}.
	 * @param content
	 *        The main content control (gets {@code flex:1}).
	 * @param footer
	 *        Optional footer control (e.g. bottom bar), or {@code null}.
	 */
	public ReactAppShellControl(ReactContext context, ReactControl header, ReactControl content, ReactControl footer) {
		super(context, null, REACT_MODULE);
		_header = header;
		_content = content;
		_footer = footer;
		_snackbar = new ReactSnackbarControl(context, "", "success", () -> { /* no-op, auto-hides */ });

		_errorSink = new ErrorSink() {
			@Override
			public void showError(HTMLFragment content) {
				showSnackbar(renderToHtml(content), "error");
			}

			@Override
			public void showWarning(HTMLFragment content) {
				showSnackbar(renderToHtml(content), "warning");
			}

			@Override
			public void showInfo(HTMLFragment content) {
				showSnackbar(renderToHtml(content), "info");
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
		showSnackbar(message, "success");
	}

	/**
	 * Shows a snackbar notification with HTML content.
	 *
	 * @param htmlContent
	 *        The notification content as rendered HTML.
	 * @param variant
	 *        "info", "success", "warning", or "error".
	 */
	public void showSnackbar(String htmlContent, String variant) {
		_snackbar.patchReactState(Map.of("content", htmlContent, "variant", variant, "visible", Boolean.TRUE));
	}

	private String renderToHtml(HTMLFragment fragment) {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();
		StringWriter buffer = new StringWriter();
		try {
			TagWriter out = new TagWriter(buffer);
			fragment.write(displayContext, out);
		} catch (IOException ex) {
			Logger.error("Failed to render info service message.", ex, ReactAppShellControl.class);
		}
		return buffer.toString();
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
