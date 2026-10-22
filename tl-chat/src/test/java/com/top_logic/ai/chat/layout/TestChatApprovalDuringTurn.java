/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.PendingApproval;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
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
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * What a chat run's approvals do while the person acts during the turn: switches auto-approve
 * (#943), or answers the moment the card is shown (#944).
 *
 * <p>
 * The run is a real {@link AgentLoop} with the approval callback a chat run gets from
 * {@link ChatProcessor#buildToolContext}; the model is scripted, and the person is a listener that
 * acts when the loop announces an approval - which is the earliest a browser can.
 * </p>
 */
public class TestChatApprovalDuringTurn extends BasicTestCase {

	private static final String MODEL = "test-model";

	private static final TokenUsage USAGE = new TokenUsage(100, 20, 0, 0, 0);

	/** How long a run may take that waits for nobody. */
	private static final long RUN_SECONDS = 10;

	private static Person account;

	private Person _previous;

	private UserInteractionStore _store;

	private ChatState _chat;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (account == null) {
			account = TestPerson.createPerson("approval-during-turn-account");
		}
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previous = subSession.getPerson();
		subSession.setPerson(account);
		_store = new UserInteractionStore();
		_chat = new TaskProgressTracker().activeChat();
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previous);
		super.tearDown();
	}

	/** An answer given while the approval is being announced reaches the run (#944). */
	public void testAnAnswerAtTheAnnouncementReachesTheRun() throws Exception {
		_chat.setApprovalMode(ApprovalMode.MANUAL);
		AgentEventListener answersAtOnce = event -> {
			if (event instanceof AgentEvent.ApprovalRequired required) {
				_store.resolve(required.approvalRequestId(), "APPROVED");
			}
		};

		AgentResult result = runWrites(1, answersAtOnce);

		assertEquals(StopReason.COMPLETE, result.stopReason());
	}

	/** Switching auto-approve on during the turn: the next writes of that turn are not asked (#943). */
	public void testSwitchingOnDuringTheTurnStopsAsking() throws Exception {
		_chat.setApprovalMode(ApprovalMode.MANUAL);
		List<String> asked = Collections.synchronizedList(new ArrayList<>());
		AgentEventListener switchesOnAtTheFirstCard = event -> {
			if (event instanceof AgentEvent.ApprovalRequired required) {
				if (_store.isPending(required.approvalRequestId())) {
					asked.add(required.toolCallId());
				}
				if ("call-0".equals(required.toolCallId())) {
					// The person switches the chat to auto-approve at the first card; the browser
					// then answers that card for them, and leaves.
					_chat.setApprovalMode(ApprovalMode.AUTO_APPROVE);
					_store.resolve(required.approvalRequestId(), "APPROVED");
				}
			}
		};

		AgentResult result = runWrites(3, switchesOnAtTheFirstCard);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("Only the write before the switch is asked.", List.of("call-0"), asked);
	}

	/** Switching auto-approve off during the turn: the next writes of that turn are asked (#943). */
	public void testSwitchingOffDuringTheTurnStartsAsking() throws Exception {
		_chat.setApprovalMode(ApprovalMode.AUTO_APPROVE);
		List<String> asked = Collections.synchronizedList(new ArrayList<>());
		AtomicInteger announced = new AtomicInteger();
		AgentEventListener switchesOffAtTheFirstWrite = event -> {
			if (event instanceof AgentEvent.ApprovalRequired required) {
				if (announced.getAndIncrement() == 0) {
					_chat.setApprovalMode(ApprovalMode.MANUAL);
				}
				if (_store.isPending(required.approvalRequestId())) {
					asked.add(required.toolCallId());
					_store.resolve(required.approvalRequestId(), "APPROVED");
				}
			}
		};

		AgentResult result = runWrites(3, switchesOffAtTheFirstWrite);

		assertEquals(StopReason.COMPLETE, result.stopReason());
		assertEquals("Every write after the switch is asked.", List.of("call-1", "call-2"), asked);
	}

	/**
	 * Runs a turn in which the model makes the given number of writes one after the other, with the
	 * approval callback of a chat run over {@link #_chat}.
	 *
	 * <p>
	 * A run that is still waiting for an approval after {@link #RUN_SECONDS} fails the test; its
	 * waits are then answered, so that it does not outlive the test.
	 * </p>
	 */
	private AgentResult runWrites(int writes, AgentEventListener person) throws Exception {
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage("u1", Instant.now(), List.of(new TextContent("Write."))));
		ToolCatalog catalog = new ToolCatalog();
		catalog.register(new WriteTool());
		catalog.freeze();
		ApprovalCallback approval =
			ChatProcessor.buildToolContext(_store, new TaskProgressTracker(), _chat, new CancellationToken())
				.approval();
		ToolContext context = new ToolContext(asksForWrites(), approval);
		AgentLoop loop = new AgentLoop(new AgentParams(MODEL, "You write.", 10,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), writesThenAnswers(writes), catalog);

		CompletableFuture<AgentResult> run =
			CompletableFuture.supplyAsync(() -> loop.run(conversation, context, person));
		try {
			return run.get(RUN_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException ex) {
			List<String> waiting = new ArrayList<>();
			for (PendingApproval pending : _store.getPendingApprovals()) {
				waiting.add(pending.request().toolCallId());
				_store.resolve(pending.id(), "REJECTED");
			}
			fail("The run still waits for an approval nobody is going to give: " + waiting);
			return null;
		}
	}

	/** A model that makes one write per turn, the given number of times, and then answers. */
	private static LLMProvider writesThenAnswers(int writes) {
		AtomicInteger calls = new AtomicInteger();
		return (request, handler) -> {
			int call = calls.getAndIncrement();
			List<ContentPart> content;
			if (call < writes) {
				// Arguments of its own each time: the loop blocks a call repeated with the same ones.
				ToolCall write = new ToolCall("call-" + call, WriteTool.NAME, "{\"n\":" + call + "}");
				handler.onEvent(new StreamEvent.ToolCallStart(0, write.id(), write.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, write.arguments()));
				content = List.of(write);
			} else if (call == writes) {
				content = List.of(new TextContent("Done."));
				handler.onEvent(new StreamEvent.TextDelta(0, "Done."));
			} else {
				throw new ProviderException(ProviderException.Kind.INVALID_REQUEST, 400,
					"No more answers (call " + call + ").");
			}
			handler.onEvent(new StreamEvent.MessageComplete("msg-" + call, MODEL,
				call < writes ? "tool_use" : "end_turn", USAGE, content));
		};
	}

	/** A context that permits every tool and asks for every write, as the chat's default does. */
	private static SecurityContext asksForWrites() {
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

	/** A write that needs an approval and changes nothing. */
	private static final class WriteTool implements Tool {

		static final String NAME = "write";

		@Override
		public String name() {
			return NAME;
		}

		@Override
		public String description() {
			return "Writes.";
		}

		@Override
		public ToolSchema schema() {
			return new ToolSchema(NAME, "Writes.", "{}");
		}

		@Override
		public ToolMode mode() {
			return ToolMode.STRUCTURAL;
		}

		@Override
		public boolean isConcurrencySafe() {
			return false;
		}

		@Override
		public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
			return ToolResult.of("Written.");
		}
	}

	/** */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatApprovalDuringTurn.class),
				ModelService.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE));
	}
}
