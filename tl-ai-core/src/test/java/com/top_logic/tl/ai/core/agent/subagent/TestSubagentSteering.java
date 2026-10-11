/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolStubs;
import com.top_logic.tl.ai.core.tools.interaction.CancelSubagentTool;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SendToSubagentTool;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.model.ModelService;

/**
 * The lead steers and stops one background subagent (issue #705): {@code send_to_subagent} leaves a
 * message the subagent reads at its next turn, {@code cancel_subagent} stops it.
 */
public class TestSubagentSteering extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 50, 0, 0, 0);

	private SubagentJobRegistry _registry;

	private final CountDownLatch _inProbe = new CountDownLatch(1);

	private final CountDownLatch _release = new CountDownLatch(1);

	/** The text of every request the child's model received, in order. */
	private final List<String> _childRequests = new CopyOnWriteArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(2);
	}

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		_registry.cancelAll();
		_registry.shutdown();
		super.tearDown();
	}

	/**
	 * A message sent while the child is inside a tool call is in its next request, in its transcript,
	 * and in the same place when that transcript is replayed.
	 */
	public void testAMessageReachesTheChildsNextTurn() throws Exception {
		SubagentJob job = spawnChild();
		assertTrue("The child must reach its tool call.", _inProbe.await(20, TimeUnit.SECONDS));

		String answer = lead(new SendToSubagentTool(),
			Map.of("subagentId", job.subagentId(), "message", "Only look at the year 2026."));
		assertTrue(answer, answer.startsWith("Sent to subagent " + job.subagentId()));
		assertTrue("The lead is told when the message is read: " + answer, answer.contains("next turn"));
		_release.countDown();
		awaitTerminal(job);

		assertEquals(SubagentJobStatus.DONE, job.status());
		assertEquals(2, _childRequests.size());
		assertFalse("Not before it was sent.", _childRequests.get(0).contains("Only look at the year 2026."));
		assertTrue("In the request after the tool call: " + _childRequests.get(1),
			_childRequests.get(1).contains("[Message from the lead agent]")
				&& _childRequests.get(1).contains("Only look at the year 2026."));

		Part.SystemPart recorded = inboxPart(job, Part.SystemEventType.LEAD_MESSAGE);
		assertTrue(recorded.details().contains("Only look at the year 2026."));

		Conversation replayed = new Conversation();
		ConversationLogReplay.rebuildMemory(job.liveLog(), replayed);
		List<Message> messages = replayed.messages();
		int at = indexOfMessageStartingWith(messages, "[Message from the lead agent]");
		assertTrue("The replayed transcript holds the message: " + messages, at > 0);
		assertTrue("After the tool result it followed: " + messages,
			messages.get(at - 1) instanceof com.top_logic.tl.ai.core.message.ToolResultMessage);
	}

	/**
	 * What the user writes from the card reaches the child's next request as the user's, is recorded
	 * in its transcript as the user's, and stands at the same place when that transcript is replayed.
	 */
	public void testAUserMessageReachesTheChildAsTheUsers() throws Exception {
		SubagentJob job = spawnChild();
		assertTrue("The child must reach its tool call.", _inProbe.await(20, TimeUnit.SECONDS));

		assertTrue(job.inbox().post(
			SubagentInbox.Message.of(SubagentInbox.Sender.USER, "Nur Anmeldungen aus 2026.")));
		_release.countDown();
		awaitTerminal(job);

		assertEquals(SubagentJobStatus.DONE, job.status());
		assertEquals(2, _childRequests.size());
		String second = _childRequests.get(1);
		assertTrue("In the request after the tool call, as the user's: " + second,
			second.contains("[Message from the user]") && second.contains("Nur Anmeldungen aus 2026."));
		assertFalse("Not passed off as the lead's: " + second, second.contains("[Message from the lead agent]"));

		Part.SystemPart recorded = inboxPart(job, Part.SystemEventType.USER_MESSAGE);
		assertTrue(recorded.details().contains("Nur Anmeldungen aus 2026."));

		Conversation replayed = new Conversation();
		ConversationLogReplay.rebuildMemory(job.liveLog(), replayed);
		List<Message> messages = replayed.messages();
		int at = indexOfMessageStartingWith(messages, "[Message from the user]");
		assertTrue("The replayed transcript holds the message: " + messages, at > 0);
		assertTrue("After the tool result it followed: " + messages,
			messages.get(at - 1) instanceof com.top_logic.tl.ai.core.message.ToolResultMessage);
	}

	/** The job keeps what its run cost, and the budget it spends from, for the chat's panel (#783). */
	public void testTheJobKnowsItsCostAndBudget() throws Exception {
		SubagentJob job = spawnChild();
		assertTrue(_inProbe.await(20, TimeUnit.SECONDS));
		assertNull("Unknown while it works.", job.cost());
		assertSame(com.top_logic.tl.ai.core.limits.BudgetAllowance.UNLIMITED, job.budget());
		_release.countDown();
		awaitTerminal(job);

		assertNotNull("Its run reported what it cost.", job.cost());
	}

	/** Cancelling ends the job cancelled with the reason, and a message to it is refused. */
	public void testCancelEndsTheJobAndRefusesMessages() throws Exception {
		SubagentJob job = spawnChild();
		assertTrue(_inProbe.await(20, TimeUnit.SECONDS));

		String answer = lead(new CancelSubagentTool(),
			Map.of("subagentId", job.subagentId(), "reason", "No longer needed."));
		assertTrue(answer, answer.startsWith("Stopping subagent " + job.subagentId()));
		_release.countDown();
		awaitTerminal(job);

		assertEquals(SubagentJobStatus.CANCELLED, job.status());
		assertEquals("Cancelled by the lead agent: No longer needed.", job.result().finalText());

		String refused = lead(new SendToSubagentTool(), Map.of("subagentId", job.subagentId(), "message", "Go on."));
		assertTrue(refused, refused.startsWith("Not sent: subagent " + job.subagentId() + " has already ended"));
		String again = lead(new CancelSubagentTool(), Map.of("subagentId", job.subagentId()));
		assertTrue(again, again.contains("has already ended"));
	}

	/**
	 * A message sent while the child writes what would have been its last answer is read in one more
	 * turn, not accepted and dropped (#804).
	 */
	public void testAMessageSentDuringTheLastAnswerIsRead() throws Exception {
		SubagentJob job = spawnAnsweringChild(AgentParams.DEFAULT_MAX_TURNS);
		assertTrue("The child must be writing its answer.", _inProbe.await(20, TimeUnit.SECONDS));

		String answer = lead(new SendToSubagentTool(),
			Map.of("subagentId", job.subagentId(), "message", "Only Europe, please."));
		assertTrue(answer, answer.startsWith("Sent to subagent " + job.subagentId()));
		_release.countDown();
		awaitTerminal(job);

		assertEquals(SubagentJobStatus.DONE, job.status());
		assertEquals("The child takes one more turn for the message: " + _childRequests, 2, _childRequests.size());
		assertTrue(_childRequests.get(1), _childRequests.get(1).contains("Only Europe, please."));
		assertEquals("Answer 2.", job.result().finalText());
		assertTrue("Read, so not reported as unread.", job.unreadMessages().isEmpty());

		String late = lead(new SendToSubagentTool(), Map.of("subagentId", job.subagentId(), "message", "And Asia."));
		assertTrue(late, late.startsWith("Not sent: subagent " + job.subagentId()));
	}

	/**
	 * A message that arrives when the child has no turn left is named in the result the lead reads,
	 * outside the child's report.
	 */
	public void testAMessageWithoutATurnLeftIsReportedAsUnread() throws Exception {
		SubagentJob job = spawnAnsweringChild(1);
		assertTrue("The child must be writing its answer.", _inProbe.await(20, TimeUnit.SECONDS));

		assertTrue(job.inbox().post(SubagentInbox.Message.of(SubagentInbox.Sender.USER, "Nur Europa.")));
		_release.countDown();
		awaitTerminal(job);

		assertEquals(1, _childRequests.size());
		assertEquals(1, job.unreadMessages().size());
		String delivered = SubagentResultDelivery.describe(job);
		int reportEnd = delivered.lastIndexOf("</untrusted_subagent_report>");
		int unread = delivered.indexOf("_Not read: 1 message");
		assertTrue("The lead is told: " + delivered, unread > reportEnd && reportEnd > 0);
		assertTrue(delivered, delivered.contains("- from the user: Nur Europa."));
	}

	/** An unknown id is an error the lead can correct. */
	public void testAnUnknownSubagentIsAnError() {
		assertTrue(lead(new SendToSubagentTool(), Map.of("subagentId", "sa-99", "message", "x"))
			.startsWith("ERROR: No such subagent: sa-99"));
		assertTrue(lead(new CancelSubagentTool(), Map.of("subagentId", "sa-99"))
			.startsWith("ERROR: No such subagent: sa-99"));
	}

	private String lead(com.top_logic.tl.ai.tool.api.Tool tool, Map<String, Object> args) {
		try (SubagentRegistryBinding.Binding bound = SubagentRegistryBinding.bind(_registry)) {
			return tool.execute(args, ToolContext.EMPTY).text();
		}
	}

	/**
	 * A child that answers in every turn, "Answer 1." first; its first request waits until the test
	 * releases it, so a message can be sent while it writes that answer.
	 */
	private SubagentJob spawnAnsweringChild(int maxTurns) {
		AtomicInteger calls = new AtomicInteger();
		LLMProvider provider = (request, handler) -> {
			int idx = calls.getAndIncrement();
			_childRequests.add(String.valueOf(request.messages()));
			if (idx == 0) {
				_inProbe.countDown();
				try {
					_release.await(30, TimeUnit.SECONDS);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}
			List<ContentPart> content = List.of(new TextContent("Answer " + (idx + 1) + "."));
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + idx, MODEL, "end_turn", USAGE, content));
		};
		ToolCatalog tools = new ToolCatalog();
		tools.freeze();
		return spawn(new AgentLoop(new AgentParams(MODEL, "child", maxTurns, AgentParams.DEFAULT_MAX_OUTPUT_TOKENS),
			provider, tools));
	}

	/** A child that calls a blocking tool in its first turn and answers in its second. */
	private SubagentJob spawnChild() {
		AtomicInteger calls = new AtomicInteger();
		LLMProvider provider = (request, handler) -> {
			int idx = calls.getAndIncrement();
			_childRequests.add(String.valueOf(request.messages()));
			List<ContentPart> content;
			if (idx == 0) {
				content = List.of(new ToolCall("c1", "probe", "{}"));
			} else if (idx == 1) {
				content = List.of(new TextContent("Done."));
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
		tools.register(ToolStubs.builder("probe").execute((args, context) -> {
			_inProbe.countDown();
			try {
				_release.await(30, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
			return ToolResult.of("probed");
		}).build());
		tools.freeze();
		return spawn(new AgentLoop(new AgentParams(MODEL, "child", AgentParams.DEFAULT_MAX_TURNS,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, tools));
	}

	private SubagentJob spawn(AgentLoop child) {
		DelegationTool spawn = new DelegationTool("delegate_to_worker", "worker", "worker", "lead",
			(name, security) -> child);
		CancellationToken token = new CancellationToken();
		ToolContext lead = new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE,
			new TransactionScope(token), token);
		try (SubagentRegistryBinding.Binding registry = SubagentRegistryBinding.bind(_registry)) {
			spawn.execute(Map.of("task", "research X"), lead);
		}
		return _registry.byStatus(SubagentJobStatus.values()).get(0);
	}

	private static void awaitTerminal(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
			Thread.sleep(10);
		}
		assertTrue("The child must have ended.", job.status().isTerminal());
	}

	private static Part.SystemPart inboxPart(SubagentJob job, Part.SystemEventType type) {
		List<Part.SystemPart> found = new ArrayList<>();
		for (Turn turn : job.liveLog().turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.SystemPart system && system.eventType() == type) {
					found.add(system);
				}
			}
		}
		assertEquals("The child's own transcript records the message once.", 1, found.size());
		return found.get(0);
	}

	private static int indexOfMessageStartingWith(List<Message> messages, String start) {
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i) instanceof UserMessage user && user.content().get(0) instanceof TextContent text
				&& text.text().startsWith(start)) {
				return i;
			}
		}
		return -1;
	}

	/** */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentSteering.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
