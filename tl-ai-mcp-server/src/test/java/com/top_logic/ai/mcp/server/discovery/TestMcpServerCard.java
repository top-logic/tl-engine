/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.ai.mcp.server.authentication.HttpDoubles;
import com.top_logic.ai.mcp.server.authentication.HttpDoubles.Recorded;
import com.top_logic.base.discovery.AgentDiscoveryServlet;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.servlet.CacheControlFilter;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.service.openapi.common.authentication.apikey.APIKeyPosition;
import com.top_logic.service.openapi.server.authentication.apikey.APIKeyAuthentication;
import com.top_logic.service.openapi.server.authentication.conf.EndpointCredential;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.service.openapi.server.authentication.http.basic.BasicAuthentication;
import com.top_logic.tl.ai.core.access.AccessTokenAuthentication;
import com.top_logic.tl.ai.core.access.EndpointAuthentication;
import com.top_logic.util.Resources;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.TypeRef;

/**
 * Tests the MCP server card ({@link McpServerCard}), how it is served ({@link ServerCardResource}),
 * and how the endpoint is described in <code>llms.txt</code> and the AI Catalog
 * ({@link McpConnectionGuide}, {@link McpDiscoveryContribution}).
 */
public class TestMcpServerCard extends TestCase {

	private static final String APP = "https://apps.haumacher.de/ehome";

	private static final DiscoveryContext CONTEXT = new DiscoveryContext(APP);

	/**
	 * The service's configuration is polymorphic and the texts come from the resource bundles.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestMcpServerCard.class));
	}

	/** The name is derived from the application URL: host reversed, last context segment. */
	public void testDefaultName() {
		assertEquals("de.haumacher.apps/ehome", name(null, APP));
		assertEquals("de.haumacher.apps/mcp", name(null, "https://apps.haumacher.de"));
		assertEquals("localhost/app", name(null, "http://localhost:8080/app"));
		assertEquals("example.h/a-b", name(null, "https://h.example/x/a+b"));
		assertEquals("local/mcp", name(null, "not a url"));
	}

	/** A configured name is used, made valid where it is not. */
	public void testConfiguredName() {
		assertEquals("com.example/orders", name("com.example/orders", APP));
		assertEquals("de.haumacher.apps/Order-book", name("Order book", APP));
		assertEquals("com-example/a-b", name("com_example/a/b", APP));

		String longName = name("x".repeat(300) + "/" + "y".repeat(300), APP);
		assertTrue(longName.length() <= McpServerCard.MAX_NAME_LENGTH);
		assertValidName(longName);
	}

	/** Every required field is there, with the media type's field names and no tools or secrets. */
	public void testCardFields() throws IOException {
		MCPServerService.Config<?> config = config();
		set(config, MCPServerService.Config.SERVER_NAME, "Order book");
		set(config, MCPServerService.Config.SERVER_VERSION, "2.1.0");

		Map<String, Object> card = json(McpServerCard.create(config, credentials(personalAccessTokens()), CONTEXT,
			Resources.getLogInstance()));

		assertEquals(McpServerCard.SCHEMA_URL, card.get("$schema"));
		assertEquals("de.haumacher.apps/ehome", card.get("name"));
		assertValidName((String) card.get("name"));
		assertEquals("2.1.0", card.get("version"));
		assertEquals(Resources.getLogInstance().getString(I18NConstants.CARD_DESCRIPTION__SERVER.fill("Order book")),
			card.get("description"));
		assertEquals("Order book", card.get("title"));
		assertEquals(APP, card.get("websiteUrl"));
		assertFalse(card.containsKey("tools"));
		assertFalse(card.containsKey("resources"));

		List<Map<String, Object>> remotes = asList(card.get("remotes"));
		assertEquals(1, remotes.size());
		Map<String, Object> remote = remotes.get(0);
		assertEquals("streamable-http", remote.get("type"));
		assertEquals(APP + "/mcp", remote.get("url"));

		List<Map<String, Object>> headers = asList(remote.get("headers"));
		assertEquals(1, headers.size());
		Map<String, Object> header = headers.get(0);
		assertEquals("Authorization", header.get("name"));
		assertEquals(Boolean.TRUE, header.get("isRequired"));
		assertEquals(Boolean.TRUE, header.get("isSecret"));
		String description = (String) header.get("description");
		assertTrue(description, description.contains(Resources.getLogInstance()
			.getString(com.top_logic.tl.ai.core.access.I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_VALUE)));
	}

	/** A configured description is used, cut to the length the schema allows. */
	public void testDescriptionIsTruncated() throws IOException {
		MCPServerService.Config<?> config = config();
		set(config, MCPServerService.Config.DESCRIPTION, "d".repeat(150));

		Map<String, Object> card = json(McpServerCard.create(config, List.of(), CONTEXT, Resources.getLogInstance()));

		assertEquals("d".repeat(McpServerCard.MAX_TEXT_LENGTH), card.get("description"));
	}

