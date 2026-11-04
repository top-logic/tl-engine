/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.layout.WelcomeName;
import com.top_logic.ai.chat.view.ChatCommandDriver.Conversation;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.agent.RunCancellationBinding;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * A login in a window keeps the window's sub-session and changes its person; the chat of that
 * window follows the person.
 *
 * <p>
 * A page opened by a visitor, signed in to through the header's login dialog in the same window,
 * kept the chat's sign-in note, and the user's names were missing, because the window's binding of
 * the conversation held what the first render had found. And the conversation of one person must
 * not be the next one's after a change of the account in the window - neither its run, which is
 * stopped, nor its waiting approvals, its frames or its presentations.
 * </p>
 */
public class TestChatLoginInWindow extends BasicTestCase {

	private static final String PANE = "paneObject";

	private static Person alice;

	private static Person bob;

	private ChatCommandDriver _driver;

	private String _taskId;

	/** How the parked agent thread's wait ended. */
	private final AtomicReference<Throwable> _agentEnded = new AtomicReference<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (alice == null) {
			alice = TestPerson.createPerson("login-in-window-alice");
			bob = TestPerson.createPerson("login-in-window-bob");
		}
		_driver = new ChatCommandDriver("login-window-" + getName(), "login-" + getName());
	}

	@Override
	protected void tearDown() throws Exception {
		if (_taskId != null) {
			PendingInteractionRegistry.unregister(_taskId);
		}
		_driver.close();
		super.tearDown();
	}

	/** The render after the login states the user, also of the controls the visitor was shown. */
	public void testTheRenderAfterALoginStatesTheUser() throws Exception {
		Conversation visitor = _driver.window(null);
		assertEquals(Boolean.FALSE, visitor.state(ChatViewBinding.STATE_AUTHENTICATED));

		_driver.actAs(alice);
		visitor.render();
		assertEquals("A reload of the visitor's tree shows the composer.", Boolean.TRUE,
			visitor.state(ChatViewBinding.STATE_AUTHENTICATED));
		assertEquals(alice.getName(), visitor.state(ChatViewBinding.STATE_USER_NAME));

		Conversation signedIn = _driver.window(alice);
		assertEquals("The tree built for the user shows the composer.", Boolean.TRUE,
			signedIn.state(ChatViewBinding.STATE_AUTHENTICATED));
		assertEquals(alice.getName(), signedIn.state(ChatViewBinding.STATE_USER_NAME));
		assertEquals(WelcomeName.of(alice), signedIn.state(ChatViewBinding.STATE_WELCOME_NAME));
		assertSame(alice, ChatCommandDriver.tracker(signedIn).getAccount());
		visitor.cleanupTree();
		signedIn.cleanupTree();
	}

	/** After a change of the account in the window, the conversation and its commands are the new account's. */
	public void testTheNextAccountGetsAChatOfItsOwn() throws Exception {
		Conversation first = _driver.window(alice);
		String chatId = ChatHistoryService.createSession("Alice's", alice).tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();
		assertEquals(Boolean.TRUE, select(first, chatId).get(ChatControl.FIELD_OPENED));
		TaskProgressTracker alicesTracker = ChatCommandDriver.tracker(first);

		Conversation second = _driver.window(bob);
		TaskProgressTracker bobsTracker = ChatCommandDriver.tracker(second);
		assertNotSame("The chat Alice had open in the window is not Bob's.", alicesTracker, bobsTracker);
		assertSame(bob, bobsTracker.getAccount());
		assertNull(bobsTracker.getActiveSessionId());
		assertSame("The window's conversation is Bob's now.", bobsTracker,
			ChatSessionRegistry.tracker(TLContextManager.getSubSession(), second.binding().conversation()));
		assertEquals(bob.getName(), second.state(ChatViewBinding.STATE_USER_NAME));
		assertEquals("Bob cannot open Alice's chat.", Boolean.FALSE, select(second, chatId).get(ChatControl.FIELD_OPENED));
		first.cleanupTree();
		second.cleanupTree();
	}

	/**
	 * A run of the account before is stopped when another person takes the window over: its token
	 * is cancelled, its approval wait ends and the approval is discarded, its frames reach none of
	 * the new person's streams, its presentations none of the new person's channels, and the new
	 * person's tracker holds nothing of it.
	 */
	public void testTheRunOfTheAccountBeforeIsStopped() throws Exception {
		Conversation first = _driver.window(alice);
		String chatId = openChat(first, "Alice's run");
		TaskProgressTracker alicesTracker = ChatCommandDriver.tracker(first);
		AIProcessingState run = runIn(alicesTracker, chatId);
		UserInteractionStore store = run.getUserInteractionStore();
		String requestId = UUID.randomUUID().toString();
		CompletableFuture<String> approval = store.registerApproval(new ApprovalRequest(requestId, "createObject",
			"call-1", Map.of(), ToolMode.DATA_WRITE, "Creates a contract.", Duration.ofMinutes(30)));
		Thread agent = waitForApproval(run, store, requestId, approval);
		ViewChannel alicesPane = new DefaultViewChannel(PANE);
		first.binding().allowPresenting(PANE, alicesPane);

		Conversation second = _driver.window(bob);
		ViewChannel bobsPane = new DefaultViewChannel(PANE);
		second.binding().allowPresenting(PANE, bobsPane);
		attach(second, "bobsstream");

		assertTrue("Alice's run is stopped.", run.getCancellationToken().isCancelled());
		agent.join(TimeUnit.SECONDS.toMillis(10));
		assertFalse("The parked agent thread is woken by the stop.", agent.isAlive());
		assertTrue(_agentEnded.get() instanceof CancellationException);
		assertTrue("The approval is discarded.", store.getPendingApprovals().isEmpty());

		TaskProgressTracker bobsTracker = ChatCommandDriver.tracker(second);
		assertNotSame(alicesTracker, bobsTracker);
		assertNull("Bob's tracker holds no run.", bobsTracker.getAnyActiveProcessingState());
		assertNull(bobsTracker.getActiveSessionId());
		assertNotSame("Alice's tracker left the window.", alicesTracker,
			ChatSessionRegistry.tracker(TLContextManager.getSubSession(), first.binding().conversation()));
		for (ChatEventHandler target : ChatEventHandlerRegistry.findStreamTargets(alicesTracker, alice, chatId)) {
			assertNotSame("Alice's frames do not reach Bob's stream.", bobsTracker, target.getTracker());
		}
		assertNull("Alice's run presents nothing on Bob's page.",
			ChatViewBinding.presentable(TLContextManager.getSubSession(), PANE, alice));
		assertSame(bobsPane, ChatViewBinding.presentable(TLContextManager.getSubSession(), PANE, bob));
		first.cleanupTree();
		second.cleanupTree();
	}

	/** The same person rendering the window again keeps the run going. */
	public void testTheSamePersonKeepsTheRun() throws Exception {
		Conversation first = _driver.window(alice);
		String chatId = openChat(first, "Alice's long run");
		AIProcessingState run = runIn(ChatCommandDriver.tracker(first), chatId);

		Conversation again = _driver.window(alice);

		assertFalse("A render for the same person stops nothing.", run.getCancellationToken().isCancelled());
		assertSame(ChatCommandDriver.tracker(first), ChatCommandDriver.tracker(again));
		first.cleanupTree();
		again.cleanupTree();
	}

	/** A chat of the conversation's account, opened in the conversation. */
	private String openChat(Conversation conversation, String title) {
		Person owner = ChatCommandDriver.tracker(conversation).getAccount();
		String chatId = ChatHistoryService.createSession(title, owner).tHandle().getObjectName().toExternalForm();
		HistoryUtils.updateSessionRevision();
		assertEquals(Boolean.TRUE, select(conversation, chatId).get(ChatControl.FIELD_OPENED));
		return chatId;
	}

	/** A run of the given chat in the given window, registered as {@code ChatProcessor} does. */
	private AIProcessingState runIn(TaskProgressTracker window, String chatId) {
		_taskId = UUID.randomUUID().toString();
		AIProcessingState state = new AIProcessingState();
		state.setCurrentTaskId(_taskId);
		state.setSessionId(chatId);
		state.setUserInteractionStore(new UserInteractionStore());
		state.startProcessing();
		window.addProcessingState(_taskId, state);
		PendingInteractionRegistry.register(_taskId, window.getAccount(), window, state);
		return state;
	}

	/**
	 * The run's agent thread, parked on an approval as the chat's approval callback parks it: the
	 * run's cancellation ends the wait, and the wait discards its approval on the way out.
	 */
	private Thread waitForApproval(AIProcessingState run, UserInteractionStore store, String requestId,
			CompletableFuture<String> approval) {
		Thread agent = new Thread(() -> {
			try (RunCancellationBinding.Binding binding = RunCancellationBinding.bind(run.getCancellationToken())) {
				RunCancellationBinding.await(approval, 30, TimeUnit.SECONDS);
			} catch (Throwable ex) {
				_agentEnded.set(ex);
			} finally {
				store.discardApproval(requestId);
			}
		}, "agent-waiting-for-approval");
		agent.setDaemon(true);
		agent.start();
		return agent;
	}

	private void attach(ChatControl control, String token) {
		assertEquals(Boolean.TRUE,
			_driver.command(control, ChatControl.CMD_ATTACH_STREAM, Map.of(ChatControl.ARG_STREAM, token))
				.get(ChatControl.FIELD_ATTACHED));
	}

	private Map<String, Object> select(ChatControl control, String chatId) {
		return _driver.command(control, ChatControl.CMD_SELECT_SESSION, Map.of(ChatControl.ARG_SESSION_ID, chatId));
	}

	/** Test suite. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatLoginInWindow.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
