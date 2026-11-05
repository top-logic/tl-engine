/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatState;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
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
	/**
	 * Sets the approval mode of the chat {@code sessionId}: on this window's copy, on the copies its
	 * running runs hold, in its stored metadata, and on the account's other windows showing it.
	 */
	public static void applyToChat(TaskProgressTracker tracker, Person person, String sessionId,
			ApprovalMode approvalMode) {
		// The chat named, not the one the window shows, and without switching to it. The mode is
		// stored below; a copy of the chat this window holds is brought in line here, and one it does
		// not hold reads the stored mode when it is loaded.
		ChatState held = tracker.getSessionManager().getChatState(sessionId);
		if (held != null) {
			held.setApprovalMode(approvalMode);
		}
		// A run of the chat reads the switch at every approval, from the copy it was started with,
		// which is another window's when another window started it (#943).
		for (ChatState running : PendingInteractionRegistry.chatsOf(person, sessionId)) {
			running.setApprovalMode(approvalMode);
		}
		// Written on its own rather than with the tracker's whole bag: a persisted bag keeps the
		// stored mode, so that no window writes back the one it opened the chat with.
		ChatHistoryService.updateMetadata(ChatHistoryService.findOwnedSessionById(sessionId, person),
			stored -> DtoMapper.withApprovalMode(stored, approvalMode));
		for (ChatEventHandler other : ChatEventHandlerRegistry.findForSession(person, sessionId)) {
			TaskProgressTracker window = other.getTracker();
			if (window == tracker) {
				continue;
			}
			window.getSessionManager().markChangedElsewhere(sessionId);
			try {
				other.sendApprovalMode(approvalMode);
			} catch (RuntimeException ex) {
				Logger.info("Could not tell a window that the approval mode of chat " + sessionId
					+ " changed: " + ex.getMessage(), ApprovalChoice.class);
			}
		}
	}

}
