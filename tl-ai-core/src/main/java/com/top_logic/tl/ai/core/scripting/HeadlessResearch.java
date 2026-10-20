/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.environment.RunEnvironment;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.CancelSignalObserver;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.limits.RunLimitExceededException;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.research.ResearchReport;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * A web research run of Albert's {@code TL_deepResearch} skill that needs no chat window (#767).
 *
 * <p>
 * The run is started by TL-Script - {@code aiResearch}, also from a scheduled {@code ScriptTask} -
 * and works on a background thread as its owner. It is recorded as a <em>chat session</em> of the
 * owner, not as a background session, because the report is meant to be read: the session appears
 * in the owner's chat list, opens like any chat, and shows the report with its footnotes and the
 * cards of the workers. The new entry in the list is how the owner learns that the report is there.
 * The engine has no notification for an account that is not logged in, and mail is a module this
 * product does not depend on.
 * </p>
 *
 * <p>
 * The run is unattended: nobody can answer a question or an approval. {@code askUser} answers at once
 * that nobody is there, and the policy's background allowlist bounds the tools, as for every
 * run with nobody to ask. The account's token and money budget bound it like a chat run; a run the
 * budget refuses records the refusal as its answer, so the owner reads why there is no report.
 * </p>
 *
 * @see AIScriptFunctions#research(String, String, Object)
 */
public final class HeadlessResearch {

	/** The agent that carries the research skill. */
	static final String LEAD = AgentRuntimeService.DEFAULT_CHAT_ENTRY_POINT;

	/** The skill that tells the lead how to research. */
	static final String SKILL = "TL_deepResearch";

	/** The effort a research gets when the caller names none. */
	static final String DEFAULT_EFFORT = ResearchReport.STANDARD;

	/** How many characters of the question the session title shows. */
	private static final int TITLE_LENGTH = 60;

	private HeadlessResearch() {
		// Static utility.
	}

