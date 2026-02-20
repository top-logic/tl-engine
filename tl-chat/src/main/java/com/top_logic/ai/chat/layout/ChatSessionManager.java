/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.Logger;
import com.top_logic.model.TLObject;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * Manages per-session in-memory state ({@link Chat} + {@link ChatMemory}) with LRU caching.
 *
 * <p>
 * Each chat session gets its own {@link Chat} (message container) and {@link ChatMemory}
 * (<code>LangChain4j</code> conversation context). When the user switches sessions, this manager persists the
 * current session, loads the target session from the database (or LRU cache), and rebuilds the AI's
 * conversation memory from the persisted messages.
 * </p>
 */
public class ChatSessionManager {

	/** Maximum number of sessions to keep in the in-memory LRU cache. */
	private static final int MAX_CACHED_SESSIONS = 5;

	/** Maximum messages in LangChain4j ChatMemory (sliding window). */
	private static final int CHAT_MEMORY_MAX_MESSAGES = 20;

	private final Object _lock = new Object();

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
	 * Per-session in-memory state: the chat entries and the AI's conversation memory.
	 */
	static class SessionState {
		final Chat _chat;

		ChatMemory _chatMemory;

		SessionState(Chat chat, ChatMemory chatMemory) {
			_chat = chat;
			_chatMemory = chatMemory;
		}
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
	 * Returns the active session's {@link ChatMemory}, or creates a transient one if no session is
	 * active.
	 */
	public ChatMemory getActiveChatMemory() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._chatMemory == null) {
					state._chatMemory = MessageWindowChatMemory.withMaxMessages(CHAT_MEMORY_MAX_MESSAGES);
				}
				return state._chatMemory;
			}
			return MessageWindowChatMemory.withMaxMessages(CHAT_MEMORY_MAX_MESSAGES);
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

			// Check LRU cache first
			SessionState cached = _sessions.get(sessionId);
			if (cached != null) {
				_activeSessionId = sessionId;
				return;
			}

			// Load from database
			Chat chat = new Chat();
			ChatMemory memory = MessageWindowChatMemory.withMaxMessages(CHAT_MEMORY_MAX_MESSAGES);

			try {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session != null) {
					String json = ChatHistoryService.getMessagesJson(session);
					if (json != null && !json.isEmpty()) {
						List<ChatEntry> entries = DtoMapper.deserializeEntries(json);
						chat.loadEntries(entries);
						rebuildChatMemory(memory, entries);
					}
				}
			} catch (Exception ex) {
				Logger.error("Failed to load session " + sessionId + ": " + ex.getMessage(), ex,
					ChatSessionManager.class);
			}

			_sessions.put(sessionId, new SessionState(chat, memory));
			_activeSessionId = sessionId;
		}
	}

	/**
	 * Persists the active session's full state (messages + task snapshots) to the database.
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
				String json = DtoMapper.serializeEntries(state._chat.getEntries());
				ChatHistoryService.saveMessages(session, json);
			}
		} catch (Exception ex) {
			Logger.error("Failed to persist session " + sessionId + ": " + ex.getMessage(), ex,
				ChatSessionManager.class);
		}
	}

	/**
	 * Rebuilds a {@link ChatMemory} from persisted chat entries by replaying user and agent
	 * messages.
	 */
	private static void rebuildChatMemory(ChatMemory memory, List<ChatEntry> entries) {
		for (ChatEntry entry : entries) {
			if (entry instanceof ChatEntry.MessageEntry me) {
				ChatMessage msg = me.message();
				String role = msg.getUser();
				if ("Agent".equals(role) || "AI".equals(role)) {
					memory.add(new AiMessage(msg.getText()));
				} else if (!"System".equals(role)) {
					memory.add(new UserMessage(msg.getText()));
				}
			}
		}
	}
}
