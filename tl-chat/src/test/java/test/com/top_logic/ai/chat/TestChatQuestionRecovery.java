/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.ai.chat.TestChatServletOwnership.Recording;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.InteractionType;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a question the agent asked keeps its shape when a reloaded page rebuilds its card (issue
 * #466).
 *
 * <p>
 * A reload leaves the run parked on the question but the window that drew the card gone, and the
 * stream re-emits a pending question only into the window whose run it is. What the new window has
 * is {@code GET /chat/interactions}, and the persisted {@code InteractionRequest} row it reads does
 * not record whether a choice takes one answer or several: {@code header}, {@code prompt} and the
 * options JSON are written down, {@code multiSelect} is not. A card rebuilt from the row alone
 * would turn a multi-select question into a radio group - the user would answer a different
 * question from the one the agent asked.
 * </p>
 *
 * <p>
 * The shape is therefore read off the run that is still waiting, through
 * {@link PendingInteractionRegistry}, exactly as an approval's tool mode has been since #393.
 * Nothing new is persisted, and the one case that leaves uncovered - the run did not survive - is
 * the case where the question cannot be answered either, which is what the last test here pins.
 * </p>
 *
 * <p>
 * The servlet is driven directly, exactly as {@link TestChatServletOwnership} drives it, and the
 * run is set up the way {@code ChatProcessor} sets one up: starting a real agent would need a
 * provider, a model and a tool catalog to ask about something the servlet decides on its own.
 * </p>
 */
public class TestChatQuestionRecovery extends BasicTestCase {

	private static Person mia;

	private static TLObject miasChat;

	private static AgentExecution execution;

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

	/** The run's interaction store, holding the question the agent thread is parked on. */
	private UserInteractionStore _store;

	/** The run of the window that asked, as {@code ChatProcessor} would have left it. */
	private AIProcessingState _run;

	/** The run's task id, the key it is registered under. */
	private String _taskId;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (mia == null) {
			mia = TestPerson.createPerson("mia-who-is-asked");
			miasChat = ChatHistoryService.createSession("Mias Chat", mia);
			execution = AgentExecutionStore.getInstance().beginExecution((AgentSession) miasChat,
				"run-" + UUID.randomUUID(), "orchestrator", "test-model", null, null);
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new TestChatServletOwnership.Probe();

		_store = new UserInteractionStore();
		_run = new AIProcessingState();
		_taskId = UUID.randomUUID().toString();
		_run.setCurrentTaskId(_taskId);
		_run.setSessionId(chatId());
		_run.setUserInteractionStore(_store);

		TaskProgressTracker windowThatAsked = new TaskProgressTracker();
		windowThatAsked.addProcessingState(_taskId, _run);
		PendingInteractionRegistry.register(_taskId, mia, windowThatAsked, _run);

		// The page has been reloaded: the feed is read from a new window with no run of its own.
		inWindow(new TaskProgressTracker());
	}

	@Override
	protected void tearDown() throws Exception {
		PendingInteractionRegistry.unregister(_taskId);
		super.tearDown();
	}

	/**
	 * A multi-select question is offered as multi-select.
	 *
	 * <p>
	 * The bug of #466: {@code multiSelect} reached no persisted field, so the feed said nothing
	 * about it and the rebuilt card fell back to a single choice.
	 * </p>
	 */
	public void testAMultiSelectQuestionKeepsItsShape() throws Exception {
		ask("choice", "[\"Rechnung 1\", \"Rechnung 2\"]", true);

		String body = interactions();

		assertTrue("The question must be offered at all: " + body, body.contains("\"type\":\"choice\""));
		assertTrue("The card must offer the several answers the agent asked for: " + body,
			body.contains("\"multiSelect\":true"));
	}

	/** A single-select question stays single-select; the shape is read, not assumed. */
	public void testASingleSelectQuestionStaysSingle() throws Exception {
		ask("choice", "[\"Ja\", \"Nein\"]", false);

		String body = interactions();

		assertTrue("A question that takes one answer must not become a checkbox group: " + body,
			body.contains("\"multiSelect\":false"));
	}

	/**
	 * The options are handed over parsed, so that the card offers the same choices however it was
	 * drawn.
	 *
	 * <p>
	 * The model may write them as bare strings or as objects; which of the two it chose is not
	 * something a client should have to know, so the same parser the live frame uses runs here.
	 * </p>
	 */
	public void testTheOptionsAreOfferedAsTheAgentWroteThem() throws Exception {
		ask("choice", "[{\"label\":\"Sofort\",\"description\":\"Heute noch\"},\"Morgen\"]", false);

		String body = interactions();

		assertTrue("The first option must keep its label and its description: " + body,
			body.contains("\"label\":\"Sofort\"") && body.contains("\"description\":\"Heute noch\""));
		assertTrue("A bare string is an option too: " + body, body.contains("\"label\":\"Morgen\""));
	}

	/** A free-text question needs no options, and is offered without any. */
	public void testAFreeTextQuestionIsOfferedWithoutOptions() throws Exception {
		ask("text", null, false);

		String body = interactions();

		assertTrue("The question must be offered: " + body, body.contains("\"type\":\"question\""));
		assertTrue("Nothing to choose from: " + body, body.contains("\"options\":[]"));
	}

	/**
	 * A question nobody is parked on any more is not offered.
	 *
	 * <p>
	 * Its shape is unknown once the run is gone - and so is any use for it: an answer would reach
	 * nothing. That is the one case reading the shape off the live run does not cover, and it is
	 * the case where there is nothing to cover.
	 * </p>
	 */
	public void testAQuestionNobodyWaitsForIsNotOffered() throws Exception {
		String requestId = ask("choice", "[\"Ja\", \"Nein\"]", true);
		runEnded();

		String body = interactions();

		assertFalse("A card that leads nowhere must not be rebuilt: " + body,
			body.contains(requestId));
	}

	/**
	 * Asks the question: the live request the run parks on, and the row the reloaded page reads.
	 *
	 * @param type
	 *        {@code "text"} or {@code "choice"}, as {@code askUser} takes it.
	 * @param options
	 *        The options JSON the model wrote, or <code>null</code> for a free-text question.
	 * @param multiSelect
	 *        Whether the choice takes more than one answer.
	 * @return The correlation id both halves are keyed by.
	 */
	private String ask(String type, String options, boolean multiSelect) {
		UserInteractionRequest request =
			_store.createRequest("Was soll ich verschicken?", "Auswahl", type, options, multiSelect);
		AgentExecutionStore.getInstance().createInteraction(execution,
			"choice".equals(type) ? InteractionType.CHOICE : InteractionType.QUESTION,
			request.getId(), request.getHeader(), request.getQuestion(), options);
		return request.getId();
	}

	/** Tears the run down the way {@code ChatProcessor} tears a finished one down. */
	private void runEnded() {
		PendingInteractionRegistry.unregister(_taskId);
		_run.setUserInteractionStore(null);
	}

	/** What {@code GET /chat/interactions} answers Mia, as JSON. */
	private String interactions() throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(
			new GetMethodWebRequest("http://localhost/chat/interactions"));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.withoutSecurityChecks(() -> {
			SessionService.getInstance().loginUser(request, response.asResponse(), mia);
		});
		_servlet.get(request, response.asResponse());
		assertEquals(HttpServletResponse.SC_OK, response._status);
		return response._body.toString();
	}

	/** Makes the given window the one the next request arrives from. */
	private static void inWindow(TaskProgressTracker window) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		ChatSessionRegistry.register(subSession, null, window);
	}

	private static String chatId() {
		return miasChat.tHandle().getObjectName().toExternalForm();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatQuestionRecovery.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
