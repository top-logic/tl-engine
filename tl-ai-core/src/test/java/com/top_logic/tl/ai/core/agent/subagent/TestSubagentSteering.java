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

		Part.SystemPart recorded = leadMessagePart(job);
		assertTrue(recorded.details().contains("Only look at the year 2026."));

		Conversation replayed = new Conversation();
		ConversationLogReplay.rebuildMemory(job.liveLog(), replayed);
		List<Message> messages = replayed.messages();
		int at = indexOfLeadMessage(messages);
		assertTrue("The replayed transcript holds the message: " + messages, at > 0);
		assertTrue("After the tool result it followed: " + messages,
			messages.get(at - 1) instanceof com.top_logic.tl.ai.core.message.ToolResultMessage);
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
		AgentLoop child = new AgentLoop(new AgentParams(MODEL, "child", AgentParams.DEFAULT_MAX_TURNS,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), provider, tools);
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

	private static Part.SystemPart leadMessagePart(SubagentJob job) {
		List<Part.SystemPart> found = new ArrayList<>();
		for (Turn turn : job.liveLog().turns()) {
			for (Part part : turn.assistantParts()) {
				if (part instanceof Part.SystemPart system
					&& system.eventType() == Part.SystemEventType.LEAD_MESSAGE) {
					found.add(system);
				}
			}
		}
		assertEquals("The child's own transcript records the message once.", 1, found.size());
		return found.get(0);
	}

	private static int indexOfLeadMessage(List<Message> messages) {
		for (int i = 0; i < messages.size(); i++) {
			if (messages.get(i) instanceof UserMessage user && user.content().get(0) instanceof TextContent text
				&& text.text().startsWith("[Message from the lead agent]")) {
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
