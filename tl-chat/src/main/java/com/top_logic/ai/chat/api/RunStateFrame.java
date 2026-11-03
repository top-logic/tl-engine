/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import com.top_logic.ai.chat.api.messages.InteractionOptionDto;
import com.top_logic.ai.chat.api.messages.PendingInteractionDto;
import com.top_logic.ai.chat.api.messages.RunStateEvent;
import com.top_logic.ai.chat.api.messages.SubagentBudgetDto;
import com.top_logic.ai.chat.api.messages.SubagentJobDto;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClassifier;
import com.top_logic.tl.ai.core.agent.approval.InteractionAsker;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.approval.PendingApproval;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.InteractionRequest;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.tl.ai.core.limits.BudgetAllowance;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

/**
 * Builds the {@code run_state} frame: the run of the chat a window shows, as the server sees it
 * now.
 *
 * <p>
 * The chat's stream replays nothing. What a window that attaches - a reloaded page, a second tab, a
 * stream attached again after a silence - or that opens another chat has to be told about the run
 * comes in this one frame: whether the agent is still working, its task list, the approvals and
 * questions waiting for the acting account, and the background subagents with the budget they
 * spend from. {@link ChatSSEHandler} sends it when it starts, and again whenever
 * {@link ChatSSEHandler#sendRunState()} is asked to.
 * </p>
 */
public final class RunStateFrame {

	/** The {@link #interactionTypeName(TLClassifier)} of a tool approval. */
	private static final String TYPE_APPROVAL = "approval";

	private RunStateFrame() {
		// Static helpers only.
	}

	/**
	 * The state of the run of the chat the given conversation of a window shows, as the given account
	 * sees it.
	 *
	 * <p>
	 * Building it also flushes what the conversation's run left behind into the window's
	 * {@link Chat} and forgets a run that has ended (see
	 * {@link ChatProcessor#applyPollUpdates(ChatProcessor.AIProcessingState, Chat, TaskProgressTracker)}),
	 * so that a run that ended with an error does not keep the conversation busy.
	 * </p>
	 *
	 * @param tracker
	 *        The conversation of the window.
	 * @param account
	 *        The acting account; <code>null</code> lists no interactions and finds no run of another
	 *        window.
	 */
	public static RunStateEvent of(TaskProgressTracker tracker, Person account) {
		ChatProcessor.AIProcessingState state = tracker.getAnyActiveProcessingState();
		if (state != null) {
			// Flush into the run's own captured chat, not the currently active one: with concurrent
			// runs a background run's messages must not leak into another session.
			Chat stateChat = state.getChat();
			ChatProcessor.applyPollUpdates(state, stateChat != null ? stateChat : tracker.getChat(), tracker);
		}
		String sessionId = tracker.getActiveSessionId();
		// Whether the chat is working is a fact about the chat, not about this window: the run may
		// have been started in a tab that is now closed, and a window that says the conversation is
		// idle offers a composer for a turn that is already under way and no way to stop it (#512).
		boolean processing = tracker.hasActiveProcessingStateFor(sessionId) || runInFlight(account, sessionId) != null;

		RunStateEvent event = RunStateEvent.create()
			.setSessionId(sessionId == null ? "" : sessionId)
			.setProcessing(processing);
		TaskStore tasks = tracker.getActiveTaskStore();
		if (tasks != null) {
			event.setTasksJson(tasks.toJson());
		}
		if (account != null && sessionId != null) {
			addInteractions(event, account, sessionId);
		}
		SubagentJobRegistry subagents = tracker.getActiveSubagentRegistry();
		if (subagents != null) {
			addSubagents(event, subagents);
		}
		return event;
	}

	/**
	 * The state of the run this account has in flight in the given chat, or <code>null</code>.
	 *
	 * <p>
	 * A run belongs to a conversation, not to the window it was started in, and everything about a
	 * turn that is still being worked on lives in memory until the run ends. Asked through
	 * {@link PendingInteractionRegistry}, so a second tab, a reloaded page and a new login find the
	 * run exactly as the window that started it does - and an account that has no run there, or is
	 * looking at somebody else's chat, finds nothing.
	 * </p>
	 *
	 * @param owner
	 *        The acting account; <code>null</code> finds nothing.
	 * @param sessionId
	 *        The id of the chat; <code>null</code> finds nothing.
	 */
	public static ChatProcessor.AIProcessingState runInFlight(Person owner, String sessionId) {
		PendingInteractionRegistry.PendingRun run = PendingInteractionRegistry.findBySession(owner, sessionId);
		if (run == null || !run.state().isProcessing()) {
			return null;
		}
		return run.state();
	}