	/**
	 * A header is required only if it carries every accepted credential; a credential not sent in a
	 * header gets no header entry.
	 */
	public void testHeaders() throws IOException {
		List<EndpointCredential> mixed = credentials(personalAccessTokens(), apiKey(APIKeyPosition.QUERY));
		Map<String, Object> header = asList(remote(mixed).get("headers")).get(0);
		assertEquals("Authorization", header.get("name"));
		assertEquals("A client may also use the query parameter.", Boolean.FALSE, header.get("isRequired"));

		List<EndpointCredential> twoHeaders = credentials(basic(), apiKey(APIKeyPosition.HEADER));
		List<Map<String, Object>> headers = asList(remote(twoHeaders).get("headers"));
		assertEquals(2, headers.size());
		assertEquals("Authorization", headers.get(0).get("name"));
		assertEquals("X-Api-Key", headers.get(1).get("name"));
		assertEquals(Boolean.FALSE, headers.get(0).get("isRequired"));
		assertEquals(Boolean.FALSE, headers.get(1).get("isRequired"));

		assertEquals(List.of(), remote(credentials(apiKey(APIKeyPosition.QUERY))).get("headers"));
	}

	/** The card is answered with its media type, CORS, caching and an entity tag. */
	public void testServedWithHeaders() throws IOException {
		ServerCard card = McpServerCard.create(config(), List.of(), CONTEXT, Resources.getLogInstance());

		Recorded get = request("GET", null);
		ServerCardResource.answer(HttpDoubles.request(get), HttpDoubles.response(get), () -> card);

		assertEquals(HttpServletResponse.SC_OK, get.status);
		assertEquals(McpServerCard.MEDIA_TYPE, get.contentType);
		assertEquals("*", get.responseHeader("Access-Control-Allow-Origin"));
		assertEquals("GET", get.responseHeader("Access-Control-Allow-Methods"));
		assertEquals("Content-Type, If-None-Match", get.responseHeader("Access-Control-Allow-Headers"));
		assertEquals("ETag", get.responseHeader("Access-Control-Expose-Headers"));
		assertEquals("public, max-age=3600", get.responseHeader(CacheControlFilter.CACHE_CONTROL_HEADER));
		String etag = get.responseHeader("ETag");
		assertNotNull(etag);
		assertTrue(etag, etag.startsWith("\"") && etag.endsWith("\""));
		String body = get.bytes.toString(StandardCharsets.UTF_8);
		assertEquals(json(card), readJson(body));
		assertEquals("The tag is a function of the body.", etag,
			ServerCardResource.etag(body.getBytes(StandardCharsets.UTF_8)));

		for (String ifNoneMatch : List.of(etag, "W/" + etag, "\"other\", " + etag, "*")) {
			Recorded cached = request("GET", ifNoneMatch);
			ServerCardResource.answer(HttpDoubles.request(cached), HttpDoubles.response(cached), () -> card);
			assertEquals(ifNoneMatch, HttpServletResponse.SC_NOT_MODIFIED, cached.status);
			assertEquals(etag, cached.responseHeader("ETag"));
			assertEquals(0, cached.bytes.size());
		}

		Recorded stale = request("GET", "\"other\"");
		ServerCardResource.answer(HttpDoubles.request(stale), HttpDoubles.response(stale), () -> card);
		assertEquals(HttpServletResponse.SC_OK, stale.status);
		assertTrue(stale.bytes.size() > 0);
	}

	/** The CORS preflight is answered without building the card. */
	public void testPreflight() throws IOException {
		Recorded options = request("OPTIONS", null);
		ServerCardResource.answer(HttpDoubles.request(options), HttpDoubles.response(options), () -> {
			throw new AssertionError("A preflight does not build the card.");
		});

		assertEquals(HttpServletResponse.SC_NO_CONTENT, options.status);
		assertEquals("*", options.responseHeader("Access-Control-Allow-Origin"));
		assertEquals("GET", options.responseHeader("Access-Control-Allow-Methods"));
		assertEquals("Content-Type, If-None-Match", options.responseHeader("Access-Control-Allow-Headers"));
	}

	/** Without a running service, the contribution describes nothing. */
	public void testContributionInactiveWithoutService() {
		assertNull("Precondition: no started MCPServerService.", MCPServerService.getInstance());
		McpDiscoveryContribution contribution = new McpDiscoveryContribution();

		assertFalse(contribution.isActive());
		assertEquals(List.of(), contribution.llmsTxtSections(CONTEXT));
		assertEquals(List.of(), contribution.catalogEntries(CONTEXT));
	}

