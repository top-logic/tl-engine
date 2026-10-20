/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.util.model.ModelService;

/**
 * A run the process ends while it waits for an approval keeps its turn (#598).
 *
 * <p>
 * The whole path a restart takes, against a real knowledge base: an {@link AgentLoop} recorded
 * through {@link AgentRunner} reads something, says what it will do next and asks for an approval.
 * While it is parked on that approval, the test does what the next boot does - the
 * {@link AgentExecutionStore#failOrphanedRunningExecutions(Date) orphan sweep} - and reads back what
 * a reopened chat would be shown. The run's thread survives here, unlike in a real restart, but the
 * database cannot tell the difference: the checkpoint is all the sweep has, exactly as after a
 * crash.
 * </p>
 */
public class TestInterruptedRunTranscript extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final String QUESTION = "Lege einen Beleg 'Restart-Test' an.";

	/**
	 * Before the sweep, the row already holds the open turn as far as the user saw it; after it,
	 * the turn is closed as interrupted and nothing the user saw is gone.
	 */
	public void testTurnWaitingOnApprovalSurvivesRestart() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "interrupted-approval");

		ConversationLog log = new ConversationLog();
		Turn userTurn = log.startTurn();
		userTurn.setUserParts(List.of(new Part.TextPart("user-1", Instant.now(), QUESTION)));
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", Instant.now(), List.of(new TextContent(QUESTION))));

		LLMProvider provider = scripted(
			List.of(new TextContent("Ich sehe mir die Belege an."),
				new ToolCall("call-read", "lookup", "{}")),
			List.of(new TextContent("Jetzt lege ich den Beleg an."),
				new ToolCall("call-write", "create", "{}")),
			List.of(new TextContent("Gut, dann nicht.")));
		ToolCatalog tools = new ToolCatalog();
		tools.register(tool("lookup", ToolMode.READ));
		tools.register(tool("create", ToolMode.DATA_WRITE));
		tools.freeze();

		List<String> checkpoints = new ArrayList<>();
		List<String> swept = new ArrayList<>();
		List<String> status = new ArrayList<>();
		ApprovalCallback restartWhileWaiting = request -> {
			String runId = runIdOf(session);
			checkpoints.add(reread(runId).getTranscript());

			// What the next boot does. A cut-off in the future: every run of "this process" is old.
			store.failOrphanedRunningExecutions(new Date(System.currentTimeMillis() + 60_000));
			AgentExecution row = reread(runId);
			swept.add(row.getTranscript());
			status.add(row.getStatus().getName());
			return new ApprovalResult.Rejected("");
		};

		ToolContext context = new ToolContext(developmentSecurity(), restartWhileWaiting);
		AgentLoop loop = new AgentLoop(new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, tools);
		AgentRunner.run(loop, "orchestrator", conversation, context, event -> {
			// nothing to show
		}, log, session, null);

		assertEquals("The run must have been parked on the approval once", 1, swept.size());

		// The checkpoint: written while the run waited, so the turn is not lost with the process.
		Turn open = onlyTurn(checkpoints.get(0));
		assertEquals(QUESTION, userText(open));
		Part.ApprovalPart waiting = only(open, Part.ApprovalPart.class);
		assertNull("While the run waits, the approval is still open", waiting.decision());
		assertTrue("The text above the approval card was flushed before the run parked",
			texts(open).contains("Jetzt lege ich den Beleg an."));

		// After the sweep: what a reopened chat shows.
		assertEquals(ExecutionStatus.ERROR.name(), status.get(0));
		Turn closed = onlyTurn(swept.get(0));
		assertEquals("The user's own message must survive the restart", QUESTION, userText(closed));
		assertEquals(List.of("Ich sehe mir die Belege an.", "Jetzt lege ich den Beleg an."),
			texts(closed));
		Part.ToolCallPart read = only(closed, Part.ToolCallPart.class);
		assertEquals("A step the run finished stays finished",
			Part.ToolCallStatus.COMPLETED, read.status());
		assertEquals("Nobody can answer the approval any more, and the chat must not offer it",
			Part.ApprovalDecision.INTERRUPTED, only(closed, Part.ApprovalPart.class).decision());
		List<Part> parts = closed.assistantParts();
		Part last = parts.get(parts.size() - 1);
		assertTrue("The turn ends saying it was interrupted: " + last,
			last instanceof Part.SystemPart system
				&& system.eventType() == Part.SystemEventType.INTERRUPTED);
	}

	/**
	 * A subagent's part changes in the transcript of a run still going only through that run's own
	 * final transcript: its checkpoint is not parsed and rewritten per change. Once the run has ended,
	 * the change is written into its transcript.
	 */
	public void testTheSubagentPartOfAnOpenRunIsLeftToItsFinalTranscript() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "subagent-part-open-lead");
		AgentExecution lead = store.beginExecution(session, "lead-open-" + System.nanoTime(), "lead", MODEL,
			null, null);
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1", true, lead);
		String queued = ConversationLogSerializer.toJson(log.turns());
		store.checkpointTranscript(lead, () -> queued);

		listener.recordSubagentRunning("sa-1");
		assertEquals("The checkpoint of the open run is not rewritten.", Part.SubagentStatus.QUEUED,
			subagentIn(reread(lead.getRunId()).getTranscript()).status());

		store.finishExecutionWithLatestTranscript(lead, ExecutionStatus.COMPLETE, null, null, TokenUsage.EMPTY,
			null, () -> ConversationLogSerializer.toJson(log.turns()), null);
		assertEquals("The run's final transcript carries the part as it is now.", Part.SubagentStatus.RUNNING,
			subagentIn(reread(lead.getRunId()).getTranscript()).status());

		listener.recordSubagentCompleted("sa-1",
			new SubagentResult("X is 42.", TokenUsage.EMPTY, 10L, 1, StopReason.COMPLETE), "run-1");
		Part.SubagentPart ended = subagentIn(reread(lead.getRunId()).getTranscript());
		assertEquals("A subagent that outlives the run is written into its transcript.", Part.SubagentStatus.DONE, ended.status());
		assertEquals("run-1", ended.childRunId());
	}

	/**
	 * The process ends while the lead still runs and its subagent has moved on: the checkpoint that
	 * was not rewritten is closed by the next boot's sweep, and the part does not show the subagent
	 * waiting or at work for ever.
	 */
	public void testTheSubagentPartOfAnOpenRunIsClosedAfterARestart() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "subagent-part-restart");
		AgentExecution lead = store.beginExecution(session, "lead-cut-off-" + System.nanoTime(), "lead", MODEL,
			null, null);
		ConversationLog log = new ConversationLog();
		ConversationLogListener listener = new ConversationLogListener(log);
		log.startTurn();
		listener.recordSubagentStarted("sa-1", "researcher", "find X", "call-1", true, lead);
		String queued = ConversationLogSerializer.toJson(log.turns());
		store.checkpointTranscript(lead, () -> queued);
		listener.recordSubagentRunning("sa-1");

		// What the next boot does. A cut-off in the future: every run of "this process" is old.
		store.failOrphanedRunningExecutions(new Date(System.currentTimeMillis() + 60_000));

		Part.SubagentPart closed = subagentIn(reread(lead.getRunId()).getTranscript());
		assertEquals(Part.SubagentStatus.FAILED, closed.status());
	}

	private static Part.SubagentPart subagentIn(String transcript) {
		return only(onlyTurn(transcript), Part.SubagentPart.class);
	}

	private static String runIdOf(AgentSession session) {
		HistoryUtils.updateSessionRevision();
		List<AgentExecution> executions = AgentExecutionStore.getInstance().executionsOf(session);
		assertEquals(1, executions.size());
		return executions.get(0).getRunId();
	}

	/**
	 * The row of the given run, read at a revision that includes the writer thread's commits - the
	 * checkpoint is queued there without waiting, and the sweep is queued behind it.
	 */
	private static AgentExecution reread(String runId) {
		HistoryUtils.updateSessionRevision();
		return AgentExecutionStore.getInstance().findExecutionByRunId(runId);
	}

	private static Turn onlyTurn(String transcript) {
		assertNotNull("The row must hold a transcript", transcript);
		List<Turn> turns = ConversationLogSerializer.fromJson(transcript).turns();
		assertEquals(1, turns.size());
		return turns.get(0);
	}

	private static String userText(Turn turn) {
		return ((Part.TextPart) turn.userParts().get(0)).text();
	}

	private static List<String> texts(Turn turn) {
		return turn.assistantParts().stream()
			.filter(Part.TextPart.class::isInstance)
			.map(part -> ((Part.TextPart) part).text())
			.toList();
	}

	private static <P extends Part> P only(Turn turn, Class<P> type) {
		List<P> found = turn.assistantParts().stream().filter(type::isInstance).map(type::cast).toList();
		assertEquals("Exactly one " + type.getSimpleName() + ": " + turn.assistantParts(), 1, found.size());
		return found.get(0);
	}

	private static Tool tool(String name, ToolMode mode) {
		return ToolStubs.builder(name).description("").schema(new ToolSchema(name, name, "{}"))
			.mode(mode).result("Result of " + name).build();
	}

	private static SecurityContext developmentSecurity() {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		};
	}

	/**
	 * A provider that streams the given answers in order, text as deltas the way a real one does.
	 */
	@SafeVarargs
	private static LLMProvider scripted(List<ContentPart>... responses) {
		AtomicInteger next = new AtomicInteger();
		return (request, handler) -> {
			int index = next.getAndIncrement();
			if (index >= responses.length) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
					"No scripted answer " + index);
			}
			List<ContentPart> content = responses[index];
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				}
			}
			boolean calls = content.stream().anyMatch(ToolCall.class::isInstance);
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + index, MODEL,
				calls ? "tool_use" : "end_turn", new TokenUsage(100, 50, 0, 0, 0), content));
		};
	}

	/**
	 * The suite, on a knowledge base with the execution store running.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestInterruptedRunTranscript.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
