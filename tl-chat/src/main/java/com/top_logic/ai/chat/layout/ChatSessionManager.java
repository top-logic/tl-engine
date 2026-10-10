/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.session.persist.InterruptedTranscript;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.memory.CompactionStrategies;
import com.top_logic.tl.ai.core.memory.CompactionStrategy;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.agent.environment.UserMemories;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

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

	/** Before this, no subagent of this process can have started; see {@link InterruptedTranscript}. */
	private static final Instant PROCESS_START =
		Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());

	/**
	 * Fallback context window budget for chat session memory, used only when no default model
	 * is configured. The budget is a placeholder until the first agent run: the agent runner
	 * re-aligns the memory with the effective per-agent context window (assignment override >
	 * model catalog) before each run.
	 */
	private static final int DEFAULT_CONTEXT_WINDOW_TOKENS = 128_000;

	private final Object _lock = new Object();

	/**
	 * Tells this window's subagent registries apart from another window's for the same chat: each
	 * window keeps its own conversation, and a lead turn reads the results of its own subagents.
	 */
	private final String _windowKey = UUID.randomUUID().toString();

	/** Back-reference to the owning tracker for metadata serialization. */
	private volatile TaskProgressTracker _tracker;

	/** LRU cache of per-session state, bounded to {@link #MAX_CACHED_SESSIONS}. */
	/**
	 * The cached sessions a run of another window has written a turn into since they were loaded.
	 *
	 * @see #markChangedElsewhere(String)
	 */
	private final Set<String> _changedElsewhere = ConcurrentHashMap.newKeySet();

	private final Map<String, SessionState> _sessions = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, SessionState> eldest) {
			boolean evict = size() > MAX_CACHED_SESSIONS;
			if (evict) {
				disposeSessionState(eldest.getValue());
			}
			return evict;
		}
	};

	/** The currently active session ID, or null if none. */
	private volatile String _activeSessionId;

	/**
	 * The person the active session was established to belong to, or {@code null} when none is
	 * active.
	 *
	 * <p>
	 * Kept so that the writes this manager performs later - and on another thread - are aimed at
	 * the same owner the activation was granted for, rather than at whatever ID happens to be in
	 * {@link #_activeSessionId}.
	 * </p>
	 */
	private volatile Person _activeOwner;

	/**
	 * Per-session in-memory state: the chat messages and the AI's conversation memory.
	 */
	static class SessionState {
		final Chat _chat;

		ConversationMemory _conversationMemory;

		ConversationLog _conversationLog;

		TaskStore _taskStore;

		/**
		 * Session-scoped tool registry (typically a {@link DynamicToolCatalog}).
		 * Persists across agent runs so dynamically loaded tools survive between messages.
		 */
		ToolRegistry _toolRegistry;

		/** Borrowed from {@link SubagentRegistries}; see {@link #getActiveSubagentRegistry()}. */
		SubagentJobRegistry _subagentRegistry;

		/** What listens to {@link #_subagentRegistry} for this state. */
		final List<SubagentJobRegistry.Listener> _subagentListeners = new ArrayList<>();

		/** Starts a lead turn for the news of {@link #_subagentRegistry}; created with it. */
		SubagentWaker _subagentWaker;

		/**
		 * The memories the agent was opened with, read once when this conversation's first message
		 * is sent.
		 *
		 * <p>
		 * {@code null} until then, so "not read yet" and "read, and there were none" stay different
		 * answers - see {@link UserMemoryBinding}.
		 * </p>
		 */
		List<UserMemories.Entry> _memories;

		/** */
		SessionState(Chat chat, ConversationMemory conversationMemory) {
			_chat = chat;
			_conversationMemory = conversationMemory;
			_taskStore = new TaskStore();
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
	 * Returns the active session's {@link TaskStore}. Returns a transient empty store if no
	 * session is active.
	 */
	public TaskStore getActiveTaskStore() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._taskStore == null) {
					state._taskStore = new TaskStore();
				}
				return state._taskStore;
			}
			return new TaskStore();
		}
	}

	/**
	 * Returns the active session's {@link ToolRegistry}, or {@code null} if none has been set.
	 */
	public ToolRegistry getActiveToolRegistry() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			return state != null ? state._toolRegistry : null;
		}
	}

	/**
	 * Sets the active session's {@link ToolRegistry}.
	 */
	public void setActiveToolRegistry(ToolRegistry registry) {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				state._toolRegistry = registry;
			}
		}
	}

	/**
	 * Returns the active session's {@link SubagentJobRegistry}, creating one on first use.
	 * Returns {@code null} if no session is active.
	 *
	 * <p>
	 * The registry is borrowed from {@link SubagentRegistries}, under a key of this chat in this
	 * window, rather than owned by the session's state: the state is dropped from the cache, or
	 * reloaded when another window wrote to the chat, and that must not stop the subagents at work.
	 * Coming back to the chat finds them again, and what they finished meanwhile.
	 * </p>
	 */
	public SubagentJobRegistry getActiveSubagentRegistry() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state == null) {
				return null;
			}
			SubagentJobRegistry registry = SubagentRegistries.forKey(subagentRegistryKey(_activeSessionId));
			if (state._subagentRegistry != registry) {
				detachSubagents(state);
				attachSubagents(state, registry);
			}
			return registry;
		}
	}

	/**
	 * Listens to the given registry for the session's state: the register that lets any window of the
	 * account find and answer what a subagent asks, and the waker that starts a lead turn when there is
	 * news - which, for a registry that worked on while nobody listened, may already be there.
	 */
	private void attachSubagents(SessionState state, SubagentJobRegistry registry) {
		state._subagentRegistry = registry;
		Person owner = _activeOwner;
		TaskProgressTracker tracker = _tracker;
		if (owner == null || tracker == null) {
			return;
		}
		state._subagentListeners.add(
			PendingInteractionRegistry.subagentListener(owner, tracker, _activeSessionId));
		state._subagentWaker = new SubagentWaker(registry, ChatProcessor.wakeHost(tracker, owner, _activeSessionId));
		state._subagentListeners.add(state._subagentWaker);
		for (SubagentJobRegistry.Listener listener : state._subagentListeners) {
			registry.addListener(listener);
		}
		state._subagentWaker.leadRunEnded();
	}

	/** Stops listening to the session's registry; its subagents work on. */
	private static void detachSubagents(SessionState state) {
		if (state._subagentWaker != null) {
			state._subagentWaker.close();
			state._subagentWaker = null;
		}
		if (state._subagentRegistry != null) {
			for (SubagentJobRegistry.Listener listener : state._subagentListeners) {
				state._subagentRegistry.removeListener(listener);
			}
		}
		state._subagentListeners.clear();
		state._subagentRegistry = null;
	}

	/** The key of the given chat's registry in this window; see {@link SubagentRegistries}. */
	private String subagentRegistryKey(String sessionId) {
		return "chat:" + sessionId + "@" + _windowKey;
	}

	/**
	 * The waker of the given chat's subagents, or <code>null</code> when that chat has started none in
	 * this window.
	 */
	SubagentWaker getSubagentWaker(String sessionId) {
		if (sessionId == null) {
			return null;
		}
		synchronized (_lock) {
			SessionState state = _sessions.get(sessionId);
			return state == null ? null : state._subagentWaker;
		}
	}

	/**
	 * The memories this conversation was opened with, read from the store on first use and kept for
	 * the rest of the conversation.
	 *
	 * <p>
	 * Read once and not once per turn, because they go into the environment block of the system
	 * prompt, which has to stay byte-identical across the turns of one conversation or the
	 * providers' prompt caches miss on every one of them. A memory written during the conversation
	 * therefore shows up in the next one - which is also the only place it is needed, the agent
	 * having just written it.
	 * </p>
	 *
	 * <p>
	 * A conversation whose in-memory state was dropped (a reload, an eviction from the LRU cache)
	 * reads afresh. The prompt cache is cold by then anyway.
	 * </p>
	 *
	 * @param owner
	 *        The account to read for; {@code null} yields an empty list.
	 */
	public List<UserMemories.Entry> getActiveMemories(Person owner) {
		SessionState state;
		synchronized (_lock) {
			state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null && state._memories != null) {
				return state._memories;
			}
		}
		// Read outside the lock: it reaches the database, and this lock also guards the session
		// switch a second window may be doing at the same moment.
		List<UserMemories.Entry> memories = UserMemories.load(owner);
		synchronized (_lock) {
			if (state != null && state._memories == null) {
				state._memories = memories;
			}
		}
		return memories;
	}

	private static void disposeSessionState(SessionState state) {
		if (state != null) {
			detachSubagents(state);
		}
	}

	/**
	 * Returns the active session ID, or null if none.
	 */
	public String getActiveSessionId() {
		return _activeSessionId;
	}

	/**
	 * The person the active session belongs to, or {@code null} when no session is active.
	 *
	 * <p>
	 * Established by {@link #switchSession(String, Person)} and not by the caller asking, so a
	 * later write can be aimed at the owner the activation was granted for.
	 * </p>
	 */
	public Person getActiveOwner() {
		return _activeOwner;
	}

	/**
	 * Switches the active session. Persists the current session first, then loads the target session
	 * from the database (or LRU cache) and rebuilds the AI's conversation memory.
	 *
	 * <p>
	 * Loading a conversation into this manager is what puts it in front of a user - it becomes the
	 * context the model is given and the transcript the next turn is written into - so the session
	 * has to be established as this person's here, not only in whichever handler called. A handler
	 * that forgets to ask gets a refused switch and keeps the session it had, instead of the
	 * caller's chat quietly becoming someone else's.
	 * </p>
	 *
	 * @param sessionId
	 *        The session ID to switch to.
	 * @param owner
	 *        The person acting; the session must be theirs. {@code null} owns nothing.
	 * @return whether the session is now the active one.
	 */
	public boolean switchSession(String sessionId, Person owner) {
		if (sessionId == null) {
			return false;
		}
		if (sessionId.equals(_activeSessionId) && owner != null && owner.equals(_activeOwner)
			&& !_changedElsewhere.contains(sessionId)) {
			return true;
		}
		if (ChatHistoryService.findOwnedSessionById(sessionId, owner) == null) {
			Logger.warn("Refused to activate chat session " + sessionId + ": not "
				+ (owner == null ? "anybody's" : "this user's") + ".", ChatSessionManager.class);
			return false;
		}
		PersistJob outgoing;
		synchronized (_lock) {
			if (sessionId.equals(_activeSessionId)) {
				_activeOwner = owner;
				if (_changedElsewhere.remove(sessionId)) {
					// What the session's metadata says is read afresh as well, so the tracker's
					// state is reset first, as a switch does.
					if (_tracker != null) {
						_tracker.resetSessionState();
					}
					disposeSessionState(_sessions.remove(sessionId));
					_sessions.put(sessionId, loadFromDatabase(sessionId, owner));
				}
				return true;
			}

			// Snapshot the outgoing session's metadata under the lock; the actual KB write
			// happens after the lock is released (see persistOutsideLock).
			outgoing = preparePersist();

			// Reset tracker state immediately so it is never retained from the previous session,
			// regardless of whether the new session is found in the LRU cache or loaded from DB.
			if (_tracker != null) {
				_tracker.resetSessionState();
			}

			// Check LRU cache first
			SessionState cached = _sessions.get(sessionId);
			if (cached != null && _changedElsewhere.remove(sessionId)) {
				disposeSessionState(_sessions.remove(sessionId));
				cached = null;
			}
			if (cached != null) {
				_activeSessionId = sessionId;
				_activeOwner = owner;
				return true;
			}

			SessionState newState = loadFromDatabase(sessionId, owner);
			_sessions.put(sessionId, newState);
			_activeSessionId = sessionId;
			_activeOwner = owner;
		}
		persistOutsideLock(outgoing);
		return true;
	}

	/**
	 * Notes that a session this manager holds was written to by a run of another window.
	 *
	 * <p>
	 * A manager caches the conversation it loaded, and a run another window of the same account
	 * started in that conversation writes its turn to the database, not into this cache. When that
	 * run ends, this window's copy is a turn short: reading the chat back showed the conversation as
	 * it was before the run, and the next message sent from here would have been answered by a model
	 * that had never seen that turn (issue #544). The run's own window does not need this - the turn
	 * was written into its cache while it ran.
	 * </p>
	 *
	 * <p>
	 * The session is read back by the next {@link #switchSession(String, Person) switch} to it -
	 * which every request of the window naming it makes - and not here: this is called from the
	 * run's thread, whose view of the database predates the turn the run has just recorded.
	 * </p>
	 *
	 * @param sessionId
	 *        The session whose turn was written elsewhere; <code>null</code> and a session this
	 *        manager does not hold are a no-op.
	 */
	public void markChangedElsewhere(String sessionId) {
		if (sessionId == null) {
			return;
		}
		synchronized (_lock) {
			if (_sessions.containsKey(sessionId)) {
				_changedElsewhere.add(sessionId);
			}
		}
	}

	/**
	 * Notes that a session this manager holds has changed in the database, unless the copy held is
	 * the given log - the one the change was made in.
	 *
	 * @param sessionId
	 *        The changed session; <code>null</code> and a session this manager does not hold are a
	 *        no-op.
	 * @param current
	 *        The log that already carries the change.
	 * @see #markChangedElsewhere(String)
	 */
	public void markChangedElsewhere(String sessionId, ConversationLog current) {
		if (sessionId == null) {
			return;
		}
		synchronized (_lock) {
			SessionState state = _sessions.get(sessionId);
			if (state != null && state._conversationLog != current) {
				_changedElsewhere.add(sessionId);
			}
		}
	}

	/**
	 * Builds the state of a session from what the database holds. Must be called under
	 * {@link #_lock}.
	 */
	private SessionState loadFromDatabase(String sessionId, Person owner) {
		Chat chat = new Chat();
		ConversationMemory memory = createDefaultMemory();
		ConversationLog convLog = new ConversationLog();
		TaskStore loadedTaskStore = new TaskStore();

		try {
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, owner);
			if (session != null) {
				String json = ChatHistoryService.loadConversationJson(session);
				if (json != null && !json.isEmpty()) {
					convLog = ConversationLogSerializer.fromJson(json);
					InterruptedTranscript.endInterruptedSubagents(convLog, PROCESS_START);
					ConversationLogReplay.rebuildMemory(convLog, memory.conversation());
				}

				if (_tracker != null) {
					String metadataJson = ChatHistoryService.getMetadataJson(session);
					DtoMapper.loadMetadata(metadataJson, _tracker);
					loadedTaskStore = DtoMapper.loadTaskStore(metadataJson);
				}
			}
		} catch (Exception ex) {
			Logger.error("Failed to load session " + sessionId + ": " + ex.getMessage(), ex,
				ChatSessionManager.class);
		}

		SessionState newState = new SessionState(chat, memory);
		newState._conversationLog = convLog;
		newState._taskStore = loadedTaskStore;
		return newState;
	}

	/**
	 * Drops everything this manager still holds for a session that no longer exists.
	 *
	 * <p>
	 * A deleted chat must not stay the active one: the next message would be written into a session
	 * that is gone, and the cached {@link Chat} would keep showing a conversation the user threw
	 * away. Nothing is persisted on the way out - there is nothing left to persist it into.
	 * </p>
	 *
	 * @param sessionId
	 *        The deleted session's id; unknown ids and {@code null} are a no-op.
	 * @return Whether this manager held that session.
	 */
	public boolean forgetSession(String sessionId) {
		if (sessionId == null || sessionId.isEmpty()) {
			return false;
		}
		synchronized (_lock) {
			SessionState state = _sessions.remove(sessionId);
			_changedElsewhere.remove(sessionId);
			boolean wasActive = sessionId.equals(_activeSessionId);
			if (state == null && !wasActive) {
				return false;
			}
			disposeSessionState(state);
			// The chat is gone, and with it the conversation its subagents would report to.
			int cancelled = SubagentRegistries.remove(subagentRegistryKey(sessionId));
			if (cancelled > 0) {
				Logger.info("Cancelled " + cancelled + " subagent(s) of deleted chat " + sessionId + ".",
					ChatSessionManager.class);
			}
			if (wasActive) {
				_activeSessionId = null;
				_activeOwner = null;
				if (_tracker != null) {
					_tracker.resetSessionState();
				}
			}
			return true;
		}
	}

	/**
	 * Persists the active session's metadata to the database.
	 *
	 * <p>
	 * The conversation itself is persisted per run by the agent runner (one transcript per
	 * execution); only the session-level metadata bag (approved tools, approval mode, tasks,
	 * loaded skills) is written here.
	 * </p>
	 */
	public void persistActiveSession() {
		PersistJob job;
		synchronized (_lock) {
			job = preparePersist();
		}
		persistOutsideLock(job);
	}

	/**
	 * Snapshot of what to persist, taken under {@link #_lock}.
	 *
	 * <p>
	 * Carries the owner the session was activated for, so the write that happens later - outside
	 * the lock, possibly on a background thread - resolves the session the same guarded way the
	 * activation did.
	 * </p>
	 */
	private record PersistJob(String sessionId, String metadataJson, Person owner) {}

	/**
	 * Computes the active session's persistable metadata. Must be called under {@link #_lock}.
	 */
	private PersistJob preparePersist() {
		String sessionId = _activeSessionId;
		if (sessionId == null) {
			return null;
		}
		if (_sessions.get(sessionId) == null) {
			return null;
		}
		String metadataJson = _tracker != null ? DtoMapper.serializeMetadata(_tracker) : null;
		return new PersistJob(sessionId, metadataJson, _activeOwner);
	}

	/**
	 * Executes the KB write of a prepared snapshot. Must be called WITHOUT holding
	 * {@link #_lock}: the write blocks on the {@code AgentExecutionStore} writer thread, and a
	 * stalled commit must not freeze all session switching and chat state access behind the
	 * lock.
	 */
	private void persistOutsideLock(PersistJob job) {
		if (job == null) {
			return;
		}
		try {
			TLObject session = ChatHistoryService.findOwnedSessionById(job.sessionId(), job.owner());
			if (session != null) {
				ChatHistoryService.saveMetadata(session, job.metadataJson());
			}
		} catch (Exception ex) {
			Logger.error("Failed to persist session " + job.sessionId() + ": " + ex.getMessage(), ex,
				ChatSessionManager.class);
		}
	}

	/**
	 * Creates a default {@link ConversationMemory} with summarization strategy.
	 *
	 * <p>
	 * Uses the default model of {@link AgentRuntimeService} for summarization.
	 * Falls back to truncation if no default model is configured or if the registry is not
	 * available. The initial context window is a placeholder; the effective per-agent window
	 * is applied by the agent runner before each run.
	 * </p>
	 */
	private static ConversationMemory createDefaultMemory() {
		CompactionStrategy strategy = CompactionStrategies.forDefaultModel();
		int contextWindow = resolveContextWindow();
		return ConversationMemory.withDefaults(strategy, contextWindow);
	}

	private static int resolveContextWindow() {
		LLMProviderService registry = LLMProviderService.getInstance();
		AgentRuntimeService runtime = AgentRuntimeService.getInstance();
		if (registry == null || runtime == null) {
			return DEFAULT_CONTEXT_WINDOW_TOKENS;
		}
		String defaultModel = runtime.getDefaultModel();
		if (defaultModel == null || defaultModel.isBlank()) {
			return DEFAULT_CONTEXT_WINDOW_TOKENS;
		}
		ModelInfo info = registry.getModel(defaultModel);
		if (info == null) {
			return DEFAULT_CONTEXT_WINDOW_TOKENS;
		}
		return info.limits().contextWindow();
	}
}
