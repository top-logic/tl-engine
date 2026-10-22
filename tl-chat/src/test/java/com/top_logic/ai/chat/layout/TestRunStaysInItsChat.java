/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A chat run reads and writes the state of the chat it was started for - its plan, its tool
 * catalog, its loaded skills - also when its window shows another chat by the time the run starts
 * or ends (#844).
 *
 * <p>
 * The run is a real one, on a scripted model: it loads a skill and writes a plan, which is what a
 * chat keeps beyond its transcript.
 * </p>
 */
public class TestRunStaysInItsChat extends BasicTestCase {

	private static final String AGENT = "TestChatPlanner";

	private static final String SKILL = "TL_taskManagement";

	private static final String PLAN = "Plan of chat A";

	private static final long TIMEOUT_SECONDS = 60;

	private Person _owner;

	private String _chatA;

	private String _chatB;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_chatA = idOf(ChatHistoryService.createSession("A", _owner));
		_chatB = idOf(ChatHistoryService.createSession("B", _owner));
		HistoryUtils.updateSessionRevision();
	}

	@Override
	protected void tearDown() throws Exception {
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * The user opens chat B while the run of chat A works: the run's skill, plan and tool catalog
	 * end up in A, and B is left as it was.
	 */
	public void testASwitchWhileTheRunWorks() throws Exception {
		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.setActiveSessionId(_chatA, _owner));

		CountDownLatch working = new CountDownLatch(1);
		CountDownLatch switched = new CountDownLatch(1);
		ScriptedModel.install((request, turn) -> switch (turn) {
			case 0 -> List.of(loadSkill());
			case 1 -> {
				working.countDown();
				await(switched);
				yield List.of(createTask());
			}
			default -> List.<ContentPart> of(new TextContent("Planned."));
		});

		runIn(window, () -> {
			assertTrue("The run did not reach its second turn.", working.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			assertTrue(window.setActiveSessionId(_chatB, _owner));
			switched.countDown();
		});

		assertEquals(_chatB, window.getActiveSessionId());
		assertUntouched(window);

		assertTrue(window.setActiveSessionId(_chatA, _owner));
		assertKeptTheRun(window);
	}

	/**
	 * The user opens chat B just after the send of chat A was registered and before its run started:
	 * the run is still A's, from its first step to its last.
	 */
	public void testASwitchBeforeTheRunStarts() throws Exception {
		SwitchingWindow window = new SwitchingWindow();
		assertTrue(window.setActiveSessionId(_chatB, _owner));
		ConversationLog logOfB = window.getSessionManager().getOrCreateConversationLog();
		int turnsOfB = logOfB.turnCount();
		int messagesOfB = window.getChat().getMessages().size();
		assertTrue(window.setActiveSessionId(_chatA, _owner));
		window._switchTo = _chatB;

		ScriptedModel.install((request, turn) -> switch (turn) {
			case 0 -> List.of(loadSkill());
			case 1 -> List.of(createTask());
			default -> List.<ContentPart> of(new TextContent("Planned."));
		});

		runIn(window, () -> {
			// The switch happens inside the send.
		});

		assertEquals(_chatB, window.getActiveSessionId());
		assertUntouched(window);
		assertEquals("Chat B got no turn.", turnsOfB, logOfB.turnCount());
		assertEquals("Chat B got no message.", messagesOfB, window.getChat().getMessages().size());

		assertTrue(window.setActiveSessionId(_chatA, _owner));
		assertKeptTheRun(window);
		assertEquals("The run's turn is chat A's.", 1, window.getSessionManager().getOrCreateConversationLog().turnCount());
	}

	/**
	 * What a chat holds besides its transcript - its approval mode, the tools approved in it, its
	 * skills, its ratings and its effort - is still there when the window comes back to it from
	 * another chat it keeps in memory.
	 */
	public void testComingBackToAChatKeepsItsState() {
		TaskProgressTracker window = new TaskProgressTracker();
		assertTrue(window.setActiveSessionId(_chatA, _owner));
		window.setApprovalMode(ApprovalMode.AUTO_APPROVE);
		window.activeChat().addApprovedTool("object-edit.updateObject");
		window.addLoadedSkill(SKILL);
		window.setFeedback(0, "up", "good");
		window.setEffort("high");

		assertTrue(window.setActiveSessionId(_chatB, _owner));
		assertEquals(ApprovalMode.MANUAL, window.getApprovalMode());
		assertTrue(window.activeChat().approvedTools().isEmpty());
		assertTrue(window.getLoadedSkills().isEmpty());
		assertTrue(window.getFeedback().isEmpty());
		assertNull(window.getEffort());

		assertTrue(window.setActiveSessionId(_chatA, _owner));
		assertEquals(ApprovalMode.AUTO_APPROVE, window.getApprovalMode());
		assertEquals(java.util.Set.of("object-edit.updateObject"), window.activeChat().approvedTools());
		assertEquals(java.util.Set.of(SKILL), window.getLoadedSkills());
		assertEquals(1, window.getFeedback().size());
		assertEquals("high", window.getEffort());
	}

	/** The window shows chat B: nothing of A's run is in it, in memory or stored. */
	private void assertUntouched(TaskProgressTracker window) {
		assertNull("Chat B got the tool catalog of A's run.", window.getActiveToolRegistry());
		assertTrue("Chat B got the skills of A's run: " + window.getLoadedSkills(),
			window.getLoadedSkills().isEmpty());
		assertTrue("Chat B got the plan of A's run.", window.getActiveTaskStore().getAllTasks().isEmpty());
		String stored = storedMetadata(_chatB);
		assertFalse("Chat B's stored state names A's skill: " + stored, stored.contains(SKILL));
		assertFalse("Chat B's stored state holds A's plan: " + stored, stored.contains(PLAN));
	}

	/** The window shows chat A: the run's skill, plan and tool catalog are there, and stored. */
	private void assertKeptTheRun(TaskProgressTracker window) {
		assertNotNull("Chat A lost the tool catalog of its run.", window.getActiveToolRegistry());
		assertEquals(java.util.Set.of(SKILL), window.getLoadedSkills());
		TaskStore plan = window.getActiveTaskStore();
		assertEquals(1, plan.getAllTasks().size());
		assertEquals(PLAN, plan.getAllTasks().get(0).getSubject());
		String stored = storedMetadata(_chatA);
		assertTrue("Chat A's stored state misses its skill: " + stored, stored.contains(SKILL));
		assertTrue("Chat A's stored state misses its plan: " + stored, stored.contains(PLAN));
	}

	/**
	 * Sends a message in the chat the window shows, does what the user does meanwhile, and waits for
	 * the run to end.
	 */
	private void runIn(TaskProgressTracker window, Meanwhile meanwhile) throws Exception {
		Handler handler = new Handler(window, _owner);
		ChatEventHandlerRegistry.register(handler);
		try {
			ChatProcessor.SendResult sent = ChatProcessor.sendMessage(window, TLContextManager.getSubSession(),
				_owner.getName(), "Plan it.", null,
				new ChatProcessor.ChatRunOptions(AGENT, null, false, List.of()));
			assertNotNull("The send was refused.", sent.getTaskId());
			ChatProcessor.AIProcessingState run = window.getProcessingState(sent.getTaskId());
			meanwhile.run();
			run._backgroundTask.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNull("The run failed: " + run.getError(), run.getError());
			HistoryUtils.updateSessionRevision();
		} finally {
			ChatEventHandlerRegistry.unregister(handler);
		}
	}

	private static String storedMetadata(String chat) {
		String json = ChatHistoryService.getMetadataJson(ChatHistoryService.findOwnedSessionById(chat,
			PersonManager.getManager().getRoot()));
		return json == null ? "" : json;
	}

	private static ToolCall loadSkill() {
		return new ToolCall("call-skill", "loadSkill", "{\"skillId\": \"" + SKILL + "\"}");
	}

	private static ToolCall createTask() {
		return new ToolCall("call-plan", "createTask", "{\"subject\": \"" + PLAN
			+ "\", \"description\": \"Everything A needs.\", \"activeForm\": \"Planning A\"}");
	}

	private static void await(CountDownLatch latch) {
		try {
			assertTrue("The window did not switch.", latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new AssertionError(ex);
		}
	}

	private static String idOf(TLObject session) {
		return session.tHandle().getObjectName().toExternalForm();
	}

	/** What the user does while the run works. */
	private interface Meanwhile {
		void run() throws Exception;
	}

	/**
	 * A window whose user opens another chat just after a send was registered, as a request of the
	 * same window can do at any moment.
	 */
	private final class SwitchingWindow extends TaskProgressTracker {

		String _switchTo;

		@Override
		public void addProcessingState(String taskId, ChatProcessor.AIProcessingState state) {
			super.addProcessingState(taskId, state);
			String target = _switchTo;
			if (target != null) {
				_switchTo = null;
				assertTrue(setActiveSessionId(target, _owner));
			}
		}
	}

	/** The window's open event stream. */
	private static final class Handler implements ChatEventHandler {

		private final TaskProgressTracker _window;

		private final Person _account;

		Handler(TaskProgressTracker window, Person account) {
			_window = window;
			_account = account;
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
			return true;
		}

		@Override
		public void onEvent(AgentEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void sendMessageComplete(MessageCompleteEvent event) {
			// Not what this test asks about.
		}

		@Override
		public void sendProviderError(ProviderErrorEvent event) {
			// Not what this test asks about.
		}
	}

	/** The suite, on the services a chat run passes through. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestRunStaysInItsChat.class),
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