	/**
	 * Starts a research run and returns its handle.
	 *
	 * @param question
	 *        What to research; must not be blank.
	 * @param effort
	 *        {@code quick}, {@code standard} or {@code deep}; {@code null} for {@code standard}.
	 * @param owner
	 *        The account the run acts as and the report belongs to, as a {@link Person} or a login
	 *        name; {@code null} for the calling account.
	 * @return The session's persistent id, which {@code aiAgentRunStatus}, {@code aiAgentRunResult}
	 *         and the other handle functions read.
	 */
	public static String start(String question, String effort, Object owner) {
		if (question == null || question.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_RESEARCH_QUESTION_EMPTY);
		}
		String level = effortOf(effort);
		Person account = ownerOf(TLContext.currentUser(), owner);
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			throw new IllegalStateException("aiResearch needs the AgentExecutionStore service, which is not active.");
		}
		AgentSession session = AgentExecutionStore.getInstance()
			.createSession(SessionKind.CHAT, account, titleOf(question, account));
		String text = prompt(question.trim(), level);
		SchedulerService.getInstance().submit(() -> ThreadContextManager.inSystemInteraction(
			HeadlessResearch.class, () -> runAs(account, () -> run(account, text, session))));
		return KBUtils.getWrappedObjectName(session).toString();
	}

	/**
	 * The effort level the skill is told.
	 *
	 * @throws TopLogicException
	 *         If the value is not one of the skill's levels.
	 */
	static String effortOf(String effort) {
		if (effort == null || effort.isBlank()) {
			return DEFAULT_EFFORT;
		}
		String level = effort.trim().toLowerCase(Locale.ROOT);
		if (!ResearchReport.EFFORTS.contains(level)) {
			throw new TopLogicException(I18NConstants.ERROR_RESEARCH_EFFORT__VALUE.fill(effort));
		}
		return level;
	}

	/**
	 * The account a research run acts as.
	 *
	 * <p>
	 * The calling account when none is named. Another account can be named only where no account
	 * calls - a system context, which is what a scheduled {@code ScriptTask} runs in: a user must not
	 * start runs that spend someone else's budget and act with someone else's rights.
	 * </p>
	 *
	 * @param current
	 *        The calling account, {@code null} in a system context.
	 * @param requested
	 *        The account the caller named: a {@link Person}, a login name or {@code null}.
	 * @throws TopLogicException
	 *         If no account can be determined, the name is unknown, or a user names another account.
	 */
	static Person ownerOf(Person current, Object requested) {
		Person named = null;
		if (requested instanceof Person person) {
			named = person;
		} else if (requested instanceof String name && !name.isBlank()) {
			named = Person.byName(name.trim());
			if (named == null) {
				throw new TopLogicException(I18NConstants.ERROR_RESEARCH_UNKNOWN_OWNER__NAME.fill(name));
			}
		} else if (requested != null && !(requested instanceof String)) {
			throw new TopLogicException(I18NConstants.ERROR_RESEARCH_UNKNOWN_OWNER__NAME.fill(requested));
		}
		if (named == null) {
			if (current == null) {
				throw new TopLogicException(I18NConstants.ERROR_RESEARCH_NO_OWNER);
			}
			return current;
		}
		if (current != null && !current.equals(named)) {
			throw new TopLogicException(I18NConstants.ERROR_RESEARCH_OTHER_OWNER);
		}
		return named;
	}

	/**
	 * What the lead is told.
	 *
	 * <p>
	 * Names the skill, so that the run does not depend on the model choosing it, and the effort, which
	 * the skill takes from the user's own words. Says that nobody can answer, so that the lead does not
	 * plan a question it cannot ask. Model-facing, so English, whatever the owner's language - the
	 * skill writes the report in the owner's language, which the environment block states.
	 * </p>
	 */
	static String prompt(String question, String effort) {
		return "Research the following question with the skill " + SKILL + " and write the report.\n"
			+ "Effort: " + effort + ".\n"
			+ "This research runs without a chat window: nobody can answer a question. Do not ask."
			+ " Where the question is open, decide and state the assumption in the report.\n\n"
			+ "Question:\n" + question;
	}

	/** The title of the session: "Research: " and the start of the question, in the owner's language. */
	private static String titleOf(String question, Person owner) {
		String shortQuestion = question.trim().replaceAll("\\s+", " ");
		if (shortQuestion.length() > TITLE_LENGTH) {
			shortQuestion = shortQuestion.substring(0, TITLE_LENGTH - 1).trim() + "…";
		}
		Locale locale = RunEnvironment.localeOf(owner);
		return Resources.getInstance(locale).getString(I18NConstants.RESEARCH_SESSION_TITLE__QUESTION.fill(shortQuestion));
	}

	/**
	 * Runs the given work with the given account on this thread's sub-session, the way a scheduled
	 * agent task does: a system interaction carries no account, and the run's tools read it there.
	 */
	private static void runAs(Person account, Runnable work) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		Person previous = subSession.getPerson();
		subSession.setPerson(account);
		try {
			work.run();
		} finally {
			subSession.setPerson(previous);
		}
	}

	/** The background body: the lead's run, recorded under {@code session}. */
	private static void run(Person owner, String text, AgentSession session) {
		UserMessage request =
			new UserMessage(UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent(text)));
		Conversation conversation = new Conversation();
		conversation.add(request);

		CancellationToken cancellation = new CancellationToken();
		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(owner, RunEntryPoint.SCRIPT, true,
				ExecutionRecorder.conversationIdOf(session))),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellation), cancellation).unattended();
		// aiAgentRunCancel sets the persisted marker from any node; this node holds the token.
		CancelSignalObserver cancelObserver = new CancelSignalObserver(context.executionHandle(), cancellation);

		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			ConversationLog log = env.logListener().log();
			Turn turn = log.startTurn();
			turn.setUserParts(List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), text)));
			try {
				AgentRunner.run(LEAD, conversation, null, context, cancelObserver, null, log, session, null);
			} catch (RunLimitExceededException ex) {
				// Refused where every run enters: no model was asked. Said as the run's answer, in the
				// owner's language, because the owner looks for the report in the chat.
				String reason = Resources.getInstance(RunEnvironment.localeOf(owner)).getString(ex.reason());
				Logger.info("Research of '" + owner.getName() + "' not started: " + ex.getMessage(),
					HeadlessResearch.class);
				recordRefusal(session, log, turn, reason, ex.isTemporary() ? null : StopReason.BUDGET_EXHAUSTED);
			}
		} catch (Throwable ex) {
			if (context.executionHandle().get() == null) {
				recordFailure(session, ex);
			}
			Logger.error("Research run of '" + owner.getName() + "' failed: " + ex.getMessage(), ex,
				HeadlessResearch.class);
		} finally {
			context.transaction().close();
		}
	}

	/**
	 * Records a run that a limit refused before it started: the question and the refusal as its
	 * answer, so the session reads like a chat that was answered with the reason.
	 *
	 * @param stopReason
	 *        {@link StopReason#BUDGET_EXHAUSTED} for a spent budget, {@code null} for a ceiling that
	 *        frees up again shortly.
	 */
	private static void recordRefusal(AgentSession session, ConversationLog log, Turn turn, String reason,
			StopReason stopReason) {
		turn.addAssistantPart(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), reason));
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentExecution execution = store.beginExecution(session, UUID.randomUUID().toString(), LEAD, null, null, null);
		store.finishExecution(execution, ExecutionStatus.REJECTED, stopReason, reason, null,
			ConversationLogSerializer.toJson(log), reason);
	}

	/** Records a run that failed before the agent loop could record it, so the handle reports it. */
	private static void recordFailure(AgentSession session, Throwable ex) {
		try {
			AgentExecutionStore store = AgentExecutionStore.getInstance();
			AgentExecution execution =
				store.beginExecution(session, UUID.randomUUID().toString(), LEAD, null, null, null);
			String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
			store.finishExecution(execution, ExecutionStatus.ERROR, StopReason.ERROR, message, null, null);
		} catch (RuntimeException recordEx) {
			Logger.error("Could not record the failure of a research run: " + recordEx.getMessage(), recordEx,
				HeadlessResearch.class);
		}
	}
}
