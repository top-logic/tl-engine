/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
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
 * <p>
 * A notice may also be restricted to the last moments before its deadline, see
 * {@link #setLeadMillis(Long)}, it may offer the user something to do about what it announces, see
 * {@link #setAction(String, Runnable)}, and it may have the client ask what happened once the
 * deadline has passed, see {@link #setDeadlinePing(Long, Runnable)} - the count-down otherwise
 * stands at {@code 0:00} until something else makes the client talk to the server.
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

	private static final String LEAD_MS = "leadMs";

	private static final String ACTION_LABEL = "actionLabel";

	private static final String PING_GRACE_MS = "pingGraceMs";

	/** The {@link ReactCommandHandler} that runs the notice's {@link #setAction(String, Runnable) action}. */
	public static final String ACTION_COMMAND = "action";

	/**
	 * The {@link ReactCommandHandler} the client calls once its count-down has passed the deadline.
	 *
	 * @see #setDeadlinePing(Long, Runnable)
	 */
	public static final String DEADLINE_PASSED_COMMAND = "deadlinePassed";

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

	private Runnable _actionHandler;

	private Runnable _deadlinePassedHandler;

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
		putState(LEAD_MS, null);
		putState(ACTION_LABEL, null);
		putState(PING_GRACE_MS, null);
	}

	/**
	 * Restricts the notice to the last moments before its deadline.
	 *
	 * <p>
	 * A notice with a lead time stays invisible while more than that remains, even though it is
	 * {@link #show(Severity, String, Long) shown}, and appears when the remaining time falls below
	 * it. The client decides this, because the moment it becomes true passes without the server
	 * being asked anything: the session timeout, for instance, has a deadline that is known from
	 * the start but must be announced only shortly before it.
	 * </p>
	 *
	 * @param leadMillis
	 *        How long before the deadline the notice appears, or {@code null} for a notice that is
	 *        visible as soon as it is shown. Ignored by a notice without a deadline, which has no
	 *        remaining time to compare against.
	 */
	public void setLeadMillis(Long leadMillis) {
		putState(LEAD_MS, leadMillis);
	}

	/**
	 * Offers the user something to do about what the notice announces.
	 *
	 * <p>
	 * The whole bar becomes the button: there is one thing to do about a notice, and the notice is
	 * what the user is looking at. The label says what that is and is displayed as the bar's
	 * tooltip.
	 * </p>
	 *
	 * @param label
	 *        What clicking the notice does, or {@code null} to leave it inert.
	 * @param handler
	 *        Runs when the user clicks the notice.
	 */
	public void setAction(String label, Runnable handler) {
		_actionHandler = handler;
		putState(ACTION_LABEL, label);
	}

	/**
	 * Makes the client report back once the deadline has passed, so that the server states what
	 * happened instead of leaving a count-down standing at {@code 0:00}.
	 *
	 * <p>
	 * The client sends one request per deadline, and only after the deadline has passed by the
	 * given grace period. The answer is the server's: either the state that has meanwhile been
	 * reached - for a session that the container discarded, the reload every window of an ended
	 * session receives - or a new deadline, in which case the count-down simply starts over.
	 * </p>
	 *
	 * @param graceMillis
	 *        How long after the deadline the client asks, or {@code null} for a notice that never
	 *        asks. It has to cover how imprecisely the caller's deadline is known to the client, so
	 *        that the request cannot arrive while the announced event is still ahead - a request
	 *        that reaches a session shortly before its timeout would renew it for a full interval.
	 * @param handler
	 *        Runs when the client reports the deadline as passed <em>and</em> the server is still
	 *        in a state to run it, which is exactly the case where the announced event has not
	 *        happened after all.
	 */
	public void setDeadlinePing(Long graceMillis, Runnable handler) {
		_deadlinePassedHandler = handler;
		putState(PING_GRACE_MS, graceMillis);
	}

	/**
	 * Runs the {@link #setAction(String, Runnable) action} of a notice the user clicked.
	 *
	 * @implNote Flagged technical: extending a session or dismissing an announcement is a gesture
	 *           about the chrome, and replaying it in a scripted test would tie the test to the
	 *           wall clock rather than to anything the application does.
	 */
	@ReactCommandHandler(value = ACTION_COMMAND, technical = true)
	void handleAction() {
		if (_actionHandler != null) {
			_actionHandler.run();
		}
	}

	/**
	 * Answers the client's report that the deadline has passed.
	 *
	 * @implNote Flagged technical for the same reason as {@link #handleAction()}: it is the
	 *           client's bookkeeping, not a gesture of the user, and replaying it in a scripted
	 *           test would tie the test to the wall clock.
	 */
	@ReactCommandHandler(value = DEADLINE_PASSED_COMMAND, technical = true)
	void handleDeadlinePassed() {
		if (_deadlinePassedHandler != null) {
			_deadlinePassedHandler.run();
		}
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
