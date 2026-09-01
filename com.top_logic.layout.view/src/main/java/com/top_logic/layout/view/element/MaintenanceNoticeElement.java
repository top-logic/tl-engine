/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.administration.MaintenanceWindowManager.MaintenanceStateListener;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl.Severity;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * Announces the application's maintenance window to the user.
 *
 * <p>
 * Placed in the {@code <notices>} area of an {@code <app-shell>}, the element shows a bar while a
 * maintenance window is announced or active, and nothing at all during normal operation. It
 * therefore is the new-world counterpart of the old-world {@code MaintenanceViewConfiguration},
 * which contributes a {@code MaintenanceControl} to the classic UI's message area.
 * </p>
 *
 * <p>
 * The element registers a {@link MaintenanceStateListener} on the {@link MaintenanceWindowManager},
 * so a user who is already logged in sees an announcement the moment an administrator makes it,
 * without reloading. Because the manager is fed from the {@code ClusterManager}, this works across
 * cluster nodes as well.
 * </p>
 */
public class MaintenanceNoticeElement implements UIElement {

	/**
	 * Configuration for {@link MaintenanceNoticeElement}.
	 */
	@TagName("maintenance-notice")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(MaintenanceNoticeElement.class)
		Class<? extends UIElement> getImplementationClass();

	}

	/**
	 * What the user is to be told about the maintenance window.
	 *
	 * @see MaintenanceNoticeElement#notice(int, String, long)
	 */
	public static final class Notice {

		private final Severity _severity;

		private final ResKey _text;

		private final Long _deadline;

		Notice(Severity severity, ResKey text, Long deadline) {
			_severity = severity;
			_text = text;
			_deadline = deadline;
		}

		/**
		 * How urgent the notice is.
		 */
		public Severity getSeverity() {
			return _severity;
		}

		/**
		 * The message to display.
		 */
		public ResKey getText() {
			return _text;
		}

		/**
		 * The time the announced event occurs as epoch milliseconds, or {@code null} if there is
		 * nothing to count down to.
		 */
		public Long getDeadline() {
			return _deadline;
		}

	}

	/**
	 * Creates a new {@link MaintenanceNoticeElement} from configuration.
	 */
	@CalledByReflection
	public MaintenanceNoticeElement(InstantiationContext context, Config config) {
		// No configuration needed.
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		MaintenanceWindowManager manager = MaintenanceWindowManager.getInstance();
		ReactNoticeBarControl control = new ReactNoticeBarControl(context);

		// Bind the viewing session's language now: the listener below runs in the thread of
		// whoever switches the mode, where the ambient locale is that administrator's, not this
		// user's.
		Resources resources = Resources.getInstance();
		update(control, manager, resources);

		MaintenanceStateListener listener = (sender, oldState, newState) -> update(control, sender, resources);
		manager.addMaintenanceStateListener(listener);
		control.addCleanupAction(() -> manager.removeMaintenanceStateListener(listener));

		return control;
	}

	/**
	 * Transfers the manager's current state to the given control.
	 *
	 * @param resources
	 *        The resources of the session displaying the control, bound when the control was
	 *        created.
	 *
	 * @implNote Called both when the control is built and from the
	 *           {@link MaintenanceStateListener} - in the latter case from the thread that caused
	 *           the state change, which is a foreign one for this control's session. That is safe:
	 *           the resulting patch travels through the session's SSE queue, which is built for
	 *           writes from other threads. The session's language, however, is not ambient in that
	 *           thread and therefore has to be passed in.
	 */
	private static void update(ReactNoticeBarControl control, MaintenanceWindowManager manager,
			Resources resources) {
		Notice notice =
			notice(manager.getMaintenanceModeState(), manager.getUserMessage(), manager.getFinishedTime());
		if (notice == null) {
			control.hide();
		} else {
			control.show(notice.getSeverity(), resources.getString(notice.getText()), notice.getDeadline());
		}
	}

	/**
	 * The state of the maintenance window as the user is to be told about it, or {@code null} while
	 * the system operates normally and there is nothing to announce.
	 *
	 * @param state
	 *        The current state, see {@link MaintenanceWindowManager#getMaintenanceModeState()}.
	 * @param message
	 *        What an administrator entered when activating the window, or {@code null} if nothing
	 *        was entered, see {@link MaintenanceWindowManager#getUserMessage()}. It is shown in
	 *        both phases, framed by the description of the state: it names the reason ("database
	 *        maintenance"), which is what a user wants to know, but never says by itself whether
	 *        the switch is still ahead or has already happened.
	 * @param finishedTime
	 *        When the announced window starts, or a value {@code <= 0} if that is unknown, see
	 *        {@link MaintenanceWindowManager#getFinishedTime()}.
	 */
	public static Notice notice(int state, String message, long finishedTime) {
		String userMessage = (message == null || message.isBlank()) ? null : message;
		switch (state) {
			case MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE:
				// A finish time of -1 means that no timer is running, so there is nothing to count
				// down to even though the window is announced.
				Long deadline = finishedTime > 0 ? Long.valueOf(finishedTime) : null;
				// Framed by the state description, as in the active case below: on its own the
				// message would name the reason ("database maintenance") without saying that the
				// system is about to switch at all.
				ResKey pending = userMessage == null
					? I18NConstants.MAINTENANCE_NOTICE_PENDING
					: I18NConstants.MAINTENANCE_NOTICE_PENDING__MESSAGE.fill(userMessage);
				return new Notice(Severity.WARNING, pending, deadline);
			case MaintenanceWindowManager.IN_MAINTENANCE_MODE:
				// An active window ends when an administrator ends it, so no deadline exists.
				ResKey active = userMessage == null
					? I18NConstants.MAINTENANCE_NOTICE_ACTIVE
					: I18NConstants.MAINTENANCE_NOTICE_ACTIVE__MESSAGE.fill(userMessage);
				return new Notice(Severity.ERROR, active, null);
			default:
				return null;
		}
	}

}
