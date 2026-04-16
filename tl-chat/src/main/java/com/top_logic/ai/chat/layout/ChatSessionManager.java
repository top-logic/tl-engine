/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.top_logic.ai.chat.api.ConversationLogSerializer;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.Logger;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.memory.CompactionStrategy;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;

/**
 * Manages per-session in-memory state ({@link Chat} + {@link ConversationMemory}) with LRU caching.
 *
 * <p>
 * Each chat session gets its own {@link Chat} (message container) and {@link ConversationMemory}
 * (conversation context). When the user switches sessions, this manager persists the current
 * session, loads the target session from the database (or LRU cache), and rebuilds the AI's
 * conversation memory from the persisted messages.
 * </p>
 */
public class ChatSessionManager {

	/** Maximum number of sessions to keep in the in-memory LRU cache. */
	private static final int MAX_CACHED_SESSIONS = 5;

	/**
	 * Default context window budget for chat session memory. This is a generous default that allows
	 * the {@link TruncatingStrategy} to keep enough context for multi-turn conversations. The
	 * actual model context window is enforced by the agent loop; this budget only controls how many
	 * messages the session memory retains.
	 */
	private static final int DEFAULT_CONTEXT_WINDOW_TOKENS = 128_000;

	private final Object _lock = new Object();

	/** Back-reference to the owning tracker for metadata serialization. */
	private volatile TaskProgressTracker _tracker;

