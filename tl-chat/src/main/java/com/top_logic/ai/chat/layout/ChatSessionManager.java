/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistries;
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

	/** Back-reference to the owning tracker, the window a chat's subagents report to. */
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

	/** Whether the window this manager belongs to has ended; see {@link #close()}. */
	private boolean _closed;

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
	 * The state of the window when it shows no chat; created on first use.
	 *
	 * <p>
	 * What a run without a chat - a scheduled task that records none - is sent with, and where a
	 * setting made before any chat was opened is kept.
	 * </p>
	 */
	private ChatState _noChat;

	/**
	 * The state of one chat this manager holds, with what its window listens to for it.
	 */
	static class SessionState extends ChatState {

		/** Borrowed from {@link SubagentRegistries}; see {@link #getActiveSubagentRegistry()}. */
		SubagentJobRegistry _subagentRegistry;

		/** What {@link #_subagentRegistry} is borrowed under while this state listens to it. */
		SubagentRegistries.Lease _subagentLease;

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
		SessionState(String sessionId, Person owner, ConversationMemory memory, ConversationLog log,
				TaskStore taskStore) {
			super(sessionId, owner, new Chat(), memory, log, taskStore);
		}
	}

	/**
	 * Sets the owning {@link TaskProgressTracker}.
	 */
	void setTracker(TaskProgressTracker tracker) {
		_tracker = tracker;
	}

	/**
	 * The state of the chat this window shows, or the window's own state when it shows none.
	 *
	 * <p>
	 * One object, read in one step: a run captures it when it is sent, and reads and writes it to its
	 * end, whatever chat the window shows by then (#844).
	 * </p>
	 */
	public ChatState getActiveChatState() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				return state;
			}
			if (_noChat == null) {
				_noChat = new ChatState(null, null, new Chat(), createDefaultMemory(), new ConversationLog(),
					new TaskStore());
			}
			return _noChat;
		}
	}

	/**
	 * The state this window holds of the given chat, or <code>null</code> if it holds none.
	 *
	 * <p>
	 * Unlike {@link #getActiveChatState()}, also for a chat the window does not show any more.
	 * </p>
	 */
	public ChatState getChatState(String sessionId) {
		if (sessionId == null) {
			return null;
		}
		synchronized (_lock) {
			return _sessions.get(sessionId);
		}
	}

	/**
	 * The state of the given chat of the given account, to act on without showing it.
	 *
	 * <p>
	 * What a request that names a chat works on - rating an answer, dropping turns - when it has no
	 * reason to switch the chat this window shows. The state this window holds of the chat, when it
	 * holds one that is current; otherwise one read from the database, which this manager does not
	 * keep: putting it in the cache could push out a chat the window holds, and a chat that a run of
	 * another window has written to is read afresh by the next switch to it anyway.
	 * </p>
	 *
	 * @param sessionId
	 *        The chat; the caller has established that it is the given account's.
	 * @param owner
	 *        The account acting.
	 * @return The chat's state, or <code>null</code> when no chat is named.
	 */
	public ChatState chatStateFor(String sessionId, Person owner) {
		if (sessionId == null) {
			return null;
		}
		synchronized (_lock) {
			SessionState held = _sessions.get(sessionId);
			if (held != null && owner != null && owner.equals(held.owner())
				&& !_changedElsewhere.contains(sessionId)) {
				return held;
			}
			return loadFromDatabase(sessionId, owner);
		}
	}

	/**
	 * Returns the active session's {@link Chat}.
	 *
	 * @see #getActiveChatState()
	 */
	public Chat getActiveChat() {
		return getActiveChatState().chat();
	}

	/**
	 * Returns the active session's {@link ConversationMemory}.
	 *
	 * @see #getActiveChatState()
	 */
	public ConversationMemory getActiveConversationMemory() {
		return getActiveChatState().memory();
	}

	/**
	 * Returns the active session's {@link ConversationLog}.
	 *
	 * @see #getActiveChatState()
	 */
	public ConversationLog getOrCreateConversationLog() {
		return getActiveChatState().log();
	}

	/**
	 * Returns the active session's {@link TaskStore}.
	 *
	 * @see #getActiveChatState()
	 */
	public TaskStore getActiveTaskStore() {
		return getActiveChatState().taskStore();
	}

	/**
	 * Returns the active session's {@link ToolRegistry}, or {@code null} if none has been set.
	 *
	 * @see #getActiveChatState()
	 */
	public ToolRegistry getActiveToolRegistry() {
		return getActiveChatState().toolRegistry();
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
				attachSubagents(state);
			}
			return registry;
		}
	}

	/**
	 * The {@link SubagentJobRegistry} of the given chat in this window, creating one on first use.
	 *
	 * <p>
	 * What a run of that chat spawns into and reads its subagents' news from - also when the window
	 * has switched to another chat since the run was started. For the active chat this is
	 * {@link #getActiveSubagentRegistry()}; another chat's registry is listened to again when the
	 * window shows that chat again.
	 * </p>
	 *
	 * @param sessionId
	 *        The chat; <code>null</code> means the active one.
	 */
	public SubagentJobRegistry getSubagentRegistry(String sessionId) {
		synchronized (_lock) {
			if (sessionId == null || sessionId.equals(_activeSessionId)) {
				return getActiveSubagentRegistry();
			}
			return SubagentRegistries.forKey(subagentRegistryKey(sessionId));
		}
	}

	/**
	 * What a run of the given chat writes into, if that chat is the one this window shows.
	 *
	 * <p>
	 * The check and the capture are one step under the lock that a session switch takes, so a switch
	 * in between cannot hand over the state of the chat the window switched to.
	 * </p>
	 *
	 * @return The chat's state, or <code>null</code> if the window does not show it.
	 */
	ChatState openChat(String sessionId) {
		synchronized (_lock) {
			if (sessionId == null || !sessionId.equals(_activeSessionId)) {
				return null;
			}
			return _sessions.get(sessionId);
		}
	}

	/**
	 * Listens to the given registry for the session's state: the register that lets any window of the
	 * account find and answer what a subagent asks, and the waker that starts a lead turn when there is
	 * news - which, for a registry that worked on while nobody listened, may already be there.
	 */
	private void attachSubagents(SessionState state) {
		Person owner = _activeOwner;
		TaskProgressTracker tracker = _tracker;
		if (owner == null || tracker == null || _closed) {
			// Not marked as attached: the next call tries again, rather than never. A closed window
			// is not attached again by a run of it that ends afterwards: nothing would detach it.
			return;
		}
		// Attached rather than merely asked for: a registry nobody listens to any more is forgotten
		// after a while, and this window is listening.
		state._subagentLease = SubagentRegistries.attach(subagentRegistryKey(_activeSessionId));
		SubagentJobRegistry registry = state._subagentLease.registry();
		state._subagentRegistry = registry;
		// Before the window's own listener: that one is removed with the window, while a subagent it
		// registered works on and must still be dropped when it ends.
		PendingInteractionRegistry.forgetEndedOf(registry);
		state._subagentListeners.add(
			PendingInteractionRegistry.subagentListener(owner, tracker, _activeSessionId));
		state._subagentWaker = new SubagentWaker(registry, ChatProcessor.wakeHost(tracker, owner, _activeSessionId));
		state._subagentListeners.add(state._subagentWaker);
		for (SubagentJobRegistry.Listener listener : state._subagentListeners) {
			registry.addListener(listener);
		}
		state._subagentWaker.leadRunEnded();
	}

	/**
	 * Listens again to the subagents the active chat started in this window before its state was
	 * dropped or reloaded. Must be called under {@link #_lock}, after the chat became the active one.
	 *
	 * <p>
	 * At once, not on the next use of {@link #getActiveSubagentRegistry()}: a subagent that finishes
	 * or asks its lead before the window sends anything would otherwise wake nobody. A chat that never
	 * started a subagent in this window gets no registry here.
	 * </p>
	 */
	private void reattachSubagents(SessionState state) {
		if (state._subagentRegistry != null) {
			return;
		}
		if (SubagentRegistries.find(subagentRegistryKey(_activeSessionId)) != null) {
			attachSubagents(state);
		}
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
		if (state._subagentLease != null) {
			state._subagentLease.close();
			state._subagentLease = null;
		}
	}

	/**
	 * Stops listening to the subagents of every chat this manager holds, because the window it
	 * belongs to has ended.
	 *
	 * <p>
	 * The registries are the process's and outlive the window; what this window hung on them - its
	 * waker, the register of its questions - holds the window's whole state, and would keep it in
	 * memory for as long as the registry lives. The subagents work on; a registry nobody listens to is
	 * forgotten after a while (see {@link SubagentRegistries}).
	 * </p>
	 */
	public void close() {
		synchronized (_lock) {
			_closed = true;
			for (SessionState state : _sessions.values()) {
				detachSubagents(state);
			}
		}
	}

	/** The key of the given chat's registry in this window; see {@link SubagentRegistries}. */
	private String subagentRegistryKey(String sessionId) {
		return SubagentRegistries.windowKey(sessionId, _windowKey);
	}

	/**
	 * The background subagent of the given chat with the given id, whichever window's run started it.
	 *
	 * <p>
	 * Subagent ids are unique within a chat, over all its windows; the id of the lead's tool call that
	 * started it, when given, makes sure a card finds the subagent it shows and no other. Does not check
	 * who asks: the caller makes sure the chat is theirs.
	 * </p>
	 *
	 * @param sessionId
	 *        The chat; <code>null</code> finds nothing.
	 * @param subagentId
	 *        The subagent's id within its chat, e.g. {@code sa-2}.
	 * @param parentToolCallId
	 *        The id of the call that started it; empty takes the first subagent with that id.
	 * @return The subagent's job, or <code>null</code> if no window holds one.
	 */
	public static SubagentJob findSubagent(String sessionId, String subagentId, String parentToolCallId) {
		if (sessionId == null || subagentId == null) {
			return null;
		}
		boolean anyCall = parentToolCallId == null || parentToolCallId.isEmpty();
		for (SubagentJobRegistry registry : SubagentRegistries.ofConversation(sessionId)) {
			SubagentJob job = registry.get(subagentId);
			if (job != null && (anyCall || parentToolCallId.equals(job.parentToolCallId()))) {
				return job;
			}
		}
		return null;
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
	 * The memories the given conversation was opened with, read from the store on first use and
	 * kept for the rest of the conversation.
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
	 * @param sessionId
	 *        The conversation; one this window does not hold reads afresh and keeps nothing.
	 * @param owner
	 *        The account to read for; {@code null} yields an empty list.
	 */
	public List<UserMemories.Entry> getMemories(String sessionId, Person owner) {
		SessionState state;
		synchronized (_lock) {
			state = sessionId != null ? _sessions.get(sessionId) : null;
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
		TLObject incoming = ChatHistoryService.findOwnedSessionById(sessionId, owner);
		if (incoming == null) {
			Logger.warn("Refused to activate chat session " + sessionId + ": not "
				+ (owner == null ? "anybody's" : "this user's") + ".", ChatSessionManager.class);
			return false;
		}
		PersistJob outgoing;
		synchronized (_lock) {
			if (sessionId.equals(_activeSessionId)) {
				_activeOwner = owner;
				if (_changedElsewhere.remove(sessionId)) {
					// What the session's metadata says is read afresh with it.
					disposeSessionState(_sessions.remove(sessionId));
					SessionState reloaded = loadFromDatabase(sessionId, owner);
					_sessions.put(sessionId, reloaded);
					reattachSubagents(reloaded);
				}
				return true;
			}

			// Snapshot the outgoing session's metadata under the lock; the actual KB write
			// happens after the lock is released (see persistOutsideLock).
			outgoing = preparePersist();

			// Check LRU cache first. A cached chat brings its settings back with it: they are its
			// own, not the window's.
			SessionState cached = _sessions.get(sessionId);
			if (cached != null && _changedElsewhere.remove(sessionId)) {
				disposeSessionState(_sessions.remove(sessionId));
				cached = null;
			}
			if (cached != null) {
				_activeSessionId = sessionId;
				_activeOwner = owner;
			} else {
				SessionState newState = loadFromDatabase(sessionId, owner);
				_sessions.put(sessionId, newState);
				_activeSessionId = sessionId;
				_activeOwner = owner;
				reattachSubagents(newState);
			}
		}
		persistOutsideLock(outgoing);
		// The window shows the chat from now on: a subagent's result that ended before is no news.
		ChatHistoryService.markSeen(incoming);
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
			if (state != null && state.log() != current) {
				_changedElsewhere.add(sessionId);
			}
		}
	}

	/**
	 * Builds the state of a session from what the database holds. Must be called under
	 * {@link #_lock}.
	 */
	private SessionState loadFromDatabase(String sessionId, Person owner) {
		ConversationMemory memory = createDefaultMemory();
		ConversationLog convLog = new ConversationLog();
		String metadataJson = null;

		try {
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, owner);
			if (session != null) {
				String json = ChatHistoryService.loadConversationJson(session);
				if (json != null && !json.isEmpty()) {
					convLog = ConversationLogSerializer.fromJson(json);
					InterruptedTranscript.endInterruptedSubagents(convLog, PROCESS_START);
					ConversationLogReplay.rebuildMemory(convLog, memory.conversation());
				}
				metadataJson = ChatHistoryService.getMetadataJson(session);
			}
		} catch (Exception ex) {
			Logger.error("Failed to load session " + sessionId + ": " + ex.getMessage(), ex,
				ChatSessionManager.class);
		}

		SessionState newState =
			new SessionState(sessionId, owner, memory, convLog, DtoMapper.loadTaskStore(metadataJson));
		DtoMapper.loadMetadata(metadataJson, newState);
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
			// Its subagents were stopped by the deletion itself, in every window - see
			// AgentExecutionStore.deleteSession. This window only stops listening to them.
			disposeSessionState(state);
			if (wasActive) {
				_activeSessionId = null;
				_activeOwner = null;
				// Nothing of the deleted chat carries over to what the window shows next.
				_noChat = null;
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
	 * Persists the metadata of the given chat, whether or not this window still shows it.
	 *
	 * <p>
	 * What a run writes when it ends: the state of its own chat, never the one the window has
	 * switched to meanwhile (#844).
	 * </p>
	 *
	 * @param chat
	 *        The chat's state; one without a chat is a no-op.
	 */
	public void persist(ChatState chat) {
		if (chat == null || chat.sessionId() == null) {
			return;
		}
		persistOutsideLock(new PersistJob(chat.sessionId(), DtoMapper.serializeMetadata(chat), chat.owner()));
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
		SessionState state = _sessions.get(sessionId);
		if (state == null) {
			return null;
		}
		return new PersistJob(sessionId, DtoMapper.serializeMetadata(state), _activeOwner);
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
			if (session != null && job.metadataJson() != null) {
				// The approval mode is the one value the switch alone writes (#658).
				ChatHistoryService.updateMetadata(session,
					stored -> DtoMapper.keepStoredApprovalMode(stored, job.metadataJson()));
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