	/**
	 * Adds the approvals and questions of the given chat that wait for the account.
	 *
	 * <p>
	 * A card rebuilt after a reload must offer what the agent asked. The persisted row does not hold
	 * everything the card is drawn from - an approval's tool mode, and whether a choice takes more
	 * than one answer - so those are read off the run that is still waiting, through
	 * {@link PendingInteractionRegistry} (#393, #466). Nothing about the shape is persisted: it
	 * matters only while a request is pending, and a request nobody is parked on can no longer be
	 * answered.
	 * </p>
	 */
	private static void addInteractions(RunStateEvent event, Person account, String sessionId) {
		TLClassifier chatKind = TlAiExecutionFactory.getCHATSessionKindClassifier();
		for (InteractionRequest request : AgentExecutionStore.getInstance().pendingInteractionsFor(account)) {
			AgentExecution execution = request.getExecution();
			AgentSession session = execution == null ? null : execution.getSession();
			// Only requests of this chat; runs of other kinds (script, background) are not shown in a
			// chat at all.
			if (session == null || !chatKind.equals(session.getKind()) || !sessionId.equals(objectId(session))) {
				continue;
			}
			String type = interactionTypeName(request.getType());
			// What the still-waiting run holds and the persisted row does not: an approval's tool
			// mode, and a question's shape.
			UserInteractionStore waiting = waitingStore(account, request.getRequestId());
			boolean approval = TYPE_APPROVAL.equals(type);
			UserInteractionRequest question = approval || waiting == null
				? null : waiting.pendingRequest(request.getRequestId());
			// A question nobody is parked on any more - the run ended, or this is the process after a
			// restart - is not offered: its shape is unknown, and an answer would reach nothing.
			if (!approval && question == null) {
				continue;
			}
			PendingInteractionDto dto = PendingInteractionDto.create()
				.setRequestId(request.getRequestId())
				.setType(type)
				.setHeader(request.getHeader())
				.setPrompt(request.getPrompt())
				.setPayload(request.getPayload())
				.setToolMode(toolMode(waiting, request.getRequestId()))
				.setSessionId(sessionId)
				.setSessionTitle(session.getTitle());
			InteractionAsker asker = waiting == null ? null : waiting.asker();
			if (asker != null) {
				dto.setAskedBySubagentId(asker.subagentId()).setAskedByAgent(asker.agentName());
			}
			if (question != null) {
				dto.setMultiSelect(question.isMultiSelect());
				for (InteractionOptionDto option : DtoMapper.parseInteractionOptions(question.getOptions())) {
					dto.addOption(option);
				}
			}
			Date createdAt = request.getCreatedAt();
			if (createdAt != null) {
				dto.setCreatedAt(formatIso(createdAt));
				dto.setRemainingMs(DtoMapper.remainingMs(createdAt.getTime(), responseDeadline(type)));
			}
			event.addInteraction(dto);
		}
	}

	/**
	 * Adds every subagent the registry knows and the budget they share.
	 */
	private static void addSubagents(RunStateEvent event, SubagentJobRegistry registry) {
		for (SubagentJob job : registry.byStatus(SubagentJobStatus.values())) {
			long tokens = job.result() != null ? job.result().tokenUsage().totalTokens() : 0;
			long durationMs = job.result() != null ? job.result().durationMs() : 0;
			// finalText doubles as the error summary for failed jobs; sending it lets the client
			// rebuild expandable finished rows after a reload, matching the live
			// subagent_completed/subagent_failed frames. Running jobs show their task.
			String detail = job.result() != null ? job.result().finalText() : job.taskBrief();
			// Lets the client offer a "view transcript" affordance only when the recorded child run is
			// available (GET /chat/subagents/{id}/transcript).
			boolean hasTranscript = job.childRunId() != null && !job.childRunId().isEmpty();
			event.addSubagent(withCost(SubagentJobDto.create()
				.setSubagentId(Objects.toString(job.subagentId(), ""))
				.setTargetAgentName(Objects.toString(job.targetAgentName(), ""))
				.setStatus(job.status().name())
				.setTotalTokens(tokens)
				.setDurationMs(durationMs)
				.setHasTranscript(hasTranscript)
				.setParentToolCallId(Objects.toString(job.parentToolCallId(), ""))
				.setTask(Objects.toString(job.taskBrief(), ""))
				.setChildRunId(Objects.toString(job.childRunId(), ""))
				.setStopReason(job.result() != null && job.result().stopReason() != null
					? job.result().stopReason().name() : "")
				.setDetail(Objects.toString(detail, "")), job.cost()));
		}
		event.setBudget(budgetOf(registry));
	}