	/** The catalog entry points to the card. */
	public void testCatalogEntry() {
		ServerCard card = McpServerCard.create(config(), List.of(), CONTEXT, Resources.getLogInstance());

		CatalogEntry entry = McpDiscoveryContribution.catalogEntry(card, CONTEXT);

		assertEquals("urn:air:apps.haumacher.de:mcp:ehome", entry.getIdentifier());
		assertEquals(McpServerCard.MEDIA_TYPE, entry.getType());
		assertEquals(APP + "/mcp/server-card", entry.getUrl());
		assertFalse(entry.hasData());
		assertEquals(card.getTitle(), entry.getDisplayName());
		assertEquals(card.getVersion(), entry.getVersion());
	}

	/**
	 * The <code>llms.txt</code> section names the endpoint, the card, the credentials, where to
	 * create a token, a command for Claude Code and the instructions.
	 */
	public void testLlmsTxtSection() {
		MCPServerService.Config<?> config = config();
		set(config, MCPServerService.Config.INSTRUCTIONS, "Identifiers are written Type:id.");
		Resources resources = Resources.getLogInstance();

		LlmsTxtSection section =
			McpConnectionGuide.llmsTxtSection(config, credentials(personalAccessTokens()), CONTEXT, resources);

		assertEquals(resources.getString(I18NConstants.LLMS_TITLE), section.title());
		String text = section.markdown();
		assertTrue(text, text.contains(APP + "/mcp "));
		assertTrue(text, text.contains(APP + "/mcp/,"));
		assertTrue(text, text.contains(APP + "/mcp/server-card"));
		assertTrue(text, text.contains(resources.getString(
			com.top_logic.tl.ai.core.access.I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_OBTAIN__URL
				.fill(APP + ViewServlet.ROOT_PATH))));
		assertTrue(text, text.contains("claude mcp add --transport http --scope user ehome " + APP + "/mcp"));
		assertTrue(text, text.contains("--header \"Authorization: " + resources.getString(
			com.top_logic.tl.ai.core.access.I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_VALUE) + "\""));
		assertTrue(text, text.contains(McpConnectionGuide.TOKEN_VARIABLE));
		assertTrue(text, text.contains("Identifiers are written Type:id."));
		assertFalse("No link to itself.", text.contains(AgentDiscoveryServlet.LLMS_TXT_PATH));
	}

	/** The command connecting Claude Code with a given token, on one line, ready to paste. */
	public void testClaudeMcpAddWithToken() {
		String command = McpConnectionGuide.claudeMcpAdd(config(), CONTEXT, EndpointCredential.AUTHORIZATION_HEADER,
			AccessTokenAuthentication.headerValue("tlpat_secret"), " ");

		assertEquals("claude mcp add --transport http --scope user ehome " + APP
			+ "/mcp --header \"Authorization: Bearer tlpat_secret\"", command);
	}

	/** Only an endpoint with a personal access token authentication accepts the tokens. */
	public void testAccessTokensAccepted() {
		assertTrue(AccessTokenAuthentication.isAccepted(List.of(basic().getValue(), personalAccessTokens().getValue())));
		assertFalse(AccessTokenAuthentication.isAccepted(List.of(basic().getValue())));
	}

	/**
	 * A credential the client obtains by itself names no header: a server offering it requires no
	 * header, and <code>llms.txt</code> shows the command without credential before the one with a
	 * header.
	 */
	public void testSignIn() throws IOException {
		EndpointCredential signIn = EndpointCredential.bySignIn(ResKey.text("OAuth sign-in"),
			context -> ResKey.text("Metadata at " + context.url("/.well-known/oauth-protected-resource/mcp")));
		List<EndpointCredential> credentials = new ArrayList<>();
		credentials.add(signIn);
		credentials.addAll(credentials(personalAccessTokens()));

		List<Map<String, Object>> headers = asList(remote(credentials).get("headers"));
		assertEquals(1, headers.size());
		assertEquals("Authorization", headers.get(0).get("name"));
		assertEquals("The sign-in needs no header.", Boolean.FALSE, headers.get(0).get("isRequired"));

		Resources resources = Resources.getLogInstance();
		String text = McpConnectionGuide.llmsTxtSection(config(), credentials, CONTEXT, resources).markdown();
		String plainCommand = "claude mcp add --transport http --scope user ehome " + APP + "/mcp\n";
		assertTrue(text, text.contains(plainCommand));
		assertTrue(text, text.contains("Metadata at " + APP + "/.well-known/oauth-protected-resource/mcp"));
		assertTrue(text, text.contains(resources.getString(I18NConstants.LLMS_CONNECT_CLAUDE_HEADER_ALTERNATIVE)));
		assertTrue("The command without credential comes first: " + text,
			text.indexOf(plainCommand) < text.indexOf("--header"));

		assertEquals(List.of(),
			remote(List.of(signIn)).get("headers"));
		String signInOnly = McpConnectionGuide.llmsTxtSection(config(), List.of(signIn), CONTEXT, resources).markdown();
		assertTrue(signInOnly, signInOnly.contains(plainCommand));
		assertFalse(signInOnly, signInOnly.contains("--header"));
	}

