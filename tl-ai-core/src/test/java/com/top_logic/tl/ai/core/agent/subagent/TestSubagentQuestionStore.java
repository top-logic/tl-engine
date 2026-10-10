/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.InteractionAsker;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.interaction.AskUserTool;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.model.ModelService;

/**
 * A background subagent asks the user through its job's own store, which outlives the turn that
 * started it and names the subagent (issue #695).
 *
 * <p>
 * The child thread used to carry the starting turn's store over, so a question asked after that turn
 * ended waited where the chat no longer looked, and nobody could see who asked.
 * </p>
 */
public class TestSubagentQuestionStore extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private SubagentJobRegistry _registry;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(2);
	}

	@Override
	protected void tearDown() throws Exception {
		_registry.shutdown();
		super.tearDown();
	}

	/** The question waits in the job's store, names the child, and the answer lets it finish. */
	public void testAChildAsksThroughItsJobsStore() throws Exception {
		UserInteractionStore leadStore = new UserInteractionStore();
		SubagentJob job = spawnAskingChild(leadStore);

		UserInteractionRequest question = awaitQuestion(job);
		assertEquals(new InteractionAsker(job.subagentId(), "worker"), question.getAskedBy());
		assertFalse("Nothing may wait in the store of the turn that started the child.",
			leadStore.hasPending());

		job.interactionStore().resolve(question.getId(), "Nordics");

		awaitTerminal(job);
		assertEquals(SubagentJobStatus.DONE, job.status());
		assertEquals("done with Nordics", job.result().finalText());
	}

	/** Cancelling the child still ends its wait, as before. */
	public void testCancellingTheChildEndsTheWait() throws Exception {
		SubagentJob job = spawnAskingChild(new UserInteractionStore());
		awaitQuestion(job);

		_registry.cancelAll();

		awaitTerminal(job);
		assertEquals(SubagentJobStatus.CANCELLED, job.status());
		assertFalse(job.interactionStore().hasPending());
	}

	/** The listener hears the job start and end, once each. */
	public void testTheListenerHearsStartAndEnd() throws Exception {
		AtomicInteger started = new AtomicInteger();
		AtomicInteger finished = new AtomicInteger();
		_registry.setListener(new SubagentJobRegistry.Listener() {
			@Override
			public void started(SubagentJob job) {
				started.incrementAndGet();
			}

			@Override
			public void finished(SubagentJob job) {
				finished.incrementAndGet();
			}
		});
		SubagentJob job = spawnAskingChild(new UserInteractionStore());
		awaitQuestion(job);
		assertEquals(1, started.get());
		assertEquals(0, finished.get());

		_registry.cancelAll();
		awaitTerminal(job);
		long deadline = System.currentTimeMillis() + 5_000;
		while (finished.get() == 0 && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertEquals(1, finished.get());
	}

	/**
	 * Spawns a child that calls {@code askUser} and then answers with what it was told, from a
	 * "turn" whose store is the given one - which is then over.
	 */
	private SubagentJob spawnAskingChild(UserInteractionStore leadStore) {
		AtomicInteger calls = new AtomicInteger();
		LLMProvider provider = (request, handler) -> {
			int idx = calls.getAndIncrement();
			List<ContentPart> content;
			if (idx == 0) {
				content = List.of(new ToolCall("q1", "askUser",
					"{\"question\":\"Which region?\",\"header\":\"Clarify\",\"type\":\"text\"}"));
			} else if (idx == 1) {
				String answer = String.valueOf(request.messages());
				content = List.of(new TextContent(answer.contains("Nordics") ? "done with Nordics" : "done"));
			} else {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400, "no more");
			}
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				}
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + idx, MODEL, "end_turn", USAGE, content));
		};
		ToolCatalog tools = new ToolCatalog();
		tools.register(new AskUserTool());
		tools.freeze();
		AgentLoop child = new AgentLoop(new AgentParams(MODEL, "child", AgentParams.DEFAULT_MAX_TURNS,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, tools);
		DelegationTool spawn = new DelegationTool("delegate_to_worker", "worker", "worker", "lead",
			(name, security) -> child);

		CancellationToken token = new CancellationToken();
		ToolContext lead = new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(token), token);
		try (SubagentRegistryBinding.Binding registry = SubagentRegistryBinding.bind(_registry);
				UserInteractionStoreBinding.Binding store = UserInteractionStoreBinding.bind(leadStore)) {
			spawn.execute(Map.of("task", "research X"), lead);
		}
		return _registry.byStatus(SubagentJobStatus.values()).get(0);
	}

	private static UserInteractionRequest awaitQuestion(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (!job.interactionStore().hasPending() && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertTrue("The child's question must wait in its job's store.", job.interactionStore().hasPending());
		return job.interactionStore().getPendingRequests().get(0);
	}

	private static void awaitTerminal(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertTrue("The child must have ended.", job.status().isTerminal());
	}

	/** */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentQuestionStore.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
