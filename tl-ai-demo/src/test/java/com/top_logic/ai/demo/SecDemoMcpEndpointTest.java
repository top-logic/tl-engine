/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import junit.framework.Test;

import com.top_logic.ai.mcp.server.authentication.McpAuthenticationFilter;
import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.authentication.McpSessionPrincipals;
import com.top_logic.ai.mcp.server.resources.InstanceResource;
import com.top_logic.ai.mcp.server.resources.TypeInstancesResource;
import com.top_logic.ai.mcp.server.tools.McpToolAdapter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeySecret;
import com.top_logic.service.openapi.server.authentication.AuthenticationFailure;
import com.top_logic.service.openapi.server.authentication.Authenticator;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.server.authentication.apikey.ServerAPIKeySecret;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.TLContext;

import jakarta.servlet.http.HttpServletRequest;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * End-to-end test that the MCP endpoint acts as the account it authenticated, and only reaches what
 * that account may reach (issue #261).
 *
 * <p>
 * The row that closes the live exposure is
 * {@link #testAnInstanceReadIsFilteredByTheCallersRights()}: before this, every MCP resource handler
 * read the {@code KnowledgeBase} inside {@code ThreadContextManager.inSystemInteraction(...)} - the
 * engine's super-user bypass - for a caller nobody had authenticated, so any object was readable by
 * TID. It is a <b>pair</b>, deliberately: an account that may not read the object getting nothing
 * proves nothing on its own, because a broken handler returns nothing too.
 * </p>
 *
 * <p>
 * Every call here binds its account through {@link McpCallerContext}, the way
 * {@code McpAuthenticationFilter} does for a real request, and then invokes the registered protocol
 * handler rather than the private read method - so what is under test is the wiring and not only the
 * filter. That the account is taken from that binding and never from the ambient
 * {@code TLContext} is itself asserted, in
 * {@link #testTheReadFollowsTheBoundAccountAndNotTheAmbientOne()}.
 * </p>
 *
 * <p>
 * The HTTP half - the status code and the {@code WWW-Authenticate} challenge - needs no application
 * and is asserted in tl-ai-mcp-server's {@code TestMcpAuthentication} and
 * {@code TestMcpAuthenticationFilter}. What needs a booted application is everything that involves a
 * real {@code Person}, which is what this test adds.
 * </p>
 */
public class SecDemoMcpEndpointTest extends SecDemoPersonaTest {

	/** The header the demo's API key would be sent in. */
	private static final String API_KEY_HEADER = "X-Api-Key";

	/** A key value for the test's own authentication configuration. */
	private static final String API_KEY = "test-api-key-value";

	/**
	 * A tool whose call leaves this application: {@code egress}, and {@link ToolMode#EXTERNAL} since
	 * issue #338.
	 */
	private static final String EXTERNAL_TOOL = "gitea.createGiteaIssue";

	// --- The live exposure -------------------------------------------------------------------------

	/**
	 * An instance read discloses the object to an account that may read it, and nothing to one that
	 * may not.
	 *
	 * <p>
	 * Anna partners on {@link #ALPHA} and holds no role on {@link #BETA}; both are instances of the
	 * same type. So the same URI shape gives her one answer and not the other, which is per-object
	 * filtering and the criterion the whole change exists for.
	 * </p>
	 *
	 * <p>
	 * The refusal is worded as "not found" on purpose: telling her the object exists but is hers to
	 * see would itself disclose that an object with that TID exists.
	 * </p>
	 */
	public void testAnInstanceReadIsFilteredByTheCallersRights() {
		String readable = readInstance(ANNA, tidOf(project(ALPHA)));
		assertHas("Anna partners on Alpha, so she reads it.", "\"tid\"", readable);
		assertHasNot("A readable object must not come back as an error.", "\"error\"", readable);

		String hidden = readInstance(ANNA, tidOf(project(BETA)));
		assertHas("Anna holds no role on Beta, so the read must not disclose it.",
			"\"error\"", hidden);
		assertHasNot("Not even the object's own attributes may leak.", "\"attributes\"", hidden);
	}

	/**
	 * The same read, from an account that may read neither project.
	 *
	 * <p>
	 * {@code Dora} holds {@code projectPartner} on the security root and on nothing else - the
	 * fixture's "write tools and nothing to write" persona. She reads no project at all, which is
	 * what makes the Anna pair above a statement about rights and not about the URI.
	 * </p>
	 */
	public void testAnAccountThatScopesNothingReadsNothing() {
		assertHas("Dora scopes no project.", "\"error\"", readInstance(DORA, tidOf(project(ALPHA))));
		assertHas("...and not the other one either.", "\"error\"",
			readInstance(DORA, tidOf(project(BETA))));
	}

	/**
	 * An unreadable attribute of a readable object is left out, and the object still renders.
	 *
	 * <p>
	 * Per-attribute Read, from the fixture's {@code <part name="pm:Ticket#body">} grant. The finer of
	 * the two decisions: collapsing them would either hide a readable ticket or disclose a hidden
	 * body.
	 * </p>
	 */
	public void testAnUnreadableAttributeIsLeftOutAndTheObjectRemains() {
		TLObject ticket = ticketOf(ALPHA);
		String forBen = readInstance(BEN, tidOf(ticket));

		assertHas("A ticket reader reads the ticket itself.", "\"tid\"", forBen);
		assertHasNot("...and not its body, which the fixture withholds from him.",
			"\"body\"", forBen);
	}

	/**
	 * A type listing counts and names only the instances the caller may read.
	 *
	 * <p>
	 * The count follows the filter because it is taken after it: a total over the unfiltered list
	 * would disclose that a second project exists, and a page taken from the unfiltered list would
	 * come back short with no explanation.
	 * </p>
	 */
	public void testATypeListingIsFilteredBeforeItIsCounted() {
		assertEquals("Precondition: the fixture seeds two projects.", 2, projects().size());

		assertHas("Anna partners on exactly one project.", "\"totalCount\": 1",
			listInstances(ANNA, "pm", "Project"));
		assertHas("A ticket reader holds no Read grant on pm:Project at all.", "\"totalCount\": 0",
			listInstances(BEN, "pm", "Project"));
	}

	/**
	 * The read follows the bound account, not whoever happens to own the thread's interaction.
	 *
	 * <p>
	 * This is why the handlers ask {@link McpCallerContext} rather than
	 * {@code TLContext.currentUser()}. {@code TLContextManager.inPersonContext} and
	 * {@code ThreadContextManager.inSystemInteraction} are both no-ops when an interaction is already
	 * bound, so a handler that trusted the ambient context would serve whatever context it found -
	 * which, before this change, was the engine's system interaction. Here the ambient account is
	 * deliberately the wrong one.
	 * </p>
	 */
	public void testTheReadFollowsTheBoundAccountAndNotTheAmbientOne() {
		// Ambient: Anna, who may read Alpha. Bound caller: Ben, who may not.
		actAs(ANNA);

		String asBen = McpCallerContext.withCaller(account(BEN),
			() -> instanceHandler().apply(null, readRequest(tidOf(project(ALPHA)))))
			.contents().get(0).toString();

		assertHas("The disclosure must follow the authenticated caller, not the thread.",
			"\"error\"", asBen);
	}

	// --- Tool execution ----------------------------------------------------------------------------

	/**
	 * A tool call over MCP executes, and is authorized against the calling account's roles.
	 *
	 * <p>
	 * The half of issue #261 that used to be closed by refusing every call: an authenticated caller
	 * now reaches the tools its account reaches. {@code Ben} holds {@code model.read}, so the model
	 * browser answers him.
	 * </p>
	 */
	public void testAnAuthenticatedToolCallExecutes() {
		McpSchema.CallToolResult result = callTool(BEN, MODEL_READ_TOOL, Map.of());

		assertFalse("A granted tool must execute, not be refused: " + text(result),
			Boolean.TRUE.equals(result.isError()));
	}

	/**
	 * A capability the calling account holds but the background allowlist withholds is refused.
	 *
	 * <p>
	 * An MCP caller has nobody to answer an approval prompt, so the run is a background run and
	 * {@code <background-allowlist>} is its ceiling. {@code Anna} holds {@code data.write}, and the
	 * demo policy deliberately leaves {@code data.write} off the allowlist - so the same account and
	 * the same tool are reachable from the chat UI and not from here.
	 * </p>
	 */
	public void testACapabilityWithheldInBackgroundIsRefused() {
		assertAllowed(ANNA, CLASSIFIED_WRITE_SCRIPT_TOOL);

		McpSchema.CallToolResult result = callTool(ANNA, CLASSIFIED_WRITE_SCRIPT_TOOL, Map.of());

		assertTrue("An unattended run must not reach a data.write tool under this policy.",
			Boolean.TRUE.equals(result.isError()));
		assertHas("The refusal must be the gate's own message, naming the allowlist.",
			"background allowlist", text(result));
	}

	/**
	 * A tool call over MCP sees only what the calling account may read.
	 *
	 * <p>
	 * The other half of "authorized against that person's roles": the ceiling decides <em>which</em>
	 * tools an unattended run reaches, and the account's model access rights decide what those tools
	 * return. {@code data.read} is on the demo's background allowlist and both personas hold it, so
	 * the same tool runs for both - and answers differently, per object.
	 * </p>
	 */
	public void testAToolCallSeesOnlyWhatTheAccountMayRead() {
		Map<String, Object> args = Map.of("moduleName", "pm", "typeName", "Project");

		assertHas("Anna partners on exactly one of the two seeded projects.", "\"totalCount\":1",
			text(callTool(ANNA, LIST_TOOL, args)));
		assertHas("A ticket reader holds no Read grant on pm:Project at all.", "\"totalCount\":0",
			text(callTool(BEN, LIST_TOOL, args)));
	}

	/**
	 * An MCP caller cannot make a call that leaves this application.
	 *
	 * <p>
	 * Where issue #338 meets this one. #338 classifies every tool that calls out to another system -
	 * a remote MCP tool, the Gitea issue tool - as {@code ToolMode.EXTERNAL}, whose only control is
	 * the approval prompt: the remote system commits by itself, so no rollback and no retry can take
	 * the call back. An MCP-server caller has no prompt to answer, so an unattended run must not
	 * reach one, and the ceiling that stops it is the {@code <background-allowlist>} - {@code egress}
	 * is deliberately off the demo's. Without that, a chain {@code /mcp/*} → agent tool → external
	 * system would have gone out auto-approved.
	 * </p>
	 */
	public void testAnMcpCallerCannotReachAnExternalSystem() {
		assertEquals("Precondition: the tool this asserts about is the one #338 classifies.",
			ToolMode.EXTERNAL, tool(EXTERNAL_TOOL).mode());

		McpSchema.CallToolResult result = callTool(ANNA, EXTERNAL_TOOL, Map.of());

		assertTrue("An unattended run must not send data out of the application.",
			Boolean.TRUE.equals(result.isError()));
		assertHas("The refusal must be the ceiling's, because that is the control that applies.",
			"background allowlist", text(result));
	}

	/**
	 * A tool call with no authenticated account is refused rather than run.
	 *
	 * <p>
	 * Defence in depth behind the filter: an adapter that fell back to a system context here would
	 * undo the whole change, so the absence of an account is a refusal and not a mode.
	 * </p>
	 */
	public void testAToolCallWithoutAnAccountIsRefused() {
		assertNull("Precondition: no MCP caller is bound on this thread.", McpCallerContext.caller());

		McpSchema.CallToolResult result =
			toolHandler(MODEL_READ_TOOL).apply(null, new McpSchema.CallToolRequest("t", Map.of()));

		assertTrue("A call with no account must not execute.", Boolean.TRUE.equals(result.isError()));
		assertHas("The refusal must say what is missing.", "account", text(result));
	}

	// --- The filter, with a real account ------------------------------------------------------------

	/**
	 * An authenticated request reaches the protocol as its own account.
	 *
	 * <p>
	 * Both bindings are asserted, because the handlers need both: the account itself (which the
	 * resource and tool handlers read) and the {@code TLContext} of that account (which the
	 * {@code KnowledgeBase} and any TL-Script the request reaches need).
	 * </p>
	 *
	 * <p>
	 * Runs on a thread of its own, because a filter runs on a servlet request thread that carries no
	 * TopLogic interaction - and the test thread does.
	 * </p>
	 */
	public void testAnAuthenticatedRequestActsAsItsAccount() {
		Person anna = account(ANNA);
		Recording recording = new Recording();

		onARequestThread(() -> McpAuthenticationFilter.guard(authenticating(anna),
			new McpSessionPrincipals(), request(recording), response(recording),
			(req, resp) -> {
				recording.chainInvoked = true;
				recording.boundCaller = McpCallerContext.caller();
				recording.ambientUser = TLContext.currentUser();
			}));

		assertTrue("An authenticated request must reach the protocol.", recording.chainInvoked);
		assertEquals("The handlers read the account from this binding.", anna, recording.boundCaller);
		assertEquals("The KnowledgeBase and TL-Script read it from the TLContext.", anna,
			recording.ambientUser);
	}

	/**
	 * A second account cannot attach to the MCP session the first one opened.
	 *
	 * <p>
	 * The transport keys every request after {@code initialize} by the {@code mcp-session-id} header
	 * and keeps no owner of its own, so authenticating each request is not enough: two accounts that
	 * both hold a valid credential would otherwise take turns on one session.
	 * </p>
	 */
	public void testASecondAccountCannotUseTheFirstOnesSession() {
		McpSessionPrincipals sessions = new McpSessionPrincipals();
		sessions.bind("session-1", ANNA);

		Person ben = account(BEN);
		Recording recording = new Recording();
		recording.requestHeaders.put("Mcp-Session-Id", "session-1");

		onARequestThread(() -> McpAuthenticationFilter.guard(authenticating(ben), sessions,
			request(recording), response(recording), (req, resp) -> recording.chainInvoked = true));

		assertFalse("Ben must not be served on Anna's session.", recording.chainInvoked);
		assertEquals(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN, recording.status);
	}

	/**
	 * The account that opens a session owns it from then on.
	 *
	 * <p>
	 * The ownership is recorded after the chain, because that is when an {@code initialize} request
	 * has a session id at all: the transport creates the session while handling the request and puts
	 * the id into the response.
	 * </p>
	 */
	public void testOpeningASessionClaimsIt() {
		Person anna = account(ANNA);
		McpSessionPrincipals sessions = new McpSessionPrincipals();
		Recording recording = new Recording();

		onARequestThread(() -> McpAuthenticationFilter.guard(authenticating(anna), sessions,
			request(recording), response(recording),
			// What the transport does while answering an initialize request.
			(req, resp) -> ((jakarta.servlet.http.HttpServletResponse) resp)
				.setHeader("Mcp-Session-Id", "session-new")));

		assertTrue("Anna opened it, so Anna may use it.", sessions.mayUse("session-new", ANNA));
		assertFalse("...and nobody else may.", sessions.mayUse("session-new", BEN));
	}

	/**
	 * A request whose thread already carries an interaction is refused, not served as somebody else.
	 *
	 * <p>
	 * {@code TLContextManager.inPersonContext} passes straight through when an interaction is bound,
	 * so binding the authenticated account would silently fail and the request would act as whoever
	 * that interaction belongs to. Nothing in the shipped deployment produces this -
	 * {@code MCPTransportServlet} is a plain servlet with no interaction-establishing filter in front
	 * of it - and the guard exists because the failure it prevents is an identity confusion no test
	 * would catch by accident. This test thread has an interaction, which is what makes the guard
	 * observable here.
	 * </p>
	 */
	public void testARequestOnAForeignInteractionIsRefused() {
		assertNotNull("Precondition: this test thread carries an interaction.",
			com.top_logic.basic.thread.ThreadContextManager.getInteraction());

		Recording recording = new Recording();
		try {
			McpAuthenticationFilter.guard(authenticating(account(ANNA)), new McpSessionPrincipals(),
				request(recording), response(recording), (req, resp) -> recording.chainInvoked = true);
		} catch (Exception ex) {
			throw new AssertionError("The guard must answer, not throw.", ex);
		}

		assertFalse("A request that cannot be bound to its account must not be served.",
			recording.chainInvoked);
		assertEquals(jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
			recording.status);
	}

	// --- Authentication ----------------------------------------------------------------------------

	/**
	 * A valid API key resolves to the account the secret names.
	 *
	 * <p>
	 * The credential half, against a real account: the framework's {@code APIKeyAuthenticator} looks
	 * the login name up in the {@code PersonManager}, so this needs the booted application that
	 * tl-ai-mcp-server's own tests do not have.
	 * </p>
	 */
	public void testAValidApiKeyResolvesToTheConfiguredAccount() throws AuthenticationFailure {
		Authenticator authenticator = apiKeyAuthenticatorFor(ANNA);

		Person resolved = authenticator.authenticate(
			requestWithHeader(API_KEY_HEADER, API_KEY), null);

		assertEquals("The request must act as the account the secret names.", account(ANNA), resolved);
	}

	/**
	 * A wrong API key resolves to nobody at all.
	 */
	public void testAWrongApiKeyIsRefused() {
		Authenticator authenticator = apiKeyAuthenticatorFor(ANNA);

		try {
			Person resolved =
				authenticator.authenticate(requestWithHeader(API_KEY_HEADER, "wrong"), null);
			fail("A wrong key must be refused, and it resolved to " + resolved + ".");
		} catch (AuthenticationFailure expected) {
			// The only acceptable outcome.
		}
	}

	// --- Helpers -----------------------------------------------------------------------------------

	/** Reads {@code toplogic://instance/{tid}} as the given persona, through the registered handler. */
	private String readInstance(String login, String tid) {
		return contentOf(McpCallerContext.withCaller(account(login),
			() -> instanceHandler().apply(null, readRequest(tid))));
	}

	/** Reads the instances of a type as the given persona, through the registered handler. */
	private String listInstances(String login, String module, String type) {
		String uri = "toplogic://model/modules/" + module + "/types/" + type + "/instances";
		return contentOf(McpCallerContext.withCaller(account(login),
			() -> TypeInstancesResource.createSpecification().readHandler()
				.apply(null, new McpSchema.ReadResourceRequest(uri))));
	}

	/** Calls a registered tool as the given persona, through the MCP tool handler. */
	private McpSchema.CallToolResult callTool(String login, String qualifiedToolName,
			Map<String, Object> args) {
		return McpCallerContext.withCaller(account(login),
			() -> toolHandler(qualifiedToolName)
				.apply(null, new McpSchema.CallToolRequest(qualifiedToolName, new HashMap<>(args))));
	}

	private static BiFunction<McpSyncServerExchange, McpSchema.CallToolRequest,
			McpSchema.CallToolResult> toolHandler(String qualifiedToolName) {
		McpServerFeatures.SyncToolSpecification specification =
			McpToolAdapter.adapt(tool(qualifiedToolName), qualifiedToolName);
		return specification.callHandler();
	}

	private static BiFunction<McpSyncServerExchange, McpSchema.ReadResourceRequest,
			McpSchema.ReadResourceResult> instanceHandler() {
		return InstanceResource.createSpecification().readHandler();
	}

	private static McpSchema.ReadResourceRequest readRequest(String tid) {
		return new McpSchema.ReadResourceRequest("toplogic://instance/" + tid);
	}

	/** The text of the single content item of a resource result. */
	private static String contentOf(McpSchema.ReadResourceResult result) {
		assertEquals("A resource read answers with exactly one content item.", 1,
			result.contents().size());
		return ((McpSchema.TextResourceContents) result.contents().get(0)).text();
	}

	/** The text of a tool result, for an assertion message. */
	private static String text(McpSchema.CallToolResult result) {
		StringBuilder buffer = new StringBuilder();
		for (McpSchema.Content content : result.content()) {
			if (content instanceof McpSchema.TextContent text) {
				buffer.append(text.text());
			}
		}
		return buffer.toString();
	}

	private static String tidOf(TLObject object) {
		return object.tId().asString();
	}

	/**
	 * An API key authentication whose single secret maps {@link #API_KEY} to the given persona.
	 */
	private static Authenticator apiKeyAuthenticatorFor(String login) {
		APIKeyAuthentication.Config<?> authentication =
			TypedConfiguration.newConfigItem(APIKeyAuthentication.Config.class);
		authentication.setDomain("mcp");
		authentication.setPosition(APIKeyPosition.HEADER);
		authentication.setParameterName(API_KEY_HEADER);

		return TypedConfigUtil.createInstance(authentication)
			.createAuthenticator(List.of(secret(login)));
	}

	/** A secret naming {@link #API_KEY} and the account a request with it acts as. */
	private static SecretConfiguration secret(String login) {
		ServerAPIKeySecret secret = TypedConfiguration.newConfigItem(ServerAPIKeySecret.class);
		TypedConfigUtil.setProperty(secret,
			APIKeySecret.DOMAIN, "mcp");
		TypedConfigUtil.setProperty(secret,
			APIKeySecret.API_KEY, API_KEY);
		TypedConfigUtil.setProperty(secret,
			ServerAPIKeySecret.USER_ID, login);
		return secret;
	}

	/**
	 * A request carrying one header, for the authenticators that read exactly that.
	 */
	private static HttpServletRequest requestWithHeader(String name, String value) {
		return (HttpServletRequest) Proxy.newProxyInstance(
			SecDemoMcpEndpointTest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> {
				if ("getHeader".equals(method.getName())) {
					return name.equalsIgnoreCase((String) args[0]) ? value : null;
				}
				if ("toString".equals(method.getName())) {
					return "request with " + name;
				}
				throw new UnsupportedOperationException(method.getName());
			});
	}

	/**
	 * An authenticator that resolves every request to the given account.
	 */
	private static Authenticator authenticating(Person account) {
		return (req, resp) -> account;
	}

	/**
	 * Runs {@code body} on a thread that carries no TopLogic interaction, the way a servlet request
	 * thread does, and relays whatever it threw.
	 *
	 * <p>
	 * Resolve any {@code Person} the body needs <b>before</b> calling this: a {@code KnowledgeBase}
	 * read is what the thread deliberately cannot do, which is the whole point of the thread.
	 * </p>
	 */
	private static void onARequestThread(ThrowingRunnable body) {
		List<Throwable> problems = new java.util.ArrayList<>();
		Thread thread = new Thread(() -> {
			try {
				body.run();
			} catch (Throwable ex) {
				problems.add(ex);
			}
		}, "mcp-request-thread");
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new AssertionError("Interrupted while waiting for the request thread.", ex);
		}
		if (!problems.isEmpty()) {
			throw new AssertionError("The request thread failed.", problems.get(0));
		}
	}

	/** A test body that may throw whatever the filter throws. */
	private interface ThrowingRunnable {
		/** Runs the body. */
		void run() throws Exception;
	}

	/**
	 * What one filter run carried and what it answered.
	 */
	private static final class Recording {

		final Map<String, String> requestHeaders = new HashMap<>();

		final Map<String, String> responseHeaders = new HashMap<>();

		int status;

		boolean chainInvoked;

		Person boundCaller;

		Person ambientUser;
	}

	/**
	 * A request reading its headers from {@code recording}.
	 *
	 * <p>
	 * A dynamic proxy rather than a stub class: {@code HttpServletRequest} declares some sixty
	 * methods and the filter calls three of them. A method the filter is not expected to call throws,
	 * so a change in the filter shows up here rather than reading a silent default.
	 * </p>
	 */
	private static HttpServletRequest request(Recording recording) {
		return (HttpServletRequest) Proxy.newProxyInstance(
			SecDemoMcpEndpointTest.class.getClassLoader(),
			new Class<?>[] { HttpServletRequest.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getHeader" -> recording.requestHeaders.get(args[0]);
				case "getMethod" -> "POST";
				case "getRequestURI" -> "/mcp";
				case "toString" -> "MCP request";
				default -> throw new UnsupportedOperationException(method.getName());
			});
	}

	/** A response recording status and headers into {@code recording}. */
	private static jakarta.servlet.http.HttpServletResponse response(Recording recording) {
		return (jakarta.servlet.http.HttpServletResponse) Proxy.newProxyInstance(
			SecDemoMcpEndpointTest.class.getClassLoader(),
			new Class<?>[] { jakarta.servlet.http.HttpServletResponse.class },
			(proxy, method, args) -> {
				switch (method.getName()) {
					case "sendError":
						recording.status = ((Integer) args[0]).intValue();
						return null;
					case "setHeader":
						recording.responseHeaders.put((String) args[0], (String) args[1]);
						return null;
					case "getHeader":
						return recording.responseHeaders.get(args[0]);
					case "toString":
						return "MCP response";
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	private static void assertHas(String message, String expected, String actual) {
		assertTrue(message + " Looked for '" + expected + "' in: " + actual, actual.contains(expected));
	}

	private static void assertHasNot(String message, String forbidden, String actual) {
		assertFalse(message + " Found '" + forbidden + "' in: " + actual, actual.contains(forbidden));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoMcpEndpointTest.class);
	}
}
