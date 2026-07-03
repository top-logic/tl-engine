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
import java.util.UUID;

import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedConversationV2;
import com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn;
import com.top_logic.util.model.ModelService;

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
 * conversation for loading, and transparently migrates legacy <code>tl.ai.chat:ChatSession</code>
 * blobs on first access.
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

	// Legacy tl.ai.chat:ChatSession (single-blob format), migrated on first access.

	private static final String LEGACY_MODULE_NAME = "tl.ai.chat";

	private static final String LEGACY_TYPE_NAME = "ChatSession";

	private static final String ATTR_TITLE = "title";

	private static final String ATTR_MESSAGES = "messages";

	private static final String ATTR_CREATED_AT = "createdAt";

	private static final String ATTR_LAST_UPDATED = "lastUpdated";

	private static final String ATTR_METADATA = "metadata";

	private static final String ATTR_OWNER = "owner";

	/** Agent name recorded on the synthetic execution holding a migrated legacy transcript. */
	private static final String LEGACY_IMPORT_AGENT = "legacy-import";

	private ChatHistoryService() {
		// Static utility class
	}

	/**
	 * Returns all chat sessions owned by the given person, sorted by <code>lastUpdated</code>
	 * descending.
	 *
	 * <p>
	 * Legacy <code>ChatSession</code> objects of the person are migrated to
	 * {@link AgentSession}s before listing.
	 * </p>
	 */
	public static List<TLObject> getSessionsForUser(Person person) {
		if (person == null) {
			return Collections.emptyList();
		}
		try {
			migrateLegacySessions(person);
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

	// ---- Legacy migration ----

	/**
	 * Migrates all legacy <code>tl.ai.chat:ChatSession</code> objects of the given person to
	 * {@link AgentSession}s.
	 *
	 * <p>
	 * Each legacy session becomes an {@link AgentSession} (kind CHAT) with the same title,
	 * timestamps and metadata, plus one synthetic completed {@link AgentExecution} carrying the
	 * old V2 message blob as its transcript. Legacy blobs in the pre-V2 format cannot be
	 * replayed and are dropped with a warning. The legacy object is deleted after migration.
	 * </p>
	 */
	private static void migrateLegacySessions(Person person) {
		TLClass legacyType = getLegacySessionType();
		if (legacyType == null) {
			return;
		}
		List<TLObject> legacySessions;
		try {
			legacySessions = MetaElementUtil.getAllInstancesOf(legacyType, TLObject.class);
		} catch (RuntimeException ex) {
			return;
		}
		for (TLObject legacy : legacySessions) {
			if (!person.equals(legacy.tValueByName(ATTR_OWNER))) {
				continue;
			}
			try {
				migrateLegacySession(legacy, person);
			} catch (RuntimeException ex) {
				Logger.error("Failed to migrate legacy chat session: " + ex.getMessage(), ex,
					ChatHistoryService.class);
			}
		}
	}

	private static void migrateLegacySession(TLObject legacy, Person owner) {
		String title = (String) legacy.tValueByName(ATTR_TITLE);
		String messagesJson = (String) legacy.tValueByName(ATTR_MESSAGES);
		String metadataJson = (String) legacy.tValueByName(ATTR_METADATA);
		Date createdAt = (Date) legacy.tValueByName(ATTR_CREATED_AT);
		Date lastUpdated = (Date) legacy.tValueByName(ATTR_LAST_UPDATED);

		AgentExecutionStore store = store();
		AgentSession session = store.createSession(SessionKind.CHAT, owner, title);
		if (metadataJson != null) {
			store.saveMetadata(session, metadataJson);
		}

		if (messagesJson != null && !messagesJson.isEmpty()) {
			if (messagesJson.trim().startsWith("{")) {
				AgentExecution execution = store.beginExecution(session,
					"legacy-" + UUID.randomUUID(), LEGACY_IMPORT_AGENT, null, null, null);
				store.finishExecution(execution, ExecutionStatus.COMPLETE, null, null, null,
					messagesJson);
			} else {
				Logger.warn("Legacy chat session '" + title
					+ "' has a pre-V2 message blob; conversation content is not migrated.",
					ChatHistoryService.class);
			}
		}

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Migrate legacy chat session"))) {
			// Preserve the original timestamps for correct history ordering.
			session.tUpdateByName(ATTR_CREATED_AT, createdAt != null ? createdAt : new Date());
			if (lastUpdated != null) {
				session.tUpdateByName(ATTR_LAST_UPDATED, lastUpdated);
			}
			legacy.tDelete();
			tx.commit();
		}
		Logger.info("Migrated legacy chat session '" + title + "' to AgentSession.",
			ChatHistoryService.class);
	}

	private static AgentExecutionStore store() {
		return AgentExecutionStore.getInstance();
	}

	private static TLClass getLegacySessionType() {
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(LEGACY_MODULE_NAME);
		if (module == null) {
			return null;
		}
		return (TLClass) module.getType(LEGACY_TYPE_NAME);
	}
}
