/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;

/**
 * The last position a person gave the auto-approve switch, stored on their
 * {@link PersonalConfiguration}.
 *
 * <p>
 * The switch belongs to the person: a new chat starts with the position the person chose last, in
 * whichever chat and window. A chat that exists keeps the mode stored with it (#658). The choice is
 * one for the whole account, not one per {@code chat-id} like the layout in
 * {@link ChatPreferencesStore}: it says how much the person wants to be asked, not how a place
 * looks.
 * </p>
 */
public final class ApprovalChoice {

	/** {@link PersonalConfiguration} key. Namespaced so it cannot collide with another setting. */
	public static final String KEY = "tl-chat.approvalMode";

	private ApprovalChoice() {
		// Utility
	}

	/** The current person's last choice, {@link ApprovalMode#MANUAL} while they made none. */
	public static ApprovalMode load() {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc != null && pc.getJSONValue(KEY) instanceof String stored) {
				return ApprovalMode.valueOf(stored);
			}
		} catch (RuntimeException ex) {
			Logger.debug("Approval choice could not be read: " + ex.getMessage(), ApprovalChoice.class);
		}
		return ApprovalMode.MANUAL;
	}

	/**
	 * Stores {@code mode} as the current person's last choice.
	 *
	 * <p>
	 * Stored at once: a value set without
	 * {@link PersonalConfiguration#storePersonalConfiguration()} is lost when the session unbinds.
	 * </p>
	 */
	public static void save(ApprovalMode mode) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null || mode.name().equals(pc.getJSONValue(KEY))) {
				return;
			}
			pc.setJSONValue(KEY, mode.name());
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Approval choice could not be stored: " + ex.getMessage(), ex, ApprovalChoice.class);
		}
	}
}
