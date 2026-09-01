/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A single system-wide notice displayed as a full-width bar, e.g. the announcement of a maintenance
 * window.
 *
 * <p>
 * Renders as a {@code TLNoticeBar} React component. While {@link #hide() hidden} the component
 * renders nothing at all, so a notice area holding only hidden notices occupies no space. This is
 * the same mechanism the {@code TLSnackbar} uses, and it replaces the old-world
 * {@link com.top_logic.layout.structure.ConditionalViewLayout}, which had to reserve the bar's
 * height in the surrounding layout.
 * </p>
 *
 * <p>
 * A notice may carry a deadline: the moment the announced event occurs, passed to
 * {@link #show(Severity, String, Long)}. The client then displays a count-down that ticks once per
 * second, without asking the server. Reaching zero does not change what the notice says - the
 * count-down stops at {@code 0:00} and waits for the server to push the new text, so the client
 * never states a system state of its own.
 * </p>
 *
 * <p>
 * Alongside the deadline the server's own clock reading is sent, letting the client measure its
 * offset and count down against server time rather than the browser's, which may be off by
 * minutes. The old-world {@code MaintenanceControl} does the same by passing
 * {@link System#currentTimeMillis()} into its {@code initTimer} call.
 * </p>
 *
 * @see com.top_logic.layout.react.control.overlay.ReactSnackbarControl
 */
public class ReactNoticeBarControl extends ReactControl {

	private static final String REACT_MODULE = "TLNoticeBar";

	private static final String VISIBLE = "visible";

	private static final String SEVERITY = "severity";

	private static final String TEXT = "text";

	private static final String DEADLINE = "deadline";

	private static final String SERVER_NOW = "serverNow";

	/**
	 * How urgent a notice is, selecting its visual appearance.
	 */
	public enum Severity implements ExternallyNamed {

		/** Neutral information. */
		INFO("info"),

		/** Something the user should act upon, e.g. an announced maintenance window. */
		WARNING("warning"),

		/** Something that already restricts the user, e.g. an active maintenance window. */
		ERROR("error");

		private final String _externalName;

		Severity(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Creates a hidden {@link ReactNoticeBarControl}.
	 *
	 * <p>
	 * The notice becomes visible only through {@link #show(Severity, String, Long)}, so an element
	 * that has nothing to announce needs no special case.
	 * </p>
	 */
	public ReactNoticeBarControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		putState(VISIBLE, false);
		putState(SEVERITY, Severity.INFO.getExternalName());
		putState(TEXT, "");
		putState(DEADLINE, null);
		putState(SERVER_NOW, null);
	}

	/**
	 * Shows the notice with the given appearance and content.
	 *
	 * <p>
	 * All properties are sent as a single patch, so the client never renders an intermediate state
	 * mixing the old text with the new deadline.
	 * </p>
	 *
	 * @param severity
	 *        How urgent the notice is.
	 * @param text
	 *        The message to display.
	 * @param deadline
	 *        The time the announced event occurs, as epoch milliseconds, or {@code null} for a
	 *        notice without a count-down.
	 */
	public void show(Severity severity, String text, Long deadline) {
		Object tx = beginUpdate();
		putState(SEVERITY, severity.getExternalName());
		putState(TEXT, text != null ? text : "");
		putState(DEADLINE, deadline);
		// Reference point for the client's clock-offset correction, taken together with the
		// deadline so that both describe the same instant.
		putState(SERVER_NOW, deadline == null ? null : Long.valueOf(System.currentTimeMillis()));
		putState(VISIBLE, true);
		commitUpdate(tx);
	}

	/**
	 * Hides the notice, so that it occupies no space.
	 */
	public void hide() {
		putState(VISIBLE, false);
	}
}
