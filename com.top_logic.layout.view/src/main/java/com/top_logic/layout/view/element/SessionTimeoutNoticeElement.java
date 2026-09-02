/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl.Severity;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.ReactWindowRegistry.ActivityListener;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * Warns the user that the session is about to end.
 *
 * <p>
 * Placed in the {@code <notices>} area of an {@code <app-shell>}, the element counts down to the
 * moment the session times out and stays out of the way until the remaining time falls below
 * {@link Config#getLeadSeconds()}.
 * </p>
 *
 * <p>
 * Clicking the notice continues the session: the request carrying the click restarts the inactivity
 * timeout like any other, so the count-down starts over.
 * </p>
 *
 * <p>
 * What ends the session is left to the container - a browser whose timers were suspended, in a
 * laptop that was closed, must not decide that a session is over just because its count-down
 * reached zero. The container, however, discards a timed-out session only once a request arrives:
 * while a window holds its SSE connection open, nothing scavenges it, so nobody would tell the
 * browser that its session has ended. The client therefore asks, a grace period after the
 * count-down passed zero, and the answer is the server's - the reload every window of an ended
 * session receives, or a new deadline.
 * </p>
 */
public class SessionTimeoutNoticeElement implements UIElement {

	/**
	 * Configuration for {@link SessionTimeoutNoticeElement}.
	 */
	@TagName("session-timeout-notice")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getLeadSeconds()}. */
		String LEAD_SECONDS = "lead-seconds";

		@Override
		@ClassDefault(SessionTimeoutNoticeElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * How long before the end of the session the warning appears, in seconds.
		 */
		@Name(LEAD_SECONDS)
		@Label("Advance warning time (seconds)")
		@IntDefault(300)
		int getLeadSeconds();

	}

	/**
	 * How far the deadline must move before the client is told about it again.
	 *
	 * <p>
	 * Every request restarts the session's timeout, and a user typing in a form produces a burst of
	 * them. Rounding the deadline to this resolution keeps that burst from turning into a patch per
	 * keystroke, at the price of a count-down that may be up to this much short of the truth - the
	 * warning appears slightly early and the session lives slightly longer than announced, which is
	 * the harmless direction.
	 * </p>
	 */
	private static final long PUSH_RESOLUTION_MILLIS = 10_000;

	/**
	 * How long after the announced end of the session the client asks the server what happened.
	 *
	 * <p>
	 * Derived from {@link #PUSH_RESOLUTION_MILLIS}, and deliberately larger: that resolution is the
	 * amount by which the client's deadline may lag behind the truth, and a request arriving while
	 * the session is still valid would restart the very timeout the notice counts down. The extra
	 * seconds cover the transport of the asking request itself.
	 * </p>
	 */
	private static final long PING_GRACE_MILLIS = PUSH_RESOLUTION_MILLIS + 5_000;

	private final long _leadMillis;

	/**
	 * Creates a new {@link SessionTimeoutNoticeElement} from configuration.
	 */
	@CalledByReflection
	public SessionTimeoutNoticeElement(InstantiationContext context, Config config) {
		_leadMillis = config.getLeadSeconds() * 1000L;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		ReactNoticeBarControl control = new ReactNoticeBarControl(context);
		control.setLeadMillis(Long.valueOf(_leadMillis));

		// Bind the viewing session's language now: the listener below runs in the thread of whatever
		// request restarts the timeout, which may belong to another window of this session.
		Resources resources = Resources.getInstance();
		control.setAction(resources.getString(I18NConstants.SESSION_TIMEOUT_EXTEND), () -> {
			// Nothing to do: the request carrying this command has restarted the session's timeout
			// before the command was dispatched, and announced that to this control like any other
			// request does.
		});

		control.setDeadlinePing(Long.valueOf(PING_GRACE_MILLIS), () -> {
			// Reached only if the session is alive after all - the container did not discard it,
			// because it counts from an access this control had not published yet. The asking
			// request has restarted the timeout, and the deadline it produced is already on its
			// way to the client, which starts the count-down over.
		});

		Countdown countdown = new Countdown(control, resources.getString(I18NConstants.SESSION_TIMEOUT_NOTICE));

		HttpSession session = currentSession();
		if (session != null) {
			// The page is being rendered, which restarted the timeout just as any other request
			// does - but before this control existed to be told about it.
			countdown.update(System.currentTimeMillis(), session.getMaxInactiveInterval());

			ReactWindowRegistry registry = ReactWindowRegistry.forSession(session);
			registry.addActivityListener(countdown);
			control.addCleanupAction(() -> registry.removeActivityListener(countdown));
		}

		return control;
	}

	/**
	 * The session of the request currently being handled, or {@code null} if the control is built
	 * outside a request - a headless consumer, a test.
	 */
	private static HttpSession currentSession() {
		InteractionContext interaction = ThreadContextManager.getInteraction();
		if (interaction == null) {
			return null;
		}
		HttpServletRequest request = interaction.asRequest();
		return request == null ? null : request.getSession(false);
	}

	/**
	 * The moment a session accessed at the given time times out, or {@code null} if it never does.
	 *
	 * @param accessTime
	 *        When the session was last accessed, as epoch milliseconds.
	 * @param maxInactiveSeconds
	 *        How long the session survives without a request, see
	 *        {@link HttpSession#getMaxInactiveInterval()}. A value {@code <= 0} means that it never
	 *        times out.
	 */
	public static Long deadline(long accessTime, int maxInactiveSeconds) {
		return maxInactiveSeconds > 0 ? Long.valueOf(accessTime + maxInactiveSeconds * 1000L) : null;
	}

	/**
	 * How long after the announced end of the session the client asks the server what happened.
	 *
	 * @see #PING_GRACE_MILLIS
	 */
	public static long pingGraceMillis() {
		return PING_GRACE_MILLIS;
	}

	/**
	 * Whether a deadline is far enough from the one the client already knows to be sent.
	 *
	 * @param knownDeadline
	 *        The deadline last sent to the client, or {@code 0} if it knows none yet.
	 * @param deadline
	 *        The deadline computed now.
	 *
	 * @see #PUSH_RESOLUTION_MILLIS
	 */
	public static boolean worthSending(long knownDeadline, long deadline) {
		return Math.abs(deadline - knownDeadline) >= PUSH_RESOLUTION_MILLIS;
	}

	/**
	 * Keeps one notice counting down to the end of the session.
	 *
	 * @implNote One instance per control, since it remembers what that control's client knows. Its
	 *           {@link #handleActivity(long, int)} runs in the thread of whichever request
	 *           restarted the timeout - a request of another window of the same session included -
	 *           so the remembered deadline is written from several threads. A lost update merely
	 *           sends one patch more than necessary.
	 */
	private static final class Countdown implements ActivityListener {

		private final ReactNoticeBarControl _control;

		private final String _text;

		private volatile long _knownDeadline;

		Countdown(ReactNoticeBarControl control, String text) {
			_control = control;
			_text = text;
		}

		@Override
		public void handleActivity(long accessTime, int maxInactiveSeconds) {
			update(accessTime, maxInactiveSeconds);
		}

		/**
		 * Sends the deadline resulting from the given access, unless the client is close enough
		 * already.
		 */
		void update(long accessTime, int maxInactiveSeconds) {
			Long deadline = deadline(accessTime, maxInactiveSeconds);
			if (deadline == null) {
				// The session never times out, so there is nothing to warn about.
				if (_knownDeadline != 0) {
					_control.hide();
					_knownDeadline = 0;
				}
				return;
			}
			if (!worthSending(_knownDeadline, deadline.longValue())) {
				return;
			}
			_knownDeadline = deadline.longValue();
			// Shown, but held back by the lead time until the session really is about to end.
			_control.show(Severity.WARNING, _text, deadline);
		}

	}

}
