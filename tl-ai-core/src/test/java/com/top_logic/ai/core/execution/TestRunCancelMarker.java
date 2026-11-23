/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.execution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.tool.api.CancellationToken;
import com.top_logic.ai.tool.api.Tool;
import com.top_logic.ai.tool.api.ToolMode;
import com.top_logic.ai.tool.api.ToolResult;
import com.top_logic.ai.tool.api.ToolSchema;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.ai.core.agent.AgentLoop;
import com.top_logic.ai.core.agent.AgentParams;
import com.top_logic.ai.core.agent.ExecutionMode;
import com.top_logic.ai.core.agent.SecurityContext;
import com.top_logic.ai.core.agent.StopReason;
import com.top_logic.ai.core.agent.ToolContext;
import com.top_logic.ai.core.agent.TransactionScope;
import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.execution.AgentExecutionStore.ExecutionStatus;
import com.top_logic.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.ai.core.execution.model.AgentExecution;
import com.top_logic.ai.core.execution.model.AgentSession;
import com.top_logic.ai.core.message.ContentPart;
import com.top_logic.ai.core.message.Conversation;
import com.top_logic.ai.core.message.TextContent;
import com.top_logic.ai.core.message.TokenUsage;
import com.top_logic.ai.core.message.ToolCall;
import com.top_logic.ai.core.message.UserMessage;
import com.top_logic.ai.core.provider.LLMProvider;
import com.top_logic.ai.core.provider.ScriptedModel;
import com.top_logic.ai.core.provider.ScriptedModel.Reply;
import com.top_logic.ai.core.provider.ScriptedModel.Script;
import com.top_logic.ai.core.session.ConversationLog;
import com.top_logic.ai.core.tools.ToolCatalog;
import com.top_logic.ai.core.tools.ToolStubs;
import com.top_logic.util.model.ModelService;

/**
 * A run that {@link AgentRunner} records stops at the cancel marker of its execution, whichever
 * entry point started it (#30483).
 *
 * <p>
 * The run is a pre-built loop, as a background subagent is. Its tool sets the marker, as
 * {@code aiAgentRunCancel} or the stop command of the agent activity monitor does from another
 * request. The run reads the marker at the start of its next turn, calls no tool any more and ends
 * as {@code CANCELLED}.
 * </p>
 */
public class TestRunCancelMarker extends BasicTestCase {

	private static final String MODEL = "test-model";

	/** A run of a pre-built loop, with no observer of its caller, stops at its marker. */
	public void testLoopRunStopsAtItsMarker() {
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session = store.createSession(SessionKind.CHAT, null, "cancel-marker-loop");
		CancellationToken token = new CancellationToken();
		ToolContext context = new ToolContext(developmentSecurity(), ApprovalCallback.AUTO_APPROVE,
			TransactionScope.NOOP, token);

		AtomicInteger calls = new AtomicInteger();
		ToolCatalog tools = new ToolCatalog();
		tools.register(ToolStubs.builder("work").description("").schema(new ToolSchema("work", "work", "{}"))
			.mode(ToolMode.READ).execute((args, tool) -> {
				if (calls.incrementAndGet() == 1) {
					store.requestCancel(context.executionHandle().get());
				}
				return ToolResult.of("Worked.");
			}).build());
		tools.freeze();
		LLMProvider provider = scripted(
			List.of(new ToolCall("call-1", "work", "{}")),
			List.of(new ToolCall("call-2", "work", "{}")),
			List.of(new TextContent("Done.")));
		AgentLoop loop = new AgentLoop(new AgentParams(MODEL, "You are a test assistant.",
			AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, tools);

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", Instant.now(), List.of(new TextContent("Work."))));
		AgentRunner.RecordedRun run =
			AgentRunner.run(loop, "worker", conversation, context, null, new ConversationLog(), session, null);

		assertTrue("The run did not see its cancel marker.", token.isCancelled());
		assertEquals("A tool was called after the cancel marker was set.", 1, calls.get());
		assertEquals(StopReason.ABORTED, run.result().stopReason());
		HistoryUtils.updateSessionRevision();
		List<AgentExecution> executions = store.executionsOf(session);
		assertEquals(1, executions.size());
		assertEquals(ExecutionStatus.CANCELLED.name(), executions.get(0).getStatus().getName());
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

	/** A model that gives the given answers in order, one per turn. */
	@SafeVarargs
	private static LLMProvider scripted(List<ContentPart>... responses) {
		List<Reply> replies = new ArrayList<>();
		for (List<ContentPart> response : responses) {
			replies.add(Reply.of(response).withUsage(new TokenUsage(100, 50, 0, 0, 0)));
		}
		return new ScriptedModel(Script.of(replies));
	}

	/**
	 * The suite, on a knowledge base with the execution store running.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunCancelMarker.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
