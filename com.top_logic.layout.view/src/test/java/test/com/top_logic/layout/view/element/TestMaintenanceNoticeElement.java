/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import junit.framework.TestCase;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.common.ReactNoticeBarControl.Severity;
import com.top_logic.layout.view.element.I18NConstants;
import com.top_logic.layout.view.element.MaintenanceNoticeElement;
import com.top_logic.layout.view.element.MaintenanceNoticeElement.Notice;

/**
 * Tests the mapping from the {@link MaintenanceWindowManager}'s state to the notice displayed by a
 * {@link MaintenanceNoticeElement}.
 *
 * <p>
 * The mapping carries the element's actual decisions - whether to show anything, how urgent it is,
 * and whether a count-down is due - and is a pure function of the manager's three observable
 * values, so it is tested without a running application.
 * </p>
 */
public class TestMaintenanceNoticeElement extends TestCase {

	private static final long SOME_TIME = 1_800_000_000_000L;

	/**
	 * In normal operation there is nothing to announce, so no notice is produced at all (rather
	 * than an invisible one).
	 */
	public void testNormalOperationIsSilent() {
		assertNull("Normal operation must not produce a notice.",
			MaintenanceNoticeElement.notice(MaintenanceWindowManager.DEFAULT_MODE, "any message", -1));
	}

	/**
	 * While the maintenance window is only announced, the notice warns and counts down to the
	 * moment the window starts.
	 */
	public void testAnnouncedWindowCountsDown() {
		Notice notice =
			MaintenanceNoticeElement.notice(MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE, "", SOME_TIME);

		assertNotNull("An announced window must be shown.", notice);
		assertEquals("An announced window is a warning, not yet a restriction.",
			Severity.WARNING, notice.getSeverity());
		assertEquals("The count-down must run towards the start of the window.",
			Long.valueOf(SOME_TIME), notice.getDeadline());
	}

	/**
	 * Once the maintenance window is active there is no known end, so the notice carries no
	 * deadline and therefore no count-down.
	 */
	public void testActiveWindowHasNoDeadline() {
		Notice notice = MaintenanceNoticeElement.notice(MaintenanceWindowManager.IN_MAINTENANCE_MODE, "", -1);

		assertNotNull("An active window must be shown.", notice);
		assertEquals("An active window already restricts the user.", Severity.ERROR, notice.getSeverity());
		assertNull("The end of an active window is unknown, so no count-down may be shown.",
			notice.getDeadline());
	}

	/**
	 * While the window is announced, the administrator's message is framed by the state
	 * description too: on its own it would name the reason without saying that a switch is coming
	 * at all.
	 */
	public void testAnnouncedWindowCarriesMessageFramedByState() {
		String message = "Database maintenance until 6 pm.";
		Notice notice =
			MaintenanceNoticeElement.notice(MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE, message,
				SOME_TIME);

		assertEquals("The message must be framed by the state description.",
			I18NConstants.MAINTENANCE_NOTICE_PENDING__MESSAGE.fill(message), notice.getText());
		assertFalse("Framed, not standing alone.", ResKey.text(message).equals(notice.getText()));
	}

	/**
	 * Should the manager yield no message at all, a generic announcement takes its place - the
	 * notice must never appear without text.
	 */
	public void testBlankMessageFallsBackToGenericText() {
		Notice noMessage =
			MaintenanceNoticeElement.notice(MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE, null, SOME_TIME);
		Notice blankMessage =
			MaintenanceNoticeElement.notice(MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE, "  ", SOME_TIME);

		assertNotNull("A missing message must be replaced by a generic announcement.", noMessage.getText());
		assertEquals("A blank message must be replaced by the same generic announcement.",
			noMessage.getText(), blankMessage.getText());
		assertFalse("The fallback must not be the empty text.",
			ResKey.text("").equals(noMessage.getText()));
	}

	/**
	 * The active window frames the message in the same way, so that the two states differ only in
	 * their state description and stay comparable to the user.
	 */
	public void testActiveWindowCarriesMessageFramedByState() {
		String message = "Database maintenance until 6 pm.";
		Notice active = MaintenanceNoticeElement.notice(MaintenanceWindowManager.IN_MAINTENANCE_MODE, message, -1);

		assertEquals("The message must be framed by the state description.",
			I18NConstants.MAINTENANCE_NOTICE_ACTIVE__MESSAGE.fill(message), active.getText());
		assertFalse("Framed, not standing alone.", ResKey.text(message).equals(active.getText()));
	}

	/**
	 * Announcement and active window must stay distinguishable even for the same message,
	 * otherwise a user cannot tell whether the switch has already happened.
	 */
	public void testStatesStayDistinguishable() {
		String message = "Database maintenance until 6 pm.";
		Notice pending = MaintenanceNoticeElement
			.notice(MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE, message, SOME_TIME);
		Notice active = MaintenanceNoticeElement.notice(MaintenanceWindowManager.IN_MAINTENANCE_MODE, message, -1);

		assertFalse("Announcement and active window must not read alike.",
			pending.getText().equals(active.getText()));
	}

	/**
	 * Without a message the active window falls back to the bare state description, so that no
	 * dangling frame around nothing is rendered.
	 */
	public void testActiveWindowWithoutMessageUsesBareState() {
		Notice active = MaintenanceNoticeElement.notice(MaintenanceWindowManager.IN_MAINTENANCE_MODE, null, -1);

		assertEquals(I18NConstants.MAINTENANCE_NOTICE_ACTIVE, active.getText());
	}

	/**
	 * An announced window whose finish time is unknown must not produce a count-down running
	 * towards a meaningless point in time.
	 *
	 * @see MaintenanceWindowManager#getFinishedTime()
	 */
	public void testUnknownFinishTimeYieldsNoDeadline() {
		Notice notice =
			MaintenanceNoticeElement.notice(MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE, "", -1);

		assertNotNull("The announcement must still be shown.", notice);
		assertNull("Without a known finish time there is nothing to count down to.", notice.getDeadline());
	}

}
