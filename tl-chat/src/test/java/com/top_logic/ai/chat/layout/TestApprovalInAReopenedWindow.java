/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.api.AccountRuns;
import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.ChatTranscripts;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * An approval answered in a window that opened the chat after the window that started the run was
 * closed (#30161).
 *
 * <p>
 * The run is a real one, on a scripted model: it makes one write the user has to approve, and
 * answers once the write is done. Window A sends the message and is closed while the run waits for
 * the approval; window B - another tracker, as a new browser session has - opens the chat, is shown
 * the approval and answers it the way its chat control does ({@link AccountRuns#respond}).
 * Everything the run does from then on has to reach B.
 * </p>
 *
 * <p>
 * That the reopened window is shown the waiting approval is {@code TestChatQuestionRecovery}'s and
 * {@code TestChatInFlightWindow}'s concern; here B answers it by the id the run announced.
 * </p>
 */
public class TestApprovalInAReopenedWindow extends BasicTestCase {

	private static final String AGENT = "TestChatWriter";

	private static final String ANSWER = "The write is done.";

	private static final long TIMEOUT_SECONDS = 60;

	private Person _owner;

	private String _chat;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_chat = ChatHistoryService.createSession("Reopened", _owner).tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * Window B answers the approval of the run window A started: B is told the approval is answered,
	 * and is shown the rest of the turn.
	 */
	public void testTheReopenedWindowSeesTheRestOfTheTurn() throws Exception {
		ScriptedModel.install((request, turn) -> switch (turn) {
			case 0 -> List.of(new ToolCall("call-write", ApprovedWriteTool.NAME, "{}"));
			default -> List.<ContentPart> of(new TextContent(ANSWER));
		});

		TaskProgressTracker windowA = new TaskProgressTracker();
		assertTrue(windowA.setActiveSessionId(_chat, _owner));
		Recorder a = new Recorder(windowA, _owner);
		ChatEventHandlerRegistry.register(a);

		ChatProcessor.SendResult sent = ChatProcessor.sendMessage(windowA, TLContextManager.getSubSession(),
			_owner.getName(), "Write it.", null, new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
		assertNotNull("The send was refused.", sent.getTaskId());
		ChatProcessor.AIProcessingState run = windowA.getProcessingState(sent.getTaskId());

		String requestId = a.awaitApproval();
		// The browser of window A is closed: its stream ends.
		a.close();

		TaskProgressTracker windowB = new TaskProgressTracker();
		assertTrue(windowB.getSessionManager().switchSession(_chat, _owner));
		Recorder b = new Recorder(windowB, _owner);
		ChatEventHandlerRegistry.register(b);
		try {
			assertTrue("Nobody was waiting for the answer.",
				AccountRuns.respond(_owner, requestId, AccountRuns.APPROVED, ApprovedWriteTool.NAME, null, null));
			run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNull("The run failed: " + run.getError(), run.getError());

			List<String> seen = b.seen();
			assertTrue("The reopened window is told the approval was answered (approval_resolved): " + seen,
				seen.contains("ApprovalResolved:" + requestId));
			assertTrue("The reopened window is shown the write: " + seen,
				seen.contains("ToolCallCompleted:" + ApprovedWriteTool.NAME));
			assertTrue("The reopened window is shown the run's end: " + seen, seen.contains("RunCompleted"));
			assertTrue("The reopened window is given the answer: " + seen,
				seen.contains("message_complete:" + ANSWER));

			// What the reopened window reads back once the run has ended (the client's
			// reloadTranscript(), served by ChatControl#getDownloadData, which first catches up with
			// the knowledge base as this does).
			HistoryUtils.updateSessionRevision();
			String transcript = DtoMapper.toJson(ChatTranscripts.sessionDetail(windowB, _owner, _chat));
			assertTrue("The reopened window reads the answer back: " + transcript, transcript.contains(ANSWER));
		} finally {
			b.close();
		}
	}

	/** A chat UI that notes what it is sent. */
	private static final class Recorder implements ChatEventHandler {

		private final TaskProgressTracker _window;

		private final Person _account;

		private final List<String> _seen = Collections.synchronizedList(new ArrayList<>());

		private final CountDownLatch _asked = new CountDownLatch(1);

		private volatile String _requestId;

		private volatile boolean _active = true;

		Recorder(TaskProgressTracker window, Person account) {
			_window = window;
			_account = account;
		}

		/** What this UI was sent, in order: an event by its kind and what it is about. */
		List<String> seen() {
			synchronized (_seen) {
				return new ArrayList<>(_seen);
			}
		}

		/** Waits until the run asks for an approval, and answers its id. */
		String awaitApproval() throws InterruptedException {
			assertTrue("The run did not ask for an approval: " + seen(),
				_asked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			return _requestId;
		}

		/** Ends this UI's stream, as {@code ChatSSEHandler#stop()} does. */
		void close() {
			_active = false;
			ChatEventHandlerRegistry.unregister(this);
		}

		@Override
		public TaskProgressTracker getTracker() {
			return _window;
		}

		@Override
		public Person getAccount() {
			return _account;
		}

		@Override
		public boolean isActive() {
			return _active;
		}

		@Override
		public void onEvent(AgentEvent event) {
			if (!_active) {
				return;
			}
			String kind = event.getClass().getSimpleName();
			if (event instanceof AgentEvent.ApprovalRequired required) {
				_seen.add(kind + ":" + required.approvalRequestId());
				_requestId = required.approvalRequestId();
				_asked.countDown();
			} else if (event instanceof AgentEvent.ApprovalResolved resolved) {
				_seen.add(kind + ":" + resolved.approvalRequestId());
			} else if (event instanceof AgentEvent.ToolCallCompleted completed) {
				_seen.add(kind + ":" + completed.toolName());
			} else {
				_seen.add(kind);
			}
		}

		@Override
		public void onTextDelta(AgentEvent.TextDelta delta, long end) {
			if (_active) {
				_seen.add("text_delta:" + delta.text());
			}
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			if (_active) {
				_seen.add("message_complete:" + event.getContentText());
			}
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			if (_active) {
				_seen.add("provider_error:" + event.getErrorMessage());
			}
		}

		@Override
		public void sendInteractionResolved(InteractionResolvedEvent event) {
			if (_active) {
				_seen.add("interaction_resolved:" + event.getRequestId());
			}
		}
	}

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestApprovalInAReopenedWindow.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}
