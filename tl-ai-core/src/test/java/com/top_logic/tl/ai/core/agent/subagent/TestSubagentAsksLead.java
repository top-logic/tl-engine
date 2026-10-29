/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.citation.ReadingListBinding;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
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
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.AskLeadTool;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.ReplyToSubagentTool;
import com.top_logic.tl.ai.core.tools.interaction.WaitForSubagentTool;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;
import com.top_logic.util.model.ModelService;

/**
 * A background subagent asks the agent that started it and goes on with the answer (issue #707).
 *
 * <p>
 * The question reaches the lead at the start of its next turn, like a result, or at once when the lead
 * waits in {@code wait_for_subagent}; the lead answers with {@code reply_to_subagent}. Waking an idle
 * lead for a question is the wake's business (issue 4 of the series) and not tested here.
 * </p>
 */
public class TestSubagentAsksLead extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private static final String QUESTION = "Which region should I look at, Europe or Asia?";

	private SubagentJobRegistry _registry;

	private final CountDownLatch _release = new CountDownLatch(1);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(4);
	}

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		_registry.cancelAll();
		_registry.shutdown();
		super.tearDown();
	}

	/**
	 * The lead's next turn is shown the question, marked as data, answers it, and the child goes on
	 * with the answer in the same turn it asked in.
	 */
	public void testTheLeadAnswersAndTheChildGoesOn() throws Exception {
		List<String> childRequests = new CopyOnWriteArrayList<>();
		SubagentJob child = spawn(new AskLeadTool(), childRequests,
			() -> List.of(new ToolCall("q1", "ask_lead", "{\"question\":\"" + QUESTION + "\"}")),
			() -> List.of(new TextContent("Looked at the answer.")));
		awaitWaiting(child);
		assertEquals(SubagentJob.Waiting.LEAD_ANSWER, child.waitingFor());

		List<String> leadRequests = new CopyOnWriteArrayList<>();
		AgentResult lead = runLead(leadRequests,
			() -> List.of(new ToolCall("r1", "reply_to_subagent",
				"{\"subagentId\":\"" + child.subagentId() + "\",\"answer\":\"Europe only.\"}")),
			() -> List.of(new TextContent("I answered the worker.")));

		assertEquals(StopReason.COMPLETE, lead.stopReason());
		String asked = leadRequests.get(0);
		assertTrue("The lead's turn is shown the question: " + asked,
			asked.contains("Subagent " + child.subagentId() + " (worker) asks you") && asked.contains(QUESTION));
		String tag = UntrustedEnvelope.SUBAGENT_REPORT.tag();
		assertTrue("The question is marked as data: " + asked,
			asked.indexOf("<" + tag) < asked.indexOf(QUESTION) && asked.indexOf(QUESTION) < asked.indexOf("</" + tag));

		awaitTerminal(child);
		assertEquals(SubagentJobStatus.DONE, child.status());
		assertEquals("The child asked once and went on in the next request of the same run.", 2, childRequests.size());
		assertTrue(childRequests.get(1), childRequests.get(1).contains("Europe only."));
		assertNull(child.waitingFor());
	}

	/**
	 * An address in the lead's answer is an address the child was given, as one in a message of its
	 * lead is: the URL provenance rule lets the child open it.
	 */
	public void testAnAddressInTheLeadsAnswerIsGiven() throws Exception {
		String manual = "https://docs.example/manual";
		CompletableFuture<Boolean> given = new CompletableFuture<>();
		Tool probe = ToolStubs.builder("probe").execute((args, context) -> {
			given.complete(Boolean.valueOf(ReadingListBinding.current().seenUrls().contains(manual)));
			return ToolResult.of("probed");
		}).build();
		SubagentJob child = spawn(List.of(new AskLeadTool(), probe), new CopyOnWriteArrayList<>(),
			() -> List.of(new ToolCall("q1", "ask_lead", "{\"question\":\"Which source should I read?\"}")),
			() -> List.of(new ToolCall("p1", "probe", "{}")),
			() -> List.of(new TextContent("Read it.")));
		awaitWaiting(child);

		runLead(new CopyOnWriteArrayList<>(),
			() -> List.of(new ToolCall("r1", "reply_to_subagent",
				"{\"subagentId\":\"" + child.subagentId() + "\",\"answer\":\"Read " + manual + ".\"}")),
			() -> List.of(new TextContent("I answered the worker.")));

		assertEquals("the address in the lead's answer is given to the child", Boolean.TRUE,
			given.get(20, TimeUnit.SECONDS));
	}

	/** A lead waiting for one subagent returns at once when another one asks. */
	public void testWaitReturnsWhenAnotherSubagentAsks() throws Exception {
		SubagentJob busy = spawn(blockingProbe(), new CopyOnWriteArrayList<>(),
			() -> List.of(new ToolCall("p1", "probe", "{}")),
			() -> List.of(new TextContent("probed")));

		CompletableFuture<String> waiting = CompletableFuture.supplyAsync(() -> lead(new WaitForSubagentTool(),
			Map.of("subagentIds", List.of(busy.subagentId()), "timeoutMs", 30_000)));
		Thread.sleep(200);

		SubagentJob asking = spawn(new AskLeadTool(), new CopyOnWriteArrayList<>(),
			() -> List.of(new ToolCall("q1", "ask_lead", "{\"question\":\"" + QUESTION + "\"}")),
			() -> List.of(new TextContent("done")));

		String answer = waiting.get(10, TimeUnit.SECONDS);
		assertTrue("The waiting lead is handed the question: " + answer,
			answer.contains("Subagent " + asking.subagentId() + " (worker) asks you") && answer.contains(QUESTION));
		assertTrue(answer.contains("_still running_"));
		assertTrue("The question is shown once: it does not come again at the next turn.",
			_registry.takeUndeliveredQuestions().isEmpty());
	}

	/**
	 * A lead that waits for a subagent whose question it was already shown, and did not answer, is
	 * handed the question again at once instead of waiting until the timeout while the subagent waits
	 * for it (#811).
	 */
	public void testWaitOnASubagentThatWaitsForTheLeadReturnsAtOnce() throws Exception {
		SubagentJob child = spawn(new AskLeadTool(), new CopyOnWriteArrayList<>(),
			() -> List.of(new ToolCall("q1", "ask_lead", "{\"question\":\"" + QUESTION + "\"}")),
			() -> List.of(new TextContent("done")));
		awaitWaiting(child);
		assertEquals("The lead's turn was shown the question.", 1, _registry.takeUndeliveredQuestions().size());

		long start = System.currentTimeMillis();
		String answer = lead(new WaitForSubagentTool(),
			Map.of("subagentIds", List.of(child.subagentId()), "timeoutMs", 20_000));
		long waited = System.currentTimeMillis() - start;

		assertTrue("The wait does not block on a subagent that waits for the lead: " + waited + " ms", waited < 5_000);
		assertTrue("The open question is named: " + answer,
			answer.contains("Subagent " + child.subagentId() + " (worker) asks you") && answer.contains(QUESTION));
		assertTrue(answer, answer.contains("reply_to_subagent(\"" + child.subagentId() + "\""));
	}

	/** No answer in time: the child is told to decide, and a late reply is refused. */
	public void testAQuestionWithoutAnswerTimesOut() throws Exception {
		List<String> childRequests = new CopyOnWriteArrayList<>();
		SubagentJob child = spawn(new AskLeadTool(Duration.ofMillis(300)), childRequests,
			() -> List.of(new ToolCall("q1", "ask_lead", "{\"question\":\"" + QUESTION + "\"}")),
			() -> List.of(new TextContent("Decided myself.")));

		awaitTerminal(child);
		assertEquals(SubagentJobStatus.DONE, child.status());
		assertTrue(childRequests.get(1), childRequests.get(1).contains("No answer from the agent that started you"));

		String late = lead(new ReplyToSubagentTool(), Map.of("subagentId", child.subagentId(), "answer", "Europe."));
		assertTrue(late, late.startsWith("Not delivered: subagent " + child.subagentId() + " is not waiting"));
	}

	/**
	 * An answer that arrives after the question timed out is refused: the lead is told it answered only
	 * when the subagent goes on with that answer (#812).
	 */
	public void testAnAnswerAfterTheTimeoutIsRefused() {
		AtomicBoolean lateAccepted = new AtomicBoolean();
		SubagentJob job = new SubagentJob("sa-1", "worker", "research X", new CancellationToken()) {
			@Override
			public void clearLeadQuestion(LeadQuestion asked) {
				// The lead's answer arrives after the timeout, before the question is forgotten.
				lateAccepted.set(answerLead("Europe only."));
				super.clearLeadQuestion(asked);
			}
		};
		String answer;
		try (SubagentJobBinding.Binding bound = SubagentJobBinding.bind(job)) {
			answer = new AskLeadTool(Duration.ofMillis(50)).execute(Map.of("question", QUESTION), leadContext()).text();
		}
		assertTrue(answer, answer.startsWith("No answer from the agent that started you"));
		assertFalse("The lead must not be told that a subagent which gave up goes on with its answer.",
			lateAccepted.get());
	}

	/** Cancelling a child that waits for an answer ends the wait and the child. */
	public void testCancelWhileWaitingEndsTheChild() throws Exception {
		SubagentJob child = spawn(new AskLeadTool(), new CopyOnWriteArrayList<>(),
			() -> List.of(new ToolCall("q1", "ask_lead", "{\"question\":\"" + QUESTION + "\"}")),
			() -> List.of(new TextContent("must not be reached")));
		awaitWaiting(child);

		child.cancel("Not needed.");

		awaitTerminal(child);
		assertEquals(SubagentJobStatus.CANCELLED, child.status());
		assertNull(child.waitingFor());
	}

	private String lead(Tool tool, Map<String, Object> args) {
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			return tool.execute(args, ToolContext.EMPTY).text();
		}
	}

	/** One lead run over the registry, with the subagent tools it answers with. */
	@SafeVarargs
	private AgentResult runLead(List<String> requests, Supplier<List<ContentPart>>... replies) {
		ToolCatalog tools = new ToolCatalog();
		tools.register(new ReplyToSubagentTool());
		tools.freeze();
		AgentLoop lead = new AgentLoop(params("lead"), scripted(requests, replies), tools);
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("How are the workers doing?"))));
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			return lead.run(conversation, leadContext(), event -> {
				// Nothing to observe: the requests are recorded.
			});
		}
	}

	@SafeVarargs
	private SubagentJob spawn(Tool tool, List<String> requests, Supplier<List<ContentPart>>... replies) {
		return spawn(List.of(tool), requests, replies);
	}

	@SafeVarargs
	private SubagentJob spawn(List<Tool> catalog, List<String> requests, Supplier<List<ContentPart>>... replies) {
		ToolCatalog tools = new ToolCatalog();
		catalog.forEach(tools::register);
		tools.freeze();
		AgentLoop child = new AgentLoop(params("child"), scripted(requests, replies), tools);
		DelegationTool spawn = new DelegationTool("delegate_to_worker", "worker", "worker", "lead",
			(name, security) -> child);
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			spawn.execute(Map.of("task", "research X"), leadContext());
		}
		List<SubagentJob> jobs = _registry.byStatus(SubagentJobStatus.values());
		return jobs.get(jobs.size() - 1);
	}

	private Tool blockingProbe() {
		return ToolStubs.builder("probe").execute((args, context) -> {
			try {
				_release.await(30, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return ToolResult.of("probed");
		}).build();
	}

	@SafeVarargs
	private static LLMProvider scripted(List<String> requests, Supplier<List<ContentPart>>... replies) {
		AtomicInteger calls = new AtomicInteger();
		return (request, handler) -> {
			int idx = calls.getAndIncrement();
			requests.add(String.valueOf(request.messages()));
			if (idx >= replies.length) {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400, "no more");
			}
			List<ContentPart> content = replies[idx].get();
			for (int i = 0; i < content.size(); i++) {
				if (content.get(i) instanceof TextContent text) {
					handler.onEvent(new StreamEvent.TextDelta(i, text.text()));
				}
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + idx, MODEL, "end_turn", USAGE, content));
		};
	}

	private static AgentParams params(String prompt) {
		return new AgentParams(MODEL, prompt, AgentParams.DEFAULT_MAX_TURNS, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS);
	}

	private static ToolContext leadContext() {
		CancellationToken token = new CancellationToken();
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(token), token);
	}

	private static void awaitWaiting(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (job.waitingFor() == null && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertNotNull("The child must be waiting for the lead.", job.waitingFor());
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
				new TestSuite(TestSubagentAsksLead.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SubagentWorkers.Module.INSTANCE));
	}
}
