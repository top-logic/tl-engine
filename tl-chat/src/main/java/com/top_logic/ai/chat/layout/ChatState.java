/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.ai.chat.api.messages.MessageFeedbackDto;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

/**
 * What one chat holds in the memory of one window: its messages, the model's memory and the log
 * of its turns, the agent's plan and tool catalog, and the settings its metadata stores.
 *
 * <p>
 * Everything here belongs to the chat, not to the window that shows it. A run captures the state of
 * its own chat when it is sent and reads and writes only that, so a window that switches to another
 * chat while the run works hands the run nothing of the other chat (#844). A window that comes back
 * to a chat it still holds finds the chat as it left it.
 * </p>
 *
 * @see ChatSessionManager#getActiveChatState()
 */
public class ChatState {

	private final String _sessionId;

	private final Person _owner;

	private final Chat _chat;

	private final ConversationMemory _memory;

	private final ConversationLog _log;

	private final TaskStore _taskStore;

	/**
	 * The tool catalog of the chat's last run (typically a {@code DynamicToolCatalog}), so that the
	 * tools a skill loaded survive between messages.
	 */
	private volatile ToolRegistry _toolRegistry;

	private volatile ApprovalMode _approvalMode = ApprovalMode.MANUAL;

	private final Set<String> _approvedTools = ConcurrentHashMap.newKeySet();

	private final Set<String> _loadedSkills = Collections.synchronizedSet(new LinkedHashSet<>());

	/** Answer ratings, keyed by the zero-based index of the turn they belong to. */
	private final Map<Integer, MessageFeedbackDto> _feedback = new ConcurrentHashMap<>();

	private volatile String _effort;

	private volatile TokenUsage _cumulativeUsage = TokenUsage.EMPTY;

	/**
	 * Creates a {@link ChatState}.
	 *
	 * @param sessionId
	 *        The chat's id; <code>null</code> for the state of a window that shows no chat.
	 * @param owner
	 *        The account the chat was opened for; <code>null</code> when there is no chat.
	 */
	ChatState(String sessionId, Person owner, Chat chat, ConversationMemory memory, ConversationLog log,
			TaskStore taskStore) {
		_sessionId = sessionId;
		_owner = owner;
		_chat = chat;
		_memory = memory;
		_log = log;
		_taskStore = taskStore;
	}

	/** The chat's id, or <code>null</code> for the state of a window that shows no chat. */
	public String sessionId() {
		return _sessionId;
	}

	/** The account the chat was opened for, or <code>null</code> when there is no chat. */
	public Person owner() {
		return _owner;
	}

	/** The chat's messages. */
	public Chat chat() {
		return _chat;
	}

	/** What the model remembers of the chat. */
	public ConversationMemory memory() {
		return _memory;
	}

	/** The chat's turns, as they are recorded. */
	public ConversationLog log() {
		return _log;
	}

	/** The agent's plan in this chat. */
	public TaskStore taskStore() {
		return _taskStore;
	}

	/** The tool catalog of the chat's last run, or <code>null</code> if none was kept. */
	public ToolRegistry toolRegistry() {
		return _toolRegistry;
	}

	/**
	 * @see #toolRegistry()
	 */
	public void setToolRegistry(ToolRegistry registry) {
		_toolRegistry = registry;
	}

	/** Whether the tools of a run in this chat need the user's approval. */
	public ApprovalMode approvalMode() {
		return _approvalMode;
	}

	/**
	 * @see #approvalMode()
	 */
	public void setApprovalMode(ApprovalMode mode) {
		_approvalMode = mode;
	}

	/** Adds a tool the user approved for the rest of this chat. */
	public void addApprovedTool(String toolName) {
		_approvedTools.add(toolName);
	}

	/** The tools the user approved for the rest of this chat. */
	public Set<String> approvedTools() {
		return Set.copyOf(_approvedTools);
	}

	/** Replaces the tools approved in this chat, e.g. with what its metadata stores. */
	public void loadApprovedTools(Set<String> tools) {
		_approvedTools.clear();
		_approvedTools.addAll(tools);
	}

	/**
	 * The skills loaded in this chat, in the order they were loaded.
	 *
	 * <p>
	 * Ordered on purpose: this is what the next run restores its tool catalog from, and a catalog
	 * restored in another order sends the same tools as different bytes - a prompt cache thrown away
	 * for nothing (Gitea #346).
	 * </p>
	 */
	public Set<String> loadedSkills() {
		synchronized (_loadedSkills) {
			return Collections.unmodifiableSet(new LinkedHashSet<>(_loadedSkills));
		}
	}

	/** Adds a skill to the ones loaded in this chat. */
	public void addLoadedSkill(String skillId) {
		_loadedSkills.add(skillId);
	}

	/** Replaces the skills loaded in this chat. */
	public void loadLoadedSkills(Set<String> skills) {
		synchronized (_loadedSkills) {
			_loadedSkills.clear();
			_loadedSkills.addAll(skills);
		}
	}

	/**
	 * Records, changes or withdraws the rating of one answer.
	 *
	 * @param turnIndex
	 *        Zero-based index of the turn whose answer was rated.
	 * @param rating
	 *        {@code "up"} or {@code "down"}; anything else withdraws an earlier rating.
	 * @param note
	 *        Free text the user added, or {@code null}.
	 */
	public void setFeedback(int turnIndex, String rating, String note) {
		if (!"up".equals(rating) && !"down".equals(rating)) {
			_feedback.remove(Integer.valueOf(turnIndex));
			return;
		}
		_feedback.put(Integer.valueOf(turnIndex), MessageFeedbackDto.create()
			.setTurnIndex(turnIndex)
			.setRating(rating)
			.setNote(note == null ? "" : note)
			.setRecordedAt(DateTimeFormatter.ISO_INSTANT.format(Instant.now())));
	}

	/** Every rating of this chat, in turn order. */
	public List<MessageFeedbackDto> feedback() {
		List<MessageFeedbackDto> all = new ArrayList<>(_feedback.values());
		all.sort(Comparator.comparingInt(MessageFeedbackDto::getTurnIndex));
		return all;
	}

	/** Replaces the ratings of this chat, e.g. with what its metadata stores. */
	public void loadFeedback(List<MessageFeedbackDto> feedback) {
		_feedback.clear();
		if (feedback == null) {
			return;
		}
		for (MessageFeedbackDto entry : feedback) {
			_feedback.put(Integer.valueOf(entry.getTurnIndex()), entry);
		}
	}

	/**
	 * Drops the ratings of turns that no longer exist.
	 *
	 * @param turnCount
	 *        How many turns the chat has left; ratings at or after this index are removed.
	 */
	public void dropFeedbackFrom(int turnCount) {
		_feedback.keySet().removeIf(index -> index.intValue() >= turnCount);
	}

	/** The effort level last chosen in this chat, or {@code null} when none was. */
	public String effort() {
		return _effort;
	}

	/**
	 * @see #effort()
	 */
	public void setEffort(String effort) {
		_effort = effort == null || effort.isBlank() ? null : effort.trim();
	}

	/** What the runs of this chat have spent together. */
	public TokenUsage cumulativeUsage() {
		return _cumulativeUsage;
	}

	/** Adds what one run of this chat spent. */
	public synchronized void addTokenUsage(TokenUsage usage) {
		_cumulativeUsage = _cumulativeUsage.add(usage);
	}
}
