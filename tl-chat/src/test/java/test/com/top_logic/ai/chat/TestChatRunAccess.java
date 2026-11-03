/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.ai.chat;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.ChatCapabilities;
import com.top_logic.ai.chat.api.ChatTranscripts;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.ai.chat.view.ChatCommandDriver;
import com.top_logic.ai.chat.view.ChatControl;
import com.top_logic.ai.chat.view.ChatConversationControl;
import com.top_logic.base.accesscontrol.Login;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.json.JSON;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
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
 * The commands of the chat controls are run as the browser sends them ({@link ChatCommandDriver}).
 * </p>
 */
public class TestChatRunAccess extends BasicTestCase {

	/** The model the test application assigns to every agent. */
	private static final String ASSIGNED_MODEL = "test-assigned-model";

	/** The agent the test application assigns {@link #GATED_MODEL}. */
	private static final String GATED_AGENT = "TestGatedAgent";

	/** The model of a provider whose key is the secret {@link #GATED_KEY}. */
	private static final String GATED_MODEL = "test-gated-model";

	/** The secret holding the key of the provider of {@link #GATED_MODEL}, set only by a test. */
	private static final String GATED_KEY = "test_chat_gated_api_key";

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


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (anna == null) {
			anna = TestPerson.createPerson("anna-who-runs-agents");
			annasChat = ChatHistoryService.createSession("Annas Chat", anna);
		}

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

	/**
	 * A model whose provider has no key is not offered, and is offered as soon as the key is
	 * provided - without a restart, because the list is computed for each request.
	 *
	 * <p>
	 * {@link #GATED_MODEL} is the own model of {@link #GATED_AGENT}, so nothing but its provider's
	 * missing key keeps it out of the menu.
	 * </p>
	 */
	public void testCapabilitiesOffersNoModelWhoseProviderHasNoKey() throws Exception {
		assertNull(System.getProperty(GATED_KEY));
		assertNull(System.getenv(GATED_KEY));
		assertTrue("The model is configured, and only its key is missing.",
			LLMProviderService.getInstance().getConfiguredModelNames().contains(GATED_MODEL));

		String without = capabilities();
		assertTrue("The agent itself is still offered: " + without,
			without.contains("\"name\":\"" + GATED_AGENT + "\""));
		assertFalse("A model whose provider has no key must not be offered: " + without,
			without.contains("\"id\":\"" + GATED_MODEL + "\""));

		System.setProperty(GATED_KEY, "sk-test-gated");
		try {
			String with = capabilities();
			assertTrue("Once its provider has its key, the model is offered: " + with,
				with.contains("\"id\":\"" + GATED_MODEL + "\""));
		} finally {
			System.clearProperty(GATED_KEY);
		}
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
		Reply response = send(anna, "{\"text\":\"hallo\",\"agent\":\"" + FRAMEWORK_AGENT
			+ "\",\"model\":\"" + OTHER_MODEL + "\"}");

		assertEquals(ChatControl.STATUS_FORBIDDEN, response.status());
		String body = response.body();
		assertTrue(body, body.contains("status=forbidden"));
		assertTrue("The client shows this to the user, so it cannot be empty: " + body,
			body.contains(OTHER_MODEL));
	}

	/** Attaching an MCP server the agent does not declare is refused too. */
	public void testSendAttachingAForeignServerIsRefused() throws Exception {
		Reply response = send(anna, "{\"text\":\"hallo\",\"agent\":\"" + FRAMEWORK_AGENT
			+ "\",\"mcpOverride\":true,\"mcpServers\":[\"some-server\"]}");

		assertEquals(ChatControl.STATUS_FORBIDDEN, response.status());
		assertTrue(response.body(), response.body().contains("some-server"));
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
		Reply response = regenerate(anna, "{\"sessionId\":\"" + id() + "\",\"agent\":\""
			+ FRAMEWORK_AGENT + "\",\"model\":\"" + ASSIGNED_MODEL + "\"}");

		// The chat has no turn to answer again, which is how this request gets that far and starts
		// nothing: what is asserted is that it was not refused.
		assertEquals("The gate must say no to the wrong request, not to every request: " + response.body(),
			ChatControl.STATUS_ERROR, response.status());
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
		Reply response = regenerate(anna, "{\"sessionId\":\"" + id() + "\",\"agent\":\""
			+ FRAMEWORK_AGENT + "\",\"model\":\"" + OTHER_MODEL + "\"}");

		assertEquals(ChatControl.STATUS_FORBIDDEN, response.status());
		assertTrue(response.body(), response.body().contains("status=forbidden"));
		assertNotNull("The chat must still be there.", ChatHistoryService.findOwnedSessionById(id(), anna));
	}

