/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.OptionalDouble;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.limits.BudgetAllowance;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobStatus;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The user writes to one background subagent from its card: {@code POST /chat/subagents/{id}/message}
 * leaves the message in the subagent's inbox, from any window of the account, and refuses everything
 * else - an ended subagent with a reason, another account's subagent as if there were none. The
 * card's stop, {@code DELETE /chat/subagents/{id}}, reaches the subagent the same way.
 */
public class TestSubagentMessageEndpoint extends BasicTestCase {

	private static Person mia;

	private static Person noah;

	private static ServletRunner runner;

	private Probe _servlet;

	/** Mia's chat, new for every test, so the subagent ids start at {@code sa-1} each time. */
	private String _chat;

	/** The window Mia's lead turn ran in; its registry holds the subagent. */
	private TaskProgressTracker _leadWindow;

	private SubagentJob _job;

	/** Exposes the servlet's own dispatch to the test. */
	static final class Probe extends ChatServlet {
		void post(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doPost(req, resp);
		}

		void delete(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doDelete(req, resp);
		}

		void get(HttpServletRequest req, HttpServletResponse resp) throws Exception {
			doGet(req, resp);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (mia == null) {
			mia = TestPerson.createPerson("mia-steering-a-subagent");
			noah = TestPerson.createPerson("noah-next-door");
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		TLObject chat = ChatHistoryService.createSession("Anmeldungen", mia);
		HistoryUtils.updateSessionRevision();
		_chat = chat.tHandle().getObjectName().toExternalForm();

		_leadWindow = new TaskProgressTracker();
		assertTrue(_leadWindow.getSessionManager().switchSession(_chat, mia));
		SubagentJobRegistry registry = _leadWindow.getActiveSubagentRegistry();
		_job = registry.register("Counter", "Anmeldungen zählen", new CancellationToken());
		_job.setParentToolCallId("call-1");
		_job.markRunning();

		_servlet = new Probe();
		// The request comes from another window of the account: a second tab on the same chat.
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		ChatSessionRegistry.register(subSession, null, new TaskProgressTracker());
	}

	@Override
	protected void tearDown() throws Exception {
		_job.cancellationToken().cancel();
		super.tearDown();
	}

	/** The message waits in the inbox as the user's, and the answer names it for the receipt. */
	public void testTheOwnerReachesTheSubagentFromAnyWindow() throws Exception {
		Recording answer = post(_job.subagentId(), mia, body("call-1", "  Nur Anmeldungen aus 2026.  "));

		assertEquals(HttpServletResponse.SC_OK, answer._status);
		assertTrue(answer.text(), answer.text().contains("\"status\":\"sent\""));
		List<SubagentInbox.Message> waiting = _job.inbox().drain();
		assertEquals(1, waiting.size());
		SubagentInbox.Message message = waiting.get(0);
		assertEquals(SubagentInbox.Sender.USER, message.sender());
		assertEquals("Nur Anmeldungen aus 2026.", message.text());
		assertTrue("The answer names the message the read receipt will name: " + answer.text(),
			answer.text().contains("\"messageId\":\"" + message.id() + "\""));
	}

	/** Another account learns nothing - not even that the subagent exists. */
	public void testAnotherAccountIsAnsweredAsIfThereWereNone() throws Exception {
		Recording foreign = post(_job.subagentId(), noah, body("call-1", "Hör auf."));
		Recording unknown = post("sa-99", mia, body("call-1", "Hör auf."));

		assertEquals(HttpServletResponse.SC_NOT_FOUND, foreign._status);
		assertEquals(HttpServletResponse.SC_NOT_FOUND, unknown._status);
		assertEquals("The two answers must not differ.", unknown.text(), foreign.text());
		assertTrue("Nothing reached the subagent.", _job.inbox().drain().isEmpty());
	}

	/** A subagent started by another call of the same id is not the one the card means. */
	public void testTheStartingCallTellsNamesakesApart() throws Exception {
		assertEquals(HttpServletResponse.SC_NOT_FOUND,
			post(_job.subagentId(), mia, body("call-of-another-life", "Weiter."))._status);
		assertTrue(_job.inbox().drain().isEmpty());
	}

	/** An ended subagent reads no more messages, and the user is told so rather than left waiting. */
	public void testAnEndedSubagentIsRefused() throws Exception {
		_job.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("Anmeldungen: 12", TokenUsage.EMPTY, 1L, 1, StopReason.COMPLETE));

		Recording answer = post(_job.subagentId(), mia, body("call-1", "Noch eins."));

		assertEquals(HttpServletResponse.SC_CONFLICT, answer._status);
		assertTrue(answer.text(), answer.text().contains("\"status\":\"ended\""));
		assertTrue(_job.inbox().drain().isEmpty());
	}