	/** Without a credential sent in a header, there is no command to show. */
	public void testLlmsTxtSectionWithoutHeaderCredential() {
		LlmsTxtSection section = McpConnectionGuide.llmsTxtSection(config(),
			credentials(apiKey(APIKeyPosition.QUERY)), CONTEXT, Resources.getLogInstance());

		assertFalse(section.markdown(), section.markdown().contains("claude mcp add"));
	}

	/** The PAT describes itself; the other schemes are described by their OpenAPI security scheme. */
	public void testCredentialDescriptions() {
		List<EndpointCredential> credentials =
			credentials(personalAccessTokens(), basic(), apiKey(APIKeyPosition.HEADER), apiKey(APIKeyPosition.QUERY));

		assertEquals(4, credentials.size());
		assertEquals(com.top_logic.tl.ai.core.access.I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN,
			credentials.get(0).credential());
		assertNotNull(credentials.get(0).obtain());
		assertEquals("Authorization", credentials.get(1).header());
		assertEquals(com.top_logic.service.openapi.server.authentication.conf.I18NConstants.CREDENTIAL_HTTP_BASIC_VALUE,
			credentials.get(1).headerValue());
		assertNull(credentials.get(1).obtain());
		assertEquals("X-Api-Key", credentials.get(2).header());
		assertFalse(credentials.get(3).isHeader());
	}

	private static String name(String configured, String applicationUrl) {
		MCPServerService.Config<?> config = config();
		set(config, MCPServerService.Config.CARD_NAME, configured);
		String result = McpServerCard.name(config, new DiscoveryContext(applicationUrl));
		assertValidName(result);
		return result;
	}

	private static void assertValidName(String name) {
		assertTrue(name, McpServerCard.NAME_PATTERN.matcher(name).matches());
		assertTrue(name, name.length() >= 3 && name.length() <= McpServerCard.MAX_NAME_LENGTH);
	}

	private static Map<String, Object> remote(List<EndpointCredential> credentials) throws IOException {
		return asList(json(McpServerCard.create(config(), credentials, CONTEXT, Resources.getLogInstance()))
			.get("remotes")).get(0);
	}

	private static Recorded request(String method, String ifNoneMatch) {
		Recorded recorded = new Recorded();
		recorded.method = method;
		recorded.path = McpServerCard.PATH_INFO;
		if (ifNoneMatch != null) {
			recorded.requestHeaders.put("If-None-Match", ifNoneMatch);
		}
		return recorded;
	}

	private static MCPServerService.Config<?> config() {
		return TypedConfiguration.newConfigItem(MCPServerService.Config.class);
	}

	@SafeVarargs
	private static List<EndpointCredential> credentials(
			Map.Entry<String, ServerAuthentication.Config<?>>... authentications) {
		Map<String, ServerAuthentication.Config<?>> map = new LinkedHashMap<>();
		for (Map.Entry<String, ServerAuthentication.Config<?>> entry : authentications) {
			map.put(entry.getKey(), entry.getValue());
		}
		return EndpointAuthentication.credentials(map);
	}

	private static Map.Entry<String, ServerAuthentication.Config<?>> personalAccessTokens() {
		AccessTokenAuthentication.Config<?> config =
			TypedConfiguration.newConfigItem(AccessTokenAuthentication.Config.class);
		config.setDomain("tokens");
		return Map.entry("tokens", config);
	}

	private static Map.Entry<String, ServerAuthentication.Config<?>> basic() {
		BasicAuthentication.Config<?> config = TypedConfiguration.newConfigItem(BasicAuthentication.Config.class);
		config.setDomain("basic");
		config.setInUserContext(true);
		return Map.entry("basic", config);
	}

	private static Map.Entry<String, ServerAuthentication.Config<?>> apiKey(APIKeyPosition position) {
		APIKeyAuthentication.Config<?> config = TypedConfiguration.newConfigItem(APIKeyAuthentication.Config.class);
		config.setDomain("key-" + position.getExternalName());
		set(config, APIKeyAuthentication.Config.PARAMETER_NAME, "X-Api-Key");
		set(config, APIKeyAuthentication.Config.POSITION, position);
		return Map.entry("key-" + position.getExternalName(), config);
	}

	private static Map<String, Object> json(ServerCard card) throws IOException {
		return readJson(new String(ServerCardResource.json(card), StandardCharsets.UTF_8));
	}

	private static Map<String, Object> readJson(String json) throws IOException {
		return McpJsonDefaults.getMapper().readValue(json, new TypeRef<>() {
			// Type capture.
		});
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> asList(Object value) {
		return (List<Map<String, Object>>) value;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}
}
