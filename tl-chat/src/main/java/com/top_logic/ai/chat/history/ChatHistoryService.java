/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.UnaryOperator;

import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.messages.SessionMetadata;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
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
 * conversation for loading. Legacy <code>tl.ai.chat:ChatSession</code> blobs were migrated
 * eagerly at startup by a conversion service; that model and that service have since been removed
 * (tl-ai-core migration {@code Issue_230_remove_module_tl_ai_chat}).
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
	 * Creates a new chat session with the given owner, which runs with the given approval mode.
	 *
	 * <p>
	 * A chat that stores no approval mode runs with {@link ApprovalMode#MANUAL} (#808), so a mode
	 * chosen before the chat exists - by the user before the first send, or by a scheduled task - is
	 * stored with the chat when it is created.
	 * </p>
	 *
	 * @return the created session object, or null on failure.
	 */
	public static TLObject createSession(String title, Person owner, ApprovalMode mode) {
		TLObject session = createSession(title, owner);
		if (session != null && mode != ApprovalMode.MANUAL) {
			updateMetadata(session, stored -> DtoMapper.withApprovalMode(stored, mode));
		}
		return session;
	}

	/**
	 * Changes the session metadata in one step, see
	 * {@link AgentExecutionStore#updateMetadata(AgentSession, UnaryOperator)}.
	 *
	 * @param session
	 *        The chat whose metadata changes.
	 * @param change
	 *        Receives the stored metadata JSON ({@code null} when none was stored yet) and returns
	 *        the JSON to store.
	 */
	public static void updateMetadata(TLObject session, UnaryOperator<String> change) {
		try {
			store().updateMetadata((AgentSession) session, change);
		} catch (RuntimeException ex) {
			Logger.error("Failed to save chat session metadata: " + ex.getMessage(), ex,
				ChatHistoryService.class);
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
	 * Drops the recorded tail of a session's conversation.
	 *
	 * @param session
	 *        the session to truncate
	 * @param keepTurns
	 *        how many turns to keep, counted from the start
	 * @return how many turns were removed
	 * @see com.top_logic.tl.ai.core.execution.AgentExecutionStore#truncateConversation
	 */
	public static int truncateConversation(TLObject session, int keepTurns) {
		try {
			return store().truncateConversation((AgentSession) session, keepTurns);
		} catch (RuntimeException ex) {
			Logger.error("Failed to truncate chat session: " + ex.getMessage(), ex, ChatHistoryService.class);
			return 0;
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
	 * Notes that a window of the owner shows the given chat now.
	 *
	 * <p>
	 * Only the {@link SessionMetadata#getLastSeen() last seen} entry of the metadata bag changes, and
	 * it changes in the bag as the database holds it when the write happens, not in a copy this
	 * thread read earlier - the approval mode stored there stays (#658). Best-effort: a failure is
	 * logged.
	 * </p>
	 *
	 * @see #lastSeen(TLObject)
	 */
	public static void markSeen(TLObject session) {
		if (!(session instanceof AgentSession)) {
			return;
		}
		long now = System.currentTimeMillis();
		updateMetadata(session, json -> {
			SessionMetadata metadata = readMetadata(json);
			// An unreadable bag is left as it is: this is only a note that the chat was shown.
			return metadata == null ? json : DtoMapper.toJson(metadata.setLastSeen(now));
		});
	}

	/**
	 * When a window of the owner last showed the given chat, in milliseconds since the epoch; 0 if
	 * never.
	 *
	 * @see #markSeen(TLObject)
	 */
	public static long lastSeen(TLObject session) {
		SessionMetadata metadata = readMetadata(getMetadataJson(session));
		return metadata == null ? 0 : metadata.getLastSeen();
	}

	/** The metadata bag of the given JSON; empty for none, <code>null</code> for one unreadable. */
	private static SessionMetadata readMetadata(String json) {
		if (json == null || json.isEmpty()) {
			return SessionMetadata.create();
		}
		try {
			return SessionMetadata.readSessionMetadata(new JsonReader(new StringR(json)));
		} catch (IOException | RuntimeException ex) {
			Logger.warn("Unreadable chat session metadata: " + ex.getMessage(), ChatHistoryService.class);
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
	 * Finds a session of the given owner by its {@link KnowledgeObject} ID.
	 *
	 * <p>
	 * The lookup every request-handling path must use. Session IDs are small sequential integers, so
	 * an ID taken from a request body names someone else's chat as easily as the caller's own;
	 * anything that acts on it - and the three that remove turns above all - has to establish first
	 * that it is theirs.
	 * </p>
	 *
	 * @param objectId
	 *        the string representation of the object ID
	 * @param owner
	 *        the person the session must belong to; {@code null} owns nothing
	 * @return the session, or {@code null} when it does not exist or is not this person's
	 * @see #findAnySessionById(String)
	 */
	public static TLObject findOwnedSessionById(String objectId, Person owner) {
		if (owner == null) {
			return null;
		}
		TLObject session = findAnySessionById(objectId);
		return session != null && isOwnedBy(session, owner) ? session : null;
	}

	/**
	 * Finds an {@link AgentSession} by its {@link KnowledgeObject} ID, no matter whose it is.
	 *
	 * <p>
	 * Named for what it does, because what it does is dangerous: it hands out any user's chat to
	 * whoever knows its number, and the numbers are small sequential integers. Twice already a
	 * request-handling path reached for the short name and got the unguarded lookup; nothing that
	 * acts on an ID a request supplied may call this. It is left for callers that have no requester
	 * to check against at all - a migration, a background job started by the system.
	 * </p>
	 *
	 * @param objectId
	 *        the string representation of the object ID.
	 * @return the session object, or null if not found.
	 * @see #findOwnedSessionById(String, Person)
	 */
	public static TLObject findAnySessionById(String objectId) {
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
