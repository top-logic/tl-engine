/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Instant;
import java.util.List;

import com.top_logic.tl.ai.core.agent.approval.InteractionAsker;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.citation.ReadingList;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.limits.BudgetAllowance;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Mutable per-job state for one asynchronous subagent run. Holds a live
 * {@link CancellationToken}, so it is a runtime object (not a serializable DTO).
 */
public class SubagentJob {

	private final String _subagentId;

	private final String _targetAgentName;

	private final String _taskBrief;

	private final Instant _startedAt;

	private final CancellationToken _cancellationToken;

	private final UserInteractionStore _interactionStore;

	private final SubagentProgress _progress = new SubagentProgress();

	private volatile ConversationLog _liveLog;

	private volatile String _parentToolCallId = "";

	private final SubagentInbox _inbox = new SubagentInbox();

	private volatile String _cancelReason;

	private volatile LeadQuestion _leadQuestion;

	private volatile Runnable _questionSignal = () -> {
		// Nobody listens.
	};

	private volatile SubagentJobStatus _status = SubagentJobStatus.QUEUED;

	private volatile SubagentResult _result;

	private volatile boolean _delivered;

	private volatile String _childRunId;

	private volatile Money _cost;

	private volatile BudgetAllowance _budget = BudgetAllowance.UNLIMITED;

	private volatile ReadingList _readingList = new ReadingList();

	private volatile List<SubagentInbox.Message> _unreadMessages = List.of();

	/** */
	public SubagentJob(String subagentId, String targetAgentName, String taskBrief,
			CancellationToken cancellationToken) {
		_subagentId = subagentId;
		_targetAgentName = targetAgentName;
		_taskBrief = taskBrief;
		_cancellationToken = cancellationToken;
		_startedAt = Instant.now();
		_interactionStore = new UserInteractionStore(new InteractionAsker(subagentId, targetAgentName));
	}

	/** Unique identifier of this subagent job within its session. */
	public String subagentId() {
		return _subagentId;
	}

	/** Name of the delegated-to agent definition. */
	public String targetAgentName() {
		return _targetAgentName;
	}

	/** The task brief the parent gave this subagent. */
	public String taskBrief() {
		return _taskBrief;
	}

	/** When the job was registered. */
	public Instant startedAt() {
		return _startedAt;
	}

	/** Token observed cooperatively by the child loop to support cancellation. */
	public CancellationToken cancellationToken() {
		return _cancellationToken;
	}

	/**
	 * The store this subagent's questions and approvals wait in.
	 *
	 * <p>
	 * Its own, and not the store of the turn that started it: that turn ends while the subagent works
	 * on, and its store is unbound with it - a question asked there afterwards was never shown and
	 * could not be answered. This one lives as long as the job, and names the subagent as the
	 * {@link UserInteractionStore#asker() asker} of everything in it.
	 * </p>
	 */
	public UserInteractionStore interactionStore() {
		return _interactionStore;
	}

	/**
	 * The id of the parent's tool call that started this subagent, so a UI can show it at that call;
	 * empty if unknown.
	 */
	public String parentToolCallId() {
		return _parentToolCallId;
	}

	/** Sets the {@link #parentToolCallId() parent's tool call id}. */
	public void setParentToolCallId(String parentToolCallId) {
		_parentToolCallId = parentToolCallId == null ? "" : parentToolCallId;
	}

	/** What the agent that started this subagent, or the user, sent it and it has not read yet. */
	public SubagentInbox inbox() {
		return _inbox;
	}

	/**
	 * Cancels this subagent on behalf of the agent that started it.
	 *
	 * <p>
	 * The same cancellation the user's Stop fires, plus the reason, which the result the lead is
	 * handed names. A subagent parked on a question ends its wait through the same token.
	 * </p>
	 *
	 * @param reason
	 *        Why the lead stopped it; may be <code>null</code>.
	 */
	public void cancel(String reason) {
		_cancelReason = reason;
		_cancellationToken.cancel();
	}

	/** Why the lead {@link #cancel(String) cancelled} this subagent, or <code>null</code>. */
	public String cancelReason() {
		return _cancelReason;
	}

	/**
	 * Asks the agent that started this subagent a question, and returns it for the asking thread to
	 * wait on.
	 *
	 * <p>
	 * One question at a time, because the asking thread waits for the answer; the registry is told, so
	 * that a lead waiting in {@code wait_for_subagent} returns to answer it.
	 * </p>
	 */
	public LeadQuestion askLead(String question) {
		LeadQuestion asked = new LeadQuestion(question);
		_leadQuestion = asked;
		_questionSignal.run();
		return asked;
	}

	/** The question this subagent waits for the lead to answer, or <code>null</code>. */
	public LeadQuestion leadQuestion() {
		return _leadQuestion;
	}

	/**
	 * Answers the question this subagent is waiting on.
	 *
	 * @return Whether a question was waiting; <code>false</code> when there is none to answer.
	 */
	public boolean answerLead(String answer) {
		LeadQuestion asked = _leadQuestion;
		return asked != null && asked.answer().complete(answer);
	}

	/** Forgets the question once the asking thread has stopped waiting for it. */
	public void clearLeadQuestion(LeadQuestion asked) {
		if (_leadQuestion == asked) {
			_leadQuestion = null;
		}
	}