	/**
	 * A first send creates the chat it goes to, titled as the browser named it, and answers with it:
	 * the conversation then shows that chat.
	 */
	public void testAFirstSendCreatesItsChat() throws Exception {
		ChatCommandDriver driver = new ChatCommandDriver("runAccessWindow", "run-access-" + getName());
		ChatConversationControl conversation = driver.conversation(anna);
		try {
			Map<String, Object> reply = driver.command(conversation, ChatControl.CMD_SEND,
				Map.of(ChatControl.ARG_TEXT, "hallo", ChatControl.ARG_TITLE, "Ein neuer Chat " + getName()));

			assertEquals(reply.toString(), ChatControl.STATUS_PROCESSING, reply.get(ChatControl.FIELD_STATUS));
			String created = (String) reply.get(ChatControl.FIELD_SESSION_ID);
			assertNotNull(reply.toString(), created);
			assertEquals(created, ChatCommandDriver.tracker(conversation).getActiveSessionId());
			assertEquals(List.of("Ein neuer Chat " + getName()), titled("Ein neuer Chat " + getName()));
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/**
	 * A conversation that has no chat yet keeps the auto-approve switch in the browser, which names it
	 * with the send that creates the chat: the chat stores it at once.
	 */
	public void testAChatIsCreatedWithTheModeItsSendNames() throws Exception {
		ChatCommandDriver driver = new ChatCommandDriver("runAccessWindow", "run-access-" + getName());
		ChatConversationControl conversation = driver.conversation(anna);
		try {
			Map<String, Object> reply = driver.command(conversation, ChatControl.CMD_SEND, Map.of(
				ChatControl.ARG_TEXT, "hallo", ChatControl.ARG_APPROVAL_MODE, ApprovalMode.AUTO_APPROVE.name()));
			String created = (String) reply.get(ChatControl.FIELD_SESSION_ID);
			assertNotNull(reply.toString(), created);
			HistoryUtils.updateSessionRevision();

			TaskProgressTracker fresh = new TaskProgressTracker();
			fresh.setActiveSessionId(created, anna);
			assertEquals(ApprovalMode.AUTO_APPROVE, fresh.getApprovalMode());
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
	}

	/**
	 * A first send that is refused keeps no chat: the chat it created is taken back, so the list holds
	 * no empty row the user has to tidy away.
	 */
	public void testARefusedFirstSendKeepsNoChat() throws Exception {
		Reply response = send(anna, "{\"text\":\"Verbessere dich selbst.\",\"agent\":\"" + INFRASTRUCTURE_AGENT
			+ "\",\"title\":\"Abgelehnt " + getName() + "\"}");

		assertEquals(ChatControl.STATUS_FORBIDDEN, response.status());
		assertFalse(response.body(), response.body().contains(ChatControl.FIELD_SESSION_ID));
		HistoryUtils.updateSessionRevision();
		assertEquals(List.of(), titled("Abgelehnt " + getName()));
	}

	/** The titles of Anna's chats that are the given title. */
	private static List<String> titled(String title) {
		return ChatTranscripts.sessionList(anna).stream()
			.map(ChatSessionDto::getTitle)
			.filter(title::equals)
			.toList();
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
		Reply response = send(anna, "{\"text\":\"hallo\"}");

		assertFalse("A send that names no agent must not be refused: " + response.body(),
			ChatControl.STATUS_FORBIDDEN.equals(response.status()));
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
		Reply response = send(anna, "{\"text\":\"hallo\",\"agent\":\"" + FRAMEWORK_AGENT + "\"}");

		assertFalse("A send that chooses nothing must not be refused: " + response.body(),
			ChatControl.STATUS_FORBIDDEN.equals(response.status()));
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
		Reply response = send(anna, "{\"text\":\"hallo\",\"agent\":\"NoSuchAgent\"}");

		assertEquals(ChatControl.STATUS_NOT_FOUND, response.status());
		assertTrue("The client shows this to the user: " + response.body(),
			response.body().contains("NoSuchAgent"));
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
		Reply response = send(anna, "{\"text\":\"Verbessere dich selbst.\",\"agent\":\""
			+ INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(ChatControl.STATUS_FORBIDDEN, response.status());
		String body = response.body();
		assertTrue(body, body.contains("status=forbidden"));
		assertTrue("The client shows this to the user, so it names the agent: " + body,
			body.contains(INFRASTRUCTURE_AGENT));
	}

	/** The other endpoint that starts a run of a named agent is refused the same way. */
	public void testRegenerateNamingAnInfrastructureAgentIsRefused() throws Exception {
		Reply response = regenerate(anna, "{\"sessionId\":\"" + id()
			+ "\",\"agent\":\"" + INFRASTRUCTURE_AGENT + "\"}");

		assertEquals(ChatControl.STATUS_FORBIDDEN, response.status());
		assertTrue(response.body(),
			response.body().contains("status=forbidden"));
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
	 * The capabilities payload of the acting account, as JSON, read in the given language - what a
	 * {@code <chat-conversation>} writes into its state on a render in that language (#578).
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
		TaskProgressTracker tracker = ChatSessionRegistry.tracker(TLContextManager.getSubSession(), null);
		String improver = tracker == null ? null : tracker.getPromptImproverAgentName();
		if (locale == null) {
			return DtoMapper.toJson(ChatCapabilities.build(anna, improver));
		}
		return TLContextManager.getSubSession().withLocale(locale,
			() -> DtoMapper.toJson(ChatCapabilities.build(anna, improver)));
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

	/** What a command answered: its status, and the whole reply for the messages. */
	private record Reply(String status, String body) {
		// Pure data.
	}

	private Reply send(Person caller, String body) throws Exception {
		return command(caller, ChatControl.CMD_SEND, body);
	}

	private Reply regenerate(Person caller, String body) throws Exception {
		return command(caller, ChatControl.CMD_REGENERATE, body);
	}

	/** Runs a command with the given JSON arguments in a conversation of the caller. */
	private Reply command(Person caller, String name, String body) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> arguments = (Map<String, Object>) JSON.fromString(body);
		ChatCommandDriver driver = new ChatCommandDriver("runAccessWindow", "run-access-" + getName());
		ChatConversationControl conversation = driver.conversation(caller);
		try {
			Map<String, Object> reply = driver.command(conversation, name, arguments);
			return new Reply((String) reply.get(ChatControl.FIELD_STATUS), reply.toString());
		} finally {
			conversation.cleanupTree();
			driver.close();
		}
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
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE,
				Login.Module.INSTANCE));
	}
}
