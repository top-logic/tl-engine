/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Chat-side access to persistent {@link AgentSession} objects (kind CHAT).
 *
 * <p>
 * Sessions and executions are stored through the {@link AgentExecutionStore}; the conversation
 * itself lives as one transcript blob per {@link AgentExecution}, written by the agent runner at
 * the end of each run. This class merges those per-run transcripts back into a single
 * conversation for loading. Legacy <code>tl.ai.chat:ChatSession</code> blobs are migrated
 * eagerly at startup by {@link LegacyChatSessionMigration}.
 * </p>
 *
 * <p>
 * All methods require a TopLogic context bound on the calling thread; the chat servlet binds the
 * user's sub-session for the duration of the request.
 * </p>
 */
public class ChatHistoryService {

	/** KB table storing {@link AgentSession} objects. */
	private static final String SESSION_TABLE_NAME = "AgentSession";

	private static final String ATTR_TITLE = "title";

	private static final String ATTR_LAST_UPDATED = "lastUpdated";

	private static final String ATTR_METADATA = "metadata";

	private static final String ATTR_OWNER = "owner";

	private ChatHistoryService() {
		// Static utility class
	}

	/**
	 * Returns all chat sessions owned by the given person, sorted by <code>lastUpdated</code>
	 * descending.
	 */
	public static List<TLObject> getSessionsForUser(Person person) {
		if (person == null) {
			return Collections.emptyList();
		}
		try {
			return new ArrayList<>(store().sessionsForOwner(person, SessionKind.CHAT));
		} catch (RuntimeException ex) {
			Logger.error("Failed to get sessions: " + ex.getMessage(), ex, ChatHistoryService.class);
			return Collections.emptyList();
		}
	}

	/**
	 * Creates a new chat session with the given owner.
	 *
	 * @return the created session object, or null on failure.
	 */
	public static TLObject createSession(String title, Person owner) {
		try {
			return store().createSession(SessionKind.CHAT, owner, title);
		} catch (RuntimeException ex) {
			Logger.error("Failed to create chat session: " + ex.getMessage(), ex, ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Updates the title of a chat session.
	 */
	public static void updateTitle(TLObject session, String newTitle) {
		try {
			store().updateTitle((AgentSession) session, newTitle);
		} catch (RuntimeException ex) {
			Logger.error("Failed to update chat session title: " + ex.getMessage(), ex,
				ChatHistoryService.class);
		}
	}

	/**
	 * Saves the session metadata (approved tools, approval mode, tasks, loaded skills).
	 */
	public static void saveMetadata(TLObject session, String metadataJson) {
		if (metadataJson == null) {
			return;
		}
		try {
			store().saveMetadata((AgentSession) session, metadataJson);
		} catch (RuntimeException ex) {
			Logger.error("Failed to save chat session metadata: " + ex.getMessage(), ex,
				ChatHistoryService.class);
		}
	}

	/**
	 * Deletes a chat session including all recorded executions, tasks and interactions.
	 */
	public static void deleteSession(TLObject session) {
		try {
			store().deleteSession((AgentSession) session);
		} catch (RuntimeException ex) {
			Logger.error("Failed to delete chat session: " + ex.getMessage(), ex, ChatHistoryService.class);
		}
	}

	/**
	 * Loads the session's full conversation as a single {@code PersistedConversationV2} JSON
	 * blob, merged from the per-execution transcripts in run order.
	 *
	 * @return the merged conversation JSON, or null if the session has no recorded turns.
	 */
	public static String loadConversationJson(TLObject session) {
		try {
			PersistedConversationV2 merged = PersistedConversationV2.create();
			for (AgentExecution execution : store().executionsOf((AgentSession) session)) {
				// Only top-level runs belong to the main chat stream. Sub-agent runs are recorded
				// as child executions (parentExecution set) with their own transcript; merging
				// them here would splice a sub-agent's internal turns inline into the chat. They
				// are surfaced separately via the background-tasks panel (execution transcript
				// endpoint).
				if (execution.getParentExecution() != null) {
					continue;
				}
				String transcript = execution.getTranscript();
				if (transcript == null || transcript.isEmpty()) {
					continue;
				}
				try {
					PersistedConversationV2 part = PersistedConversationV2
						.readPersistedConversationV2(new JsonReader(new StringR(transcript)));
					for (PersistedTurn turn : part.getTurns()) {
						merged.addTurn(turn);
					}
				} catch (IOException | RuntimeException ex) {
					Logger.warn("Skipping unreadable transcript of run " + execution.getRunId()
						+ ": " + ex.getMessage(), ChatHistoryService.class);
				}
			}
			if (merged.getTurns().isEmpty()) {
				return null;
			}
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				merged.writeTo(json);
			}
			return buffer.toString();
		} catch (IOException | RuntimeException ex) {
			Logger.error("Failed to load conversation: " + ex.getMessage(), ex, ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Returns the title of a session.
	 */
	public static String getTitle(TLObject session) {
		try {
			return (String) session.tValueByName(ATTR_TITLE);
		} catch (Exception ex) {
			Logger.error("Failed to get session title: " + ex.getMessage(), ex, ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Returns the metadata JSON for the given session, or null if not set.
	 */
	public static String getMetadataJson(TLObject session) {
		try {
			return (String) session.tValueByName(ATTR_METADATA);
		} catch (Exception ex) {
			Logger.error("Failed to get session metadata: " + ex.getMessage(), ex, ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Returns the last updated date of a session.
	 */
	public static Date getLastUpdated(TLObject session) {
		try {
			return (Date) session.tValueByName(ATTR_LAST_UPDATED);
		} catch (Exception ex) {
			Logger.error("Failed to get last updated: " + ex.getMessage(), ex, ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Finds an {@link AgentSession} by its {@link KnowledgeObject} ID.
	 *
	 * @param objectId
	 *        the string representation of the object ID.
	 * @return the session object, or null if not found.
	 */
	public static TLObject findSessionById(String objectId) {
		if (objectId == null || objectId.isEmpty()) {
			return null;
		}
		long id;
		try {
			// Handle both plain numbers ("9002") and TLID format ("ID(9002)")
			String numStr = objectId;
			if (numStr.startsWith("ID(") && numStr.endsWith(")")) {
				numStr = numStr.substring(3, numStr.length() - 1);
			}
			id = Long.parseLong(numStr);
		} catch (NumberFormatException ex) {
			return null; // Invalid ID format from client — not a server error
		}
		try {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			KnowledgeObject ko = kb.getKnowledgeObject(SESSION_TABLE_NAME, LongID.valueOf(id));
			if (ko != null) {
				return ko.getWrapper();
			}
			return null;
		} catch (Exception ex) {
			Logger.error("Failed to find chat session " + objectId + ": " + ex.getMessage(), ex,
				ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Checks if the given session is owned by the given person.
	 */
	public static boolean isOwnedBy(TLObject session, Person person) {
		if (person == null) {
			return false;
		}
		try {
			Object owner = session.tValueByName(ATTR_OWNER);
			return person.equals(owner);
		} catch (Exception ex) {
			Logger.error("Failed to check session ownership: " + ex.getMessage(), ex, ChatHistoryService.class);
			return false;
		}
	}

	private static AgentExecutionStore store() {
		return AgentExecutionStore.getInstance();
	}
}
