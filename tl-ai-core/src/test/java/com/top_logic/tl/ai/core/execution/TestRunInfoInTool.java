/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * What a tool is told about the run it executes in, driven through a real {@link AgentLoop}
 * (Gitea #405).
 *
 * <p>
 * The revision half of #405 - that the run's persistent {@code AgentExecution} cannot be
 * dereferenced from a tool - needs a booted {@code KnowledgeBase} and lives in
 * {@link TestRunInfoRevisionSafety}. This covers the other half: that the facts reach every tool of
 * a run, including the tools of a concurrent batch, which run on worker threads of their own.
 * </p>
 */
public class TestRunInfoInTool extends TestCase {

	private static final Instant T0 = Instant.parse("2026-09-19T10:00:00Z");

	private static final TokenUsage USAGE = new TokenUsage(10, 5, 0, 0, 0);

	private static final String MODEL = "test-model";

	/** Names of the threads the probe tools ran on, to tell a concurrent batch from a serial one. */
	private static final List<String> THREADS = new CopyOnWriteArrayList<>();

	/**
	 * A run publishes its identity to its tools before the first one of them executes, without any
	 * execution recorder involved - which is what makes the facts visible to a test, the gap that
	 * let #405 pass 506 of them.
	 */
	public void testToolLearnsRunIdAndModelWithoutARecorder() {
		List<RunInfo> seen = new CopyOnWriteArrayList<>();

		runWithTools(seen, List.of(new ToolCall("tc1", "probe_a", "{}")));

		assertEquals("the tool ran once", 1, seen.size());
		RunInfo run = seen.get(0);
		assertTrue("the tool knows it is in a run", run.isKnown());
		assertNotNull("the run id the loop generated", run.runId());
		assertEquals("the model this run talks to", MODEL, run.model());
		assertNull("no recorder ran, so there is no execution row to name", run.executionId());
	}

	/**
	 * The tools of a concurrent READ batch run on worker threads of a shared pool. Each must see the
	 * same run - the facts travel on the context, which every copy shares, and are bound on the
	 * thread the tool actually runs on.
	 */
	public void testConcurrentBatchSeesOneRun() {
		List<RunInfo> seen = new CopyOnWriteArrayList<>();
		List<RunInfo> bound = new CopyOnWriteArrayList<>();
		THREADS.clear();

		runWithTools(seen, bound,
			List.of(new ToolCall("tc1", "probe_a", "{}"), new ToolCall("tc2", "probe_b", "{}")));

		assertEquals("both tools of the batch ran", 2, seen.size());
		// Two READ tools form one concurrent batch, which AgentLoop dispatches onto its worker
		// pool - a thread that inherits none of the caller's thread-locals. A serial call would
		// have run on this test's own thread instead. (The pool is a cached one, so the two calls
		// may or may not land on the same worker; what matters is that it was a worker at all.)
		assertEquals("both tools ran on the concurrent-batch worker pool", 2,
			THREADS.stream().filter(name -> name.startsWith("agent-tool-worker")).count());
		assertEquals("the batch ran as one run", seen.get(0).runId(), seen.get(1).runId());
		assertNotNull("...and that run is identified", seen.get(0).runId());
		assertEquals("the thread-bound view agrees with the context, on every worker thread",
			List.of(seen.get(0).runId(), seen.get(1).runId()),
			List.of(bound.get(0).runId(), bound.get(1).runId()));
	}

	/**
	 * A worker thread of the shared pool must not report the previous batch's run once its call is
	 * over: the binding is restored, not merely overwritten by the next task.
	 */
	public void testBindingDoesNotOutliveTheCall() {
		List<RunInfo> first = new CopyOnWriteArrayList<>();
		runWithTools(first, List.of(new ToolCall("tc1", "probe_a", "{}")));

		assertFalse("nothing is bound on this thread after the run",
			RunInfoBinding.current().isKnown());
	}

	/**
	 * A delegated run is its own run and names the one that delegated it, all the way up to the
	 * top-level run - as plain data, so a child tool can read the chain.
	 */
	public void testDelegatedRunSeesItselfAndItsParent() {
		ToolContext parent = context();
		parent.executionHandle().setRunId("run-parent");
		parent.executionHandle().setAgentName("orchestrator");
		parent.executionHandle().setConversationId("conv-1");

		ToolContext child = parent.forDelegation(new TransactionScope());
		child.executionHandle().setRunId("run-child");
		child.executionHandle().setAgentName("researcher");

		RunInfo run = child.runInfo();
		assertEquals("the child's own run", "run-child", run.runId());
		assertEquals("the child's own agent", "researcher", run.agentName());
		assertEquals("one level below the top", 1, run.delegationDepth());
		assertNotNull("the child names its parent", run.parent());
		assertEquals("the parent's run", "run-parent", run.parent().runId());
		assertEquals("the parent's agent", "orchestrator", run.parent().agentName());
		assertEquals("a child stays in the parent's conversation", "conv-1", run.conversationId());

		ToolContext grandchild = child.forDelegation(new TransactionScope());
		grandchild.executionHandle().setRunId("run-grandchild");
		assertEquals("two levels below the top", 2, grandchild.runInfo().delegationDepth());
		assertEquals("the chain leads back to the top-level run", "run-parent",
			grandchild.runInfo().root().runId());
	}

	/**
	 * A detached (fire-and-forget) subagent gets the same treatment: its own run, its parent named.
	 * It is the case where the parent's turn has already ended, so nothing can be read back from the
	 * parent later - the facts have to have travelled with the child's context.
	 */
	public void testDetachedSubagentSeesItselfAndItsParent() {
		ToolContext parent = context();
		parent.executionHandle().setRunId("run-parent");
		parent.executionHandle().setAgentName("orchestrator");

		ToolContext child =
			parent.forDetachedSubagent(new TransactionScope(), new CancellationToken());
		child.executionHandle().setRunId("run-subagent");
		child.executionHandle().setAgentName("explorer");

		assertEquals("the subagent's own run", "run-subagent", child.runInfo().runId());
		assertEquals("the delegating run", "run-parent", child.runInfo().parent().runId());
		assertEquals("one level below the top", 1, child.runInfo().delegationDepth());
	}

	/**
	 * Outside an agent run - a tool called from a script, a test, the MCP endpoint - every fact is
	 * absent and reading it is still safe. That is the contract that replaces the handle whose every
	 * use threw.
	 */
	public void testOutsideARunEverythingIsReadableAndEmpty() {
		ToolExecutionContext outside = new ToolExecutionContext() {
			@Override
			public com.top_logic.tl.ai.tool.api.ProgressReporter progressReporter() {
				return com.top_logic.tl.ai.tool.api.ProgressReporter.NOOP;
			}

			@Override
			public CancellationToken cancellationToken() {
				return CancellationToken.UNCANCELLABLE;
			}
		};

		RunInfo run = outside.runInfo();
		assertNotNull("never null", run);
		assertFalse("and it says so", run.isKnown());
		assertNull(run.runId());
		assertNull(run.agentName());
		assertNull(run.model());
		assertNull(run.executionId());
		assertSame("a run that is not a run is its own root", run, run.root());
	}

	private void runWithTools(List<RunInfo> seen, List<ToolCall> calls) {
		runWithTools(seen, new CopyOnWriteArrayList<>(), calls);
	}

	/**
	 * Runs a loop whose single turn issues {@code calls} and whose tools record what they are told
	 * about the run - from the context into {@code seen}, from the thread binding into
	 * {@code bound}.
	 */
	private void runWithTools(List<RunInfo> seen, List<RunInfo> bound, List<ToolCall> calls) {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", T0, List.of(new TextContent("Go"))));

		ToolCatalog registry = new ToolCatalog();
		for (ToolCall call : calls) {
			registry.register(probe(call.name(), seen, bound));
		}
		registry.freeze();

		List<ContentPart> toolTurn = new ArrayList<>(calls);
		AgentLoop loop = new AgentLoop(
			new AgentParams(MODEL, "system", 10, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS),
			mockProvider(toolTurn, List.of(new TextContent("Done."))), registry);

		loop.run(conversation, context(), (AgentEventListener) event -> {
			// The events are not what this test is about.
		});
	}

	private static ToolContext context() {
		CancellationToken cancellationToken = new CancellationToken();
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(cancellationToken), cancellationToken);
	}

	/**
	 * A READ tool - so a batch of them is dispatched concurrently - that records what it is told
	 * about its run.
	 */
	private static Tool probe(String name, List<RunInfo> seen, List<RunInfo> bound) {
		return new Tool() {
			@Override
			public String name() {
				return name;
			}

			@Override
			public String description() {
				return "Records the run it executes in.";
			}

			@Override
			public ToolMode mode() {
				return ToolMode.READ;
			}

			@Override
			public ToolSchema schema() {
				return new ToolSchema(name, "Records the run it executes in.", "{}");
			}

			@Override
			public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
				seen.add(context.runInfo());
				bound.add(RunInfoBinding.current());
				THREADS.add(Thread.currentThread().getName());
				return ToolResult.of("recorded");
			}
		};
	}

	private static LLMProvider mockProvider(List<ContentPart> first, List<ContentPart> second) {
		AtomicInteger callIndex = new AtomicInteger(0);
		List<List<ContentPart>> responses = List.of(first, second);

		return (request, handler) -> {
			int index = callIndex.getAndIncrement();
			if (index >= responses.size()) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
					"Mock provider: no more responses (call " + index + ")");
			}
			List<ContentPart> content = responses.get(index);
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				} else if (content.get(i) instanceof ToolCall call) {
					handler.onEvent(new StreamEvent.ToolCallStart(i, call.id(), call.name()));
					handler.onEvent(new StreamEvent.ToolCallDelta(i, call.arguments()));
				}
			}
			String finishReason =
				content.stream().anyMatch(ToolCall.class::isInstance) ? "tool_use" : "end_turn";
			handler.onEvent(
				new StreamEvent.MessageComplete("msg-" + index, MODEL, finishReason, USAGE, content));
		};
	}

	/**
	 * Boots the {@code ThreadContextManager} module, which {@code ToolExecutions#execute} and the
	 * concurrent batch dispatch both need to establish an interaction for a tool.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestRunInfoInTool.class)));
	}
}