	private static SubagentJobDto withCost(SubagentJobDto job, Money cost) {
		if (cost == null) {
			return job;
		}
		Money shown = CostConversion.shown(cost, null);
		return job.setCost(shown.amount()).setCurrency(shown.currency());
	}

	/**
	 * The budget the subagents of this list spend from: the allowance of the run that started them,
	 * which they share (#783). Its headroom when that run started, per kind; -1 for a kind it does
	 * not bound. <code>null</code> when no subagent's run is bounded.
	 */
	private static SubagentBudgetDto budgetOf(SubagentJobRegistry registry) {
		for (SubagentJob job : registry.byStatus(SubagentJobStatus.values())) {
			BudgetAllowance budget = job.budget();
			if (!budget.isLimited()) {
				continue;
			}
			BudgetAllowance.MoneyBudget money = budget.moneyBudget();
			return SubagentBudgetDto.create()
				.setTokens(budget.isTokenLimited() ? budget.tokenHeadroom() : -1)
				.setMoney(money != null ? money.headroom() : -1)
				.setCurrency(money != null ? money.currency() : "");
		}
		return null;
	}

	/**
	 * How long the responder has for a request of the given kind.
	 *
	 * <p>
	 * The deadline is not persisted with the request, so it is taken from the configuration the
	 * waiting agent thread is using.
	 * </p>
	 *
	 * @param type
	 *        Lowercased interaction type as sent to the client.
	 */
	private static Duration responseDeadline(String type) {
		return TYPE_APPROVAL.equals(type)
			? InteractionTimeoutService.approvalTimeout()
			: InteractionTimeoutService.questionTimeout();
	}

	/**
	 * The interaction store of the run or the background subagent still parked on the given
	 * request, or <code>null</code>.
	 *
	 * <p>
	 * Everything a persisted {@link InteractionRequest} row does not record - an approval's tool
	 * mode, a question's shape, the subagent that asks - lives here, and only for as long as the run
	 * is waiting, which is also the only time a client may still answer.
	 * </p>
	 *
	 * @param responder
	 *        The account the request is addressed to.
	 * @param requestId
	 *        The correlation id of the approval or question.
	 */
	private static UserInteractionStore waitingStore(Person responder, String requestId) {
		PendingInteractionRegistry.Waiting waiting = PendingInteractionRegistry.findWaiting(responder, requestId);
		return waiting == null ? null : waiting.store();
	}

	/**
	 * The mode of the tool a still-waiting approval is about, or the empty string.
	 *
	 * <p>
	 * The persisted request records the reason the security context wrote, in English; the mode is
	 * what lets the client phrase the question in the reader's language, exactly as it does for the
	 * card the live event drew.
	 * </p>
	 *
	 * @param store
	 *        The store of the run still waiting, as {@link #waitingStore(Person, String)} found it;
	 *        <code>null</code> when no run is.
	 * @param requestId
	 *        The correlation id of the approval.
	 */
	private static String toolMode(UserInteractionStore store, String requestId) {
		PendingApproval approval = store == null ? null : store.pendingApproval(requestId);
		if (approval == null || approval.request().mode() == null) {
			return "";
		}
		return approval.request().mode().name();
	}

	/** Lowercased classifier name ("approval"/"question"/"choice") of an interaction type. */
	private static String interactionTypeName(TLClassifier type) {
		return type == null ? null : type.getName().toLowerCase(Locale.ROOT);
	}

	private static String objectId(AgentSession session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	private static String formatIso(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(date);
	}
}