	/** An empty message is not sent. */
	public void testAnEmptyMessageIsRefused() throws Exception {
		Recording answer = post(_job.subagentId(), mia, body("call-1", "   "));

		assertEquals(HttpServletResponse.SC_BAD_REQUEST, answer._status);
		assertTrue(_job.inbox().drain().isEmpty());
	}

	/**
	 * "Abbrechen" on a card stops the subagent from any window of the account - not only from the one
	 * whose run started it (#769).
	 */
	public void testTheOwnerStopsTheSubagentFromAnyWindow() throws Exception {
		Recording answer = delete(_job.subagentId(), mia, "call-1");

		assertTrue(answer.text(), answer.text().contains("\"cancelled\":true"));
		assertTrue(_job.cancellationToken().isCancelled());
	}

	/** Another account stops nothing, and is answered as if there were nothing to stop. */
	public void testAnotherAccountCannotStopIt() throws Exception {
		Recording answer = delete(_job.subagentId(), noah, "call-1");

		assertTrue(answer.text(), answer.text().contains("\"cancelled\":false"));
		assertFalse(_job.cancellationToken().isCancelled());
	}

	/**
	 * The list of the window's subagents says what each run cost and the budget they spend from, so
	 * the panel can sum them up (#783). A run reports US dollars; the list converts them into the
	 * system currency at today's rate (#858).
	 */
	public void testTheListSaysWhatTheSubagentsSpentAndFromWhat() throws Exception {
		_job.setCost(new Money(0.025, Money.USD));
		_job.setBudget(BudgetAllowance.of(200_000,
			new BudgetAllowance.MoneyBudget(5.0, 1.0, "EUR", 0.5)));

		String json;
		try (var rates = CostConversion.override("EUR", day -> OptionalDouble.of(0.5))) {
			json = listSubagents();
		}
		assertTrue(json, json.contains("\"cost\":0.0125,\"currency\":\"EUR\""));
		assertTrue(json, json.contains("\"budget\":{\"tokens\":200000,\"money\":4.0,\"currency\":\"EUR\"}"));
	}

	/** Without a rate the list shows what the run cost in US dollars, as it recorded it. */
	public void testWithoutARateTheListShowsDollars() throws Exception {
		_job.setCost(new Money(0.025, Money.USD));

		String json;
		try (var rates = CostConversion.override("EUR", day -> OptionalDouble.empty())) {
			json = listSubagents();
		}
		assertTrue(json, json.contains("\"cost\":0.025,\"currency\":\"USD\""));
	}

	private String listSubagents() throws Exception {
		ChatSessionRegistry.register(TLContextManager.getSubSession(), null, _leadWindow);
		Recording answer = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(
			new com.meterware.httpunit.GetMethodWebRequest("http://localhost/chat/subagents"));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.withoutSecurityChecks(() -> {
			SessionService.getInstance().loginUser(request, answer.asResponse(), mia);
		});
		_servlet.get(request, answer.asResponse());
		return answer.text();
	}

	private String body(String parentToolCallId, String text) {
		return "{\"sessionId\":\"" + _chat + "\",\"parentToolCallId\":\"" + parentToolCallId
			+ "\",\"text\":\"" + text + "\"}";
	}

	private Recording post(String subagentId, Person caller, String body) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat/subagents/" + subagentId + "/message",
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.withoutSecurityChecks(() -> {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		});
		_servlet.post(request, response.asResponse());
		return response;
	}

	private Recording delete(String subagentId, Person caller, String parentToolCallId) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = runner.newClient().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat/subagents/" + subagentId + "?sessionId=" + _chat + "&parentToolCallId="
				+ parentToolCallId,
			new ByteArrayInputStream(new byte[0]), "application/json"));
		HttpServletRequest request = invocation.getRequest();
		ThreadContext.withoutSecurityChecks(() -> {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		});
		_servlet.delete(request, response.asResponse());
		return response;
	}

	/** Records what the servlet answered. */
	static final class Recording {

		int _status = HttpServletResponse.SC_OK;

		final StringWriter _body = new StringWriter();

		private HttpServletResponse _view;

		String text() {
			return _body.toString();
		}

		HttpServletResponse asResponse() {
			if (_view == null) {
				_view = (HttpServletResponse) Proxy.newProxyInstance(
					HttpServletResponse.class.getClassLoader(),
					new Class<?>[] { HttpServletResponse.class },
					new Handler());
			}
			return _view;
		}

		private final class Handler implements InvocationHandler {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws IOException {
				switch (method.getName()) {
					case "sendError":
					case "setStatus":
						_status = ((Integer) args[0]).intValue();
						return null;
					case "getWriter":
						return new PrintWriter(_body, true);
					default:
						return null;
				}
			}
		}
	}

	/** Services the servlet and the chat history need. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSubagentMessageEndpoint.class),
				ModelService.Module.INSTANCE,
				SchedulerService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
