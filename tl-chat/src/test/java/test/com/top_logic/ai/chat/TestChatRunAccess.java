/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.servletunit.InvocationContext;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.top_logic.ai.chat.api.ChatServlet;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * That a chat request may only name an agent, a model and an MCP server the acting account may use.
 *
 * <p>
 * The policy of {@code #392} decides in {@code AgentFactory}, where every entry point passes; these
 * are the two halves tl-chat adds on top - the menu, which must not offer what a run would refuse,
 * and the refusal itself, which must reach the user as a sentence rather than as a turn that failed.
 * </p>
 *
 * <p>
 * Asserted against the <em>shipped defaults</em> rather than against a policy written for the test:
 * the test application configures two models and grants nobody anything, so "a request may not choose
 * a model other than its agent's" is the behaviour of an application that configured no policy at all
 * - which is the claim most worth pinning. The role-keyed half is unit-tested in
 * {@code TestAgentAccess}, {@code TestModelAccess} and {@code TestMcpAccess}, and driven end to end
 * against the {@code secdemo} fixture.
 * </p>
 *
 * <p>
 * The servlet is driven directly, exactly as {@link TestChatServletOwnership} drives it, and for the
 * same reason.
 * </p>
 */
public class TestChatRunAccess extends BasicTestCase {

	/** The model the test application assigns to every agent. */
	private static final String ASSIGNED_MODEL = "test-assigned-model";

	/** A second configured model, which is therefore a choice rather than the agent's own. */
	private static final String OTHER_MODEL = "test-other-model";

	/** An agent of the test application that carries no user-facing line. */
	private static final String AGENT_WITHOUT_SUMMARY = "TestAgentWithoutSummary";

	/** An agent this product ships, which carries both a display name and a summary. */
	private static final String FRAMEWORK_AGENT = "TL_ModelExplorer";

	/**
	 * An agent this product ships as {@code infrastructure}: the chat runs it <em>for</em> the user.
	 */
	private static final String INFRASTRUCTURE_AGENT = "TL_PromptImprover";

	/** What the test application's configuration overlay renames {@link #FRAMEWORK_AGENT} to. */
	private static final String OVERRIDDEN_NAME_EN = "Model Scout";

	/** The same, in German. */
	private static final String OVERRIDDEN_NAME_DE = "Modell-Späher";

	/** The summary the overlay leaves alone, as this product ships it. */
	private static final String SHIPPED_SUMMARY_EN = "Explores model and data, changes nothing.";

	/** The same, in German. */
	private static final String SHIPPED_SUMMARY_DE = "Erkundet Modell und Daten, ändert nichts.";

	private static Person anna;

	private static TLObject annasChat;

	private static ServletRunner runner;

	private TestChatServletOwnership.Probe _servlet;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-who-runs-agents");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
			runner = new ServletRunner();
			runner.registerServlet("chat/*", ChatServlet.class.getName());
		}
		_servlet = new TestChatServletOwnership.Probe();

		TLSubSessionContext subSession = TLContextManager.getSubSession();
		assertNotNull("The test needs a sub-session to hang the tracker on.", subSession);
		ChatSessionRegistry.register(subSession, null, new TaskProgressTracker());
	}

	/** Precondition: the fixture this test reasons about actually reached the application. */
	public void testFixture() {
		assertTrue("The security service decides here; without it nothing is checked.",
			AiToolAccessService.Module.INSTANCE.isActive());
		assertTrue("Two models must be configured for 'the other one' to mean anything: "
			+ LLMProviderService.getInstance().getConfiguredModelNames(),
			LLMProviderService.getInstance().getConfiguredModelNames().containsAll(
				java.util.List.of(ASSIGNED_MODEL, OTHER_MODEL)));
	}

	/**
	 * The menu offers the model the agent is assigned, and not the second one.
	 *
	 * <p>
	 * The shipped default of the model section: a request runs on what its agent is assigned until an
	 * administrator hands the choice out. A client that was offered the other model would be offered a
	 * turn that comes back refused.
	 * </p>
	 */
	public void testCapabilitiesOffersOnlyTheAgentsOwnModel() throws Exception {
		String body = capabilities();

		assertTrue(body, body.contains(ASSIGNED_MODEL));
		assertFalse("The model no role grants must not be offered: " + body, body.contains(OTHER_MODEL));
	}

	/** An agent that carries a display name is offered under it, with its identifier beside it. */
	public void testCapabilitiesCarriesTheDisplayNameAndTheSummary() throws Exception {
		String agent = agentEntry(capabilities(), FRAMEWORK_AGENT);

		assertTrue("The identifier stays what a request names: " + agent,
			agent.contains("\"name\":\"" + FRAMEWORK_AGENT + "\""));
		assertFalse("A framework agent has a display name: " + agent, agent.contains("\"label\":\"\""));
		assertFalse("A framework agent has a summary: " + agent, agent.contains("\"summary\":\"\""));
	}

	/**
	 * What the menu shows is what the installation configured, not what the product ships (#403).
	 *
	 * <p>
	 * The test application overrides the display name of {@link #FRAMEWORK_AGENT} and says nothing
	 * about its summary - the shape of an administrator renaming one agent in the service editor of
	 * <em>TopLogic-Agents and Skills</em>. Both halves are asserted here, because they are what makes
	 * the setting worth having: the renamed text reaches the client, and the text beside it is still
	 * the shipped one rather than empty.
	 * </p>
	 */
	public void testCapabilitiesCarriesTheInstallationsOwnDisplayName() throws Exception {
		String agent = agentEntry(capabilities(Locale.ENGLISH), FRAMEWORK_AGENT);

		assertEquals("The name the installation configured is the name the menu shows: " + agent,
			OVERRIDDEN_NAME_EN, valueOf(agent, "label"));
		assertEquals("The summary nobody overrode is still the shipped one: " + agent,
			SHIPPED_SUMMARY_EN, valueOf(agent, "summary"));
		assertTrue("The identifier a request names is not what was renamed: " + agent,
			agent.contains("\"name\":\"" + FRAMEWORK_AGENT + "\""));
	}

	/**
	 * Both texts are answered in the reader's language - the overridden one and the shipped one.
	 *
	 * <p>
	 * They are localized separately, so a German reader gets the installation's German name and the
	 * product's German summary in the same payload.
	 * </p>
	 */
	public void testCapabilitiesAnswersInTheReadersLanguage() throws Exception {
		String agent = agentEntry(capabilities(Locale.GERMAN), FRAMEWORK_AGENT);

		assertEquals("The installation's German name: " + agent,
			OVERRIDDEN_NAME_DE, valueOf(agent, "label"));
		assertEquals("The shipped German summary: " + agent,
			SHIPPED_SUMMARY_DE, valueOf(agent, "summary"));
	}

	/**
	 * An agent with no user-facing line is offered by its name alone.
	 *
	 * <p>
	 * Empty rather than filled with the agent's {@code description}: that one is a delegation
	 * instruction addressed to a model, and showing it to a person is what once filled the picker with
	 * English model instructions.
	 * </p>
	 */
	public void testAgentWithoutASummaryIsOfferedByNameAlone() throws Exception {
		String agent = agentEntry(capabilities(), AGENT_WITHOUT_SUMMARY);

		assertTrue("An agent without a summary says nothing: " + agent,
			agent.contains("\"summary\":\"\"") || !agent.contains("\"summary\""));
		assertFalse("The model-facing description must not reach the client: " + agent,
			agent.contains("Delegate here"));
	}

	/** Choosing a model the account may not choose is refused, with the reason it is refused for. */
	public void testSendWithAForeignModelIsRefused() throws Exception {
		Recording response = send(anna, "{\"text\":\"hallo\",\"agent\":\"" + FRAMEWORK_AGENT
			+ "\",\"model\":\"" + OTHER_MODEL + "\"}");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response._status);
		String body = response._body.toString();
		assertTrue(body, body.contains("\"status\":\"forbidden\""));
		assertTrue("The client shows this to the user, so it cannot be empty: " + body,
			body.contains(OTHER_MODEL));
	}

	/** Attaching an MCP server the agent does not declare is refused too. */
	public void testSendAttachingAForeignServerIsRefused() throws Exception {
		Recording response = send(anna, "{\"text\":\"hallo\",\"agent\":\"" + FRAMEWORK_AGENT
			+ "\",\"mcpOverride\":true,\"mcpServers\":[\"some-server\"]}");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response._status);
		assertTrue(response._body.toString(), response._body.toString().contains("some-server"));
	}

	/**
	 * Naming the model the agent is already assigned is not choosing one.
	 *
	 * <p>
	 * The load-bearing case: the browser client sends the model it is showing on every turn, which is
	 * the agent's own until the user picks another. A rule that called that a choice would refuse every
	 * request.
	 * </p>
	 */
	public void testRegenerateWithTheAgentsOwnModelIsNotRefused() throws Exception {
		Recording response = post("/regenerate", anna, "{\"sessionId\":\"" + id() + "\",\"agent\":\""
			+ FRAMEWORK_AGENT + "\",\"model\":\"" + ASSIGNED_MODEL + "\"}");

		assertEquals("The gate must say no to the wrong request, not to every request.",
			HttpServletResponse.SC_OK, response._status);
		// The chat has no turn to answer again, which is how this request gets that far and starts
		// nothing: what is asserted is that it was not refused.
		assertTrue(response._body.toString(), response._body.toString().contains("\"status\":\"error\""));
	}

	/**
	 * A regenerate that is refused leaves the answer it was going to replace where it is.
	 *
	 * <p>
	 * The check has to run before the turn is removed. It used to be written after it, which would
	 * have deleted the last answer and then refused to produce a new one.
	 * </p>
	 */
	public void testRefusedRegenerateRemovesNothing() throws Exception {
		Recording response = post("/regenerate", anna, "{\"sessionId\":\"" + id() + "\",\"agent\":\""
			+ FRAMEWORK_AGENT + "\",\"model\":\"" + OTHER_MODEL + "\"}");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response._status);
		assertTrue(response._body.toString(), response._body.toString().contains("\"status\":\"forbidden\""));
		assertNotNull("The chat must still be there.", ChatHistoryService.findOwnedSessionById(id(), anna));
	}

	/**
	 * The entry point the payload reports is one this account may run.
	 *
	 * <p>
	 * The configured one is infrastructure: a client that names no agent is not asking for it, and a
	 * payload that reported it unfiltered would name an agent a restricted account may not have - and
	 * every client that sends no agent would then be refused by that name. The shipped legacy chat
	 * control is exactly such a client.
	 * </p>
	 */
	public void testEntryPointIsAnAgentThisAccountMayRun() throws Exception {
		String body = capabilities();
		String entryPoint = valueOf(body, "entryPointAgent");

		assertFalse("An account with agents to run must be told one: " + body, entryPoint.isEmpty());
		assertTrue("The entry point has to be one of the agents offered: " + body,
			body.contains("\"name\":\"" + entryPoint + "\""));
	}

	/**
	 * A send that names no agent is not refused for an agent nobody named.
	 *
	 * <p>
	 * The regression of the review's B1: the fallback used to be the configured entry point, so the
	 * legacy control - which never sends an agent - was answered 403 and showed nothing at all.
	 * </p>
	 */
	public void testSendWithoutAnAgentIsNotRefused() throws Exception {
		Recording response = send(anna, "{\"text\":\"hallo\"}");

		assertFalse("A send that names no agent must not be refused: " + response._body,
			HttpServletResponse.SC_FORBIDDEN == response._status);
	}

	/**
	 * A send that names no model at all is not refused for the model it is silently given.
	 *
	 * <p>
	 * Since the second round of #392 the run asks about its agent's <em>own</em> model as well, so
	 * that a {@code <deny>} on it cannot be escaped by saying nothing. This is the other side of that
	 * question: under the shipped default the assigned model is permitted, and a request that chooses
	 * nothing has to go through - which is every request of the legacy chat control.
	 * </p>
	 */
	public void testSendWithoutAModelIsNotRefused() throws Exception {
		Recording response = send(anna, "{\"text\":\"hallo\",\"agent\":\"" + FRAMEWORK_AGENT + "\"}");

		assertFalse("A send that chooses nothing must not be refused: " + response._body,
			HttpServletResponse.SC_FORBIDDEN == response._status);
	}

	/**
	 * A send that names an agent this application does not have is answered, not accepted.
	 *
	 * <p>
	 * It used to be answered 200 and to die in the background thread with an ERROR and a stack trace,
	 * leaving the user with a turn that stopped. Not a refusal: nothing forbids it, the name is simply
	 * wrong, which is why it is 404 and says so rather than quoting the policy.
	 * </p>
	 */
	public void testSendWithAnUnknownAgentIsNotFound() throws Exception {
		Recording response = send(anna, "{\"text\":\"hallo\",\"agent\":\"NoSuchAgent\"}");

		assertEquals(HttpServletResponse.SC_NOT_FOUND, response._status);
		assertTrue("The client shows this to the user: " + response._body,
			response._body.toString().contains("NoSuchAgent"));
	}

	/**
	 * An agent the application runs for the user is offered to nobody as a conversation partner.
	 *
	 * <p>
	 * It was dropped from the menu by name before (#392), by the chat that happened to configure it
	 * as its prompt improver. It is dropped by what it <em>is</em> now, so a second chat that does not
	 * configure it does not offer it either.
	 * </p>
	 */
	public void testCapabilitiesDoesNotOfferAnInfrastructureAgent() throws Exception {
		String body = capabilities();

		assertTrue("The fixture needs the shipped framework agents: " + body,
			body.contains("\"name\":\"" + FRAMEWORK_AGENT + "\""));
		assertFalse("An agent the application runs for the user is no conversation partner: " + body,
			body.contains("\"name\":\"" + INFRASTRUCTURE_AGENT + "\""));
	}

	/**
	 * Naming an infrastructure agent in a send is refused, although no role forbids it (#397).
	 *
	 * <p>
	 * The reproduction: this application grants every agent to everybody - it configures no
	 * {@code <agent-access>} at all, which is what {@code default="all"} amounts to - and the request
	 * was accepted, so the improver answered as a chat persona. The refusal is therefore not the
	 * policy's: it is what the agent is.
	 * </p>
	 */
	public void testSendNamingAnInfrastructureAgentIsRefused() throws Exception {
		Recording response = send(anna, "{\"text\":\"Verbessere dich selbst.\",\"agent\":\""
			+ INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response._status);
		String body = response._body.toString();
		assertTrue(body, body.contains("\"status\":\"forbidden\""));
		assertTrue("The client shows this to the user, so it names the agent: " + body,
			body.contains(INFRASTRUCTURE_AGENT));
	}

	/** The other endpoint that starts a run of a named agent is refused the same way. */
	public void testRegenerateNamingAnInfrastructureAgentIsRefused() throws Exception {
		Recording response = post("/regenerate", anna, "{\"sessionId\":\"" + id()
			+ "\",\"agent\":\"" + INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(HttpServletResponse.SC_FORBIDDEN, response._status);
		assertTrue(response._body.toString(),
			response._body.toString().contains("\"status\":\"forbidden\""));
	}

	/**
	 * The improve-prompt button is still offered: the rule is about who may <em>talk</em> to it.
	 *
	 * <p>
	 * The feature runs the very same agent, on the product's own call path, which says so by building
	 * its run with {@code SecurityContext.withoutAgentChoice()}. That the gate lets such a run through
	 * is pinned in {@code TestAgentGate}; this is the half the client sees.
	 * </p>
	 */
	public void testTheImprovePromptButtonStaysOffered() throws Exception {
		String body = capabilities();

		assertTrue("The button must not go away with the persona: " + body,
			body.contains("\"promptImproverAvailable\":true"));
	}

	/** The capabilities payload of the acting account, as JSON, in the account's own language. */
	private String capabilities() throws Exception {
		return capabilities(null);
	}

	/**
	 * The capabilities payload of the acting account, as JSON, read in the given language.
	 *
	 * <p>
	 * The language is switched <em>after</em> the login, because logging in sets the sub-session to
	 * the account's own locale - a switch made before it would be undone by it, and the payload would
	 * come back in the account's language whichever one the test named (#578). That is also the order
	 * a real reader has: logged in first, language chosen afterwards.
	 * </p>
	 *
	 * <p>
	 * Through {@code withLocale} rather than by setting the locale, so the test leaves the sub-session
	 * as it found it. Named explicitly in both directions, because the account's own language is
	 * neither obvious nor the point.
	 * </p>
	 *
	 * @param locale
	 *        The reader's language, or {@code null} for the account's own.
	 */
	private String capabilities(Locale locale) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = client().newInvocation(
			new GetMethodWebRequest("http://localhost/chat/capabilities"));
		HttpServletRequest request = invocation.getRequest();
		login(request, anna, response);
		if (locale == null) {
			_servlet.get(request, response.asResponse());
		} else {
			TLContextManager.getSubSession().withLocale(locale, () -> {
				try {
					_servlet.get(request, response.asResponse());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				return null;
			});
		}
		return response._body.toString();
	}

	/** The value of a top-level string field of a JSON object. */
	private static String valueOf(String json, String field) {
		String key = "\"" + field + "\":\"";
		int start = json.indexOf(key);
		if (start < 0) {
			return "";
		}
		int from = start + key.length();
		return json.substring(from, json.indexOf('"', from));
	}

	/** The JSON object of one agent in a capabilities payload. */
	private static String agentEntry(String capabilities, String agentName) {
		int start = capabilities.indexOf("\"name\":\"" + agentName + "\"");
		assertTrue("No agent '" + agentName + "' in " + capabilities, start >= 0);
		int open = capabilities.lastIndexOf('{', start);
		int close = capabilities.indexOf('}', start);
		return capabilities.substring(open, close + 1);
	}

	private Recording send(Person caller, String body) throws Exception {
		return post("/send", caller, body);
	}

	private Recording post(String path, Person caller, String body) throws Exception {
		Recording response = new Recording();
		InvocationContext invocation = client().newInvocation(new PostMethodWebRequest(
			"http://localhost/chat" + path,
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
			"application/json"));
		HttpServletRequest request = invocation.getRequest();
		login(request, caller, response);
		_servlet.post(request, response.asResponse());
		return response;
	}

	private static void login(HttpServletRequest request, Person caller, Recording response)
			throws Exception {
		ThreadContext.pushSuperUser();
		try {
			SessionService.getInstance().loginUser(request, response.asResponse(), caller);
		} finally {
			ThreadContext.popSuperUser();
		}
	}

	private static ServletUnitClient client() {
		return runner.newClient();
	}

	private static String id() {
		return annasChat.tHandle().getObjectName().toExternalForm();
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatRunAccess.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