	/** Sets what is told when this subagent asks its lead; see {@link #askLead(String)}. */
	void setQuestionSignal(Runnable signal) {
		_questionSignal = signal;
	}

	/** What the subagent has done so far; see {@link SubagentProgress}. */
	public SubagentProgress progress() {
		return _progress;
	}

	/** What a running subagent is parked on, while it waits for somebody. */
	public enum Waiting {
		/** A question to the agent that started it. */
		LEAD_ANSWER,
		/** A question to the user. */
		USER_ANSWER,
		/** An approval of a tool call. */
		APPROVAL;
	}

	/**
	 * What this subagent waits for, or <code>null</code> when it is not waiting - it works, has not
	 * started, or has ended.
	 *
	 * <p>
	 * Read off its {@link #interactionStore() store}, which holds everything it can be parked on, so
	 * the answer cannot fall behind what the child actually does.
	 * </p>
	 */
	public Waiting waitingFor() {
		if (_status != SubagentJobStatus.RUNNING) {
			return null;
		}
		LeadQuestion asked = _leadQuestion;
		if (asked != null && !asked.answer().isDone()) {
			return Waiting.LEAD_ANSWER;
		}
		if (_interactionStore.hasPendingApprovals()) {
			return Waiting.APPROVAL;
		}
		if (_interactionStore.hasPending()) {
			return Waiting.USER_ANSWER;
		}
		return null;
	}

	/**
	 * The conversation log the running subagent records into, or <code>null</code> when it has none
	 * (yet).
	 *
	 * <p>
	 * What its transcript is read from while it works; once it has ended, its recorded execution is,
	 * and the job lets go of the log.
	 * Written by the child's thread - a reader takes {@link ConversationLog#turns()}, which is a
	 * snapshot.
	 * </p>
	 */
	public ConversationLog liveLog() {
		return _liveLog;
	}

	/** Sets the {@link #liveLog() live log}. */
	public void setLiveLog(ConversationLog liveLog) {
		_liveLog = liveLog;
	}

	/** Current lifecycle state. */
	public SubagentJobStatus status() {
		return _status;
	}

	/** Terminal result envelope, or {@code null} while non-terminal. */
	public SubagentResult result() {
		return _result;
	}

	/** Whether the result has already been handed to the parent (injection or wait). */
	public boolean isDelivered() {
		return _delivered;
	}

	/**
	 * Run id of the child run, set as soon as the run starts; {@code null} before. Lets the UI open
	 * the subagent's transcript - its {@link #liveLog() live log} while it works, the recorded
	 * {@link com.top_logic.tl.ai.core.execution.model.AgentExecution} afterwards.
	 */
	public String childRunId() {
		return _childRunId;
	}

	/**
	 * What the subagent's run cost, as its run reported when it ended; <code>null</code> while it
	 * works or when its run reported none.
	 */
	public Money cost() {
		return _cost;
	}

	/** Sets the {@link #cost() cost} of the subagent's run. */
	public void setCost(Money cost) {
		_cost = cost;
	}

	/**
	 * The budget the subagent spends from: the allowance of the run that started it, which a
	 * background subagent shares (see {@code ToolContext.forDetachedSubagent}). Unlimited by default.
	 */
	public BudgetAllowance budget() {
		return _budget;
	}

	/** Sets the {@link #budget() budget} the subagent spends from. */
	public void setBudget(BudgetAllowance budget) {
		_budget = budget == null ? BudgetAllowance.UNLIMITED : budget;
	}

	/** Sets the {@link #childRunId() recorded child run id}. */
	public void setChildRunId(String childRunId) {
		_childRunId = childRunId;
	}

	/**
	 * What the subagent's run read.
	 *
	 * <p>
	 * Joins the reading list of the run its result is delivered to, so that answer may cite what the
	 * subagent read - see {@link com.top_logic.tl.ai.core.citation.ReadingList}.
	 * </p>
	 */
	public ReadingList readingList() {
		return _readingList;
	}

	/** Sets the {@link #readingList() reading list} of the subagent's run. */
	public void setReadingList(ReadingList readingList) {
		_readingList = readingList == null ? new ReadingList() : readingList;
	}

	/**
	 * The messages sent to this subagent that it did not read, because they arrived when its run had
	 * no turn left - it stopped at its limit, failed or was cancelled; empty while it works.
	 *
	 * <p>
	 * Its result names them, so that whoever sent one learns that it was not taken into account.
	 * </p>
	 */
	public List<SubagentInbox.Message> unreadMessages() {
		return _unreadMessages;
	}

	/** Sets the {@link #unreadMessages() messages this subagent did not read}. */
	void setUnreadMessages(List<SubagentInbox.Message> unread) {
		_unreadMessages = List.copyOf(unread);
	}

	/** Transition QUEUED -&gt; RUNNING. */
	public void markRunning() {
		_status = SubagentJobStatus.RUNNING;
	}

	/** Transition to a terminal state, storing the result envelope. */
	public void markTerminal(SubagentJobStatus terminal, SubagentResult result) {
		if (!terminal.isTerminal()) {
			throw new IllegalArgumentException("Not a terminal status: " + terminal);
		}
		_result = result;
		_status = terminal;
		// Read only while the subagent works; afterwards its recorded execution is.
		_liveLog = null;
	}

	/** Marks the result as delivered to the parent (idempotent). */
	public void markDelivered() {
		_delivered = true;
	}
}