	/** LRU cache of per-session state, bounded to {@link #MAX_CACHED_SESSIONS}. */
	private final Map<String, SessionState> _sessions = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, SessionState> eldest) {
			return size() > MAX_CACHED_SESSIONS;
		}
	};

	/** The currently active session ID, or null if none. */
	private volatile String _activeSessionId;

	/**
	 * Per-session in-memory state: the chat messages and the AI's conversation memory.
	 */
	static class SessionState {
		final Chat _chat;

		ConversationMemory _conversationMemory;

		ConversationLog _conversationLog;

		/** */
		SessionState(Chat chat, ConversationMemory conversationMemory) {
			_chat = chat;
			_conversationMemory = conversationMemory;
		}
	}

	/**
	 * Sets the owning {@link TaskProgressTracker} for metadata persistence during session switches.
	 */
	void setTracker(TaskProgressTracker tracker) {
		_tracker = tracker;
	}

	/**
	 * Returns the active session's {@link Chat}, or a default empty chat if no session is active.
	 */
	public Chat getActiveChat() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				return state._chat;
			}
			// No active session — return a transient chat (will be replaced on session activation)
			return new Chat();
		}
	}

	/**
	 * Returns the active session's {@link ConversationMemory}, or creates a transient one if no
	 * session is active.
	 */
	public ConversationMemory getActiveConversationMemory() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._conversationMemory == null) {
					state._conversationMemory = createDefaultMemory();
				}
				return state._conversationMemory;
			}
			return createDefaultMemory();
		}
	}

	/**
	 * Returns the active session's {@link ConversationLog}, creating one if it does not exist.
	 * Returns a transient log if no session is active.
	 */
	public ConversationLog getOrCreateConversationLog() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._conversationLog == null) {
					state._conversationLog = new ConversationLog();
				}
				return state._conversationLog;
			}
			return new ConversationLog();
		}
	}

	/**
	 * Returns the active session ID, or null if none.
	 */
	public String getActiveSessionId() {
		return _activeSessionId;
	}

	/**
	 * Switches the active session. Persists the current session first, then loads the target session
	 * from the database (or LRU cache) and rebuilds the AI's conversation memory.
	 *
	 * @param sessionId
	 *        The session ID to switch to.
	 */
	public void switchSession(String sessionId) {
		if (sessionId == null) {
			return;
		}
		synchronized (_lock) {
			if (sessionId.equals(_activeSessionId)) {
				return;
			}

			// Persist current session before switching
			persistActiveSessionInternal();

			// Reset tracker state immediately so it is never retained from the previous session,
			// regardless of whether the new session is found in the LRU cache or loaded from DB.
			if (_tracker != null) {
				_tracker.resetSessionState();
			}

			// Check LRU cache first
			SessionState cached = _sessions.get(sessionId);
			if (cached != null) {
				_activeSessionId = sessionId;
				return;
			}

			// Load from database
			Chat chat = new Chat();
			ConversationMemory memory = createDefaultMemory();
			ConversationLog convLog = null;

			try {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session != null) {
					String json = ChatHistoryService.getMessagesJson(session);
					if (json != null && !json.isEmpty()) {
						// V2 format is a JSON object (starts with '{'), V1 is a JSON array ('[')
						if (json.trim().startsWith("{")) {
							convLog = ConversationLogSerializer.fromJson(json);
							ConversationLogReplay.rebuildMemory(convLog, memory.conversation());
						} else {
							List<ChatMessage> messages = DtoMapper.deserializeMessages(json);
							chat.loadMessages(messages);
							rebuildConversationMemory(memory, messages);
						}
					}

					if (_tracker != null) {
						String metadataJson = ChatHistoryService.getMetadataJson(session);
						DtoMapper.loadMetadata(metadataJson, _tracker);
					}
				}
			} catch (Exception ex) {
				Logger.error("Failed to load session " + sessionId + ": " + ex.getMessage(), ex,
					ChatSessionManager.class);
			}

			SessionState newState = new SessionState(chat, memory);
			newState._conversationLog = convLog;
			_sessions.put(sessionId, newState);
			_activeSessionId = sessionId;
		}
	}

	/**
	 * Persists the active session's full state (messages) to the database.
	 */
	public void persistActiveSession() {
		synchronized (_lock) {
			persistActiveSessionInternal();
		}
	}

	private void persistActiveSessionInternal() {
		String sessionId = _activeSessionId;
		if (sessionId == null) {
			return;
		}
		SessionState state = _sessions.get(sessionId);
		if (state == null) {
			return;
		}
		try {
			TLObject session = ChatHistoryService.findSessionById(sessionId);
			if (session != null) {
				String json;
				if (state._conversationLog != null) {
					json = ConversationLogSerializer.toJson(state._conversationLog);
				} else {
					json = DtoMapper.serializeMessages(state._chat.getMessages());
				}
				ChatHistoryService.saveMessages(session, json);

				if (_tracker != null) {
					String metadataJson = DtoMapper.serializeMetadata(_tracker);
					ChatHistoryService.saveMetadata(session, metadataJson);
				}
			}
		} catch (Exception ex) {
			Logger.error("Failed to persist session " + sessionId + ": " + ex.getMessage(), ex,
				ChatSessionManager.class);
		}
	}

	/**
	 * Rebuilds a {@link ConversationMemory} from persisted chat messages by replaying user and
	 * assistant messages into the underlying {@link Conversation}.
	 */
	private static void rebuildConversationMemory(ConversationMemory memory, List<ChatMessage> messages) {
		Conversation conversation = memory.conversation();
		for (ChatMessage msg : messages) {
			String role = msg.getUser();
			if ("Agent".equals(role) || "AI".equals(role)) {
				conversation.add(new AssistantMessage(
					UUID.randomUUID().toString(), Instant.now(),
					List.of(new TextContent(msg.getText())),
					TokenUsage.EMPTY, "stop"));
			} else if (!"System".equals(role)) {
				conversation.add(new UserMessage(
					UUID.randomUUID().toString(), Instant.now(),
					List.of(new TextContent(msg.getText()))));
			}
		}
	}

	/**
	 * Creates a default {@link ConversationMemory} with truncating strategy.
	 */
	private static ConversationMemory createDefaultMemory() {
		CompactionStrategy strategy = new TruncatingStrategy();
		return ConversationMemory.withDefaults(strategy, DEFAULT_CONTEXT_WINDOW_TOKENS);
	}
}
