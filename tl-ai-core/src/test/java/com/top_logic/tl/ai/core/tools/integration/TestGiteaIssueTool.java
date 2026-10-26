/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.integration;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.registry.ReflectiveToolFactory;

/**
 * Tests how {@link GiteaIssueTool} authenticates its requests: with the access token in the secret
 * {@link GiteaIssueTool#TOKEN_SECRET} by default (read from the variable of that name), with the credentials configured on its
 * {@link ReflectiveToolFactory}, and once more with a fresh token after a rejection.
 */
public class TestGiteaIssueTool extends TestCase {

	private static final String URL_VARIABLE = "tl_gitea_url";

	private static final String OWNER_VARIABLE = "tl_gitea_repo_owner";

	private static final String REPO_VARIABLE = "tl_gitea_repo_name";

	private static final String TOOL_NAME = "createGiteaIssue";

	private HttpServer _server;

	/** The <code>Authorization</code> headers of the received requests. */
	private final List<String> _authorizations = new ArrayList<>();

	/** Decides from the <code>Authorization</code> header whether a request is accepted. */
	private Predicate<String> _accepted = authorization -> true;

	/**
	 * Parsing the factory configuration resolves the nested configuration types of the credentials
	 * through the <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestGiteaIssueTool.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
		System.setProperty(URL_VARIABLE, "http://127.0.0.1:" + _server.getAddress().getPort());
		System.setProperty(OWNER_VARIABLE, "owner");
		System.setProperty(REPO_VARIABLE, "repo");
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		System.clearProperty(URL_VARIABLE);
		System.clearProperty(OWNER_VARIABLE);
		System.clearProperty(REPO_VARIABLE);
		System.clearProperty(GiteaIssueTool.TOKEN_SECRET);
		super.tearDown();
	}

	/** Without configured credentials, the token of the variable is sent with the scheme of Gitea. */
	public void testDefaultToken() {
		System.setProperty(GiteaIssueTool.TOKEN_SECRET, "gitea-token");

		String result = createIssue(new GiteaIssueTool());

		assertTrue(result, result.startsWith("Issue created"));
		assertEquals(List.of("token gitea-token"), _authorizations);
	}

	/** Without configured credentials and token, no issue is created and the variable is named. */
	public void testMissingToken() {
		if (System.getenv(GiteaIssueTool.TOKEN_SECRET) != null) {
			// The token of the environment cannot be removed for the test.
			return;
		}
		String result = createIssue(new GiteaIssueTool());

		assertTrue(result, result.startsWith("Error:"));
		assertTrue(result, result.contains(GiteaIssueTool.TOKEN_SECRET));
		assertTrue(_authorizations.isEmpty());
	}

	/** The credentials configured on the tool of the factory are used. */
	public void testConfiguredCredentials() throws ConfigurationException {
		Tool tool = tool("<tool class='" + GiteaIssueTool.class.getName() + "'>"
			+ "<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer'"
			+ " secret='unencrypted:configured-token'/>"
			+ "</tool>");

		ToolResult result = tool.execute(Map.of("title", "T", "category", "tool-misuse", "severity", "low",
			"description", "D"), null);

		assertTrue(result.text(), result.text().contains("Issue created"));
		assertEquals(List.of("Bearer configured-token"), _authorizations);
	}

	/** The factory still instantiates a tool class by its no-argument constructor. */
	public void testToolClass() throws ConfigurationException {
		System.setProperty(GiteaIssueTool.TOKEN_SECRET, "gitea-token");

		Tool tool = tool("", "tool-class='" + GiteaIssueTool.class.getName() + "'");
		tool.execute(Map.of("title", "T", "category", "tool-misuse", "severity", "low", "description", "D"), null);

		assertEquals(List.of("token gitea-token"), _authorizations);
	}

	/** A rejected request is repeated once with a fresh token. */
	public void testRetryWithFreshToken() {
		System.setProperty(GiteaIssueTool.TOKEN_SECRET, "old-token");
		_accepted = authorization -> {
			if ("token old-token".equals(authorization)) {
				// The token is rotated while the request is rejected.
				System.setProperty(GiteaIssueTool.TOKEN_SECRET, "new-token");
				return false;
			}
			return true;
		};

		String result = createIssue(new GiteaIssueTool());

		assertTrue(result, result.startsWith("Issue created"));
		assertEquals(List.of("token old-token", "token new-token"), _authorizations);
	}

	/** A token rejected again is not tried a third time. */
	public void testRetryOnlyOnce() {
		System.setProperty(GiteaIssueTool.TOKEN_SECRET, "wrong-token");
		_accepted = authorization -> false;

		String result = createIssue(new GiteaIssueTool());

		assertTrue(result, result.contains("HTTP 401"));
		assertEquals(2, _authorizations.size());
	}

	private static String createIssue(GiteaIssueTool tool) {
		return tool.createGiteaIssue("T", "tool-misuse", "low", "D", null);
	}

	private Tool tool(String content) throws ConfigurationException {
		return tool(content, "");
	}

	private Tool tool(String content, String attributes) throws ConfigurationException {
		String xml = "<implementation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + ReflectiveToolFactory.Config.class.getName() + "'"
			+ " class='" + ReflectiveToolFactory.class.getName() + "' " + attributes + ">"
			+ content + "</implementation>";
		ReflectiveToolFactory.Config config =
			(ReflectiveToolFactory.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		ReflectiveToolFactory factory = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		for (Tool tool : factory.createTools()) {
			if (TOOL_NAME.equals(tool.name())) {
				return tool;
			}
		}
		throw new AssertionError("No tool " + TOOL_NAME + " in " + factory.createTools());
	}

	private void handle(HttpExchange exchange) throws IOException {
		exchange.getRequestBody().readAllBytes();
		String authorization = exchange.getRequestHeaders().getFirst(CredentialProvider.AUTHORIZATION);
		boolean accepted;
		synchronized (_authorizations) {
			_authorizations.add(authorization);
			accepted = _accepted.test(authorization);
		}
		int status = accepted ? HttpURLConnection.HTTP_CREATED : HttpURLConnection.HTTP_UNAUTHORIZED;
		byte[] body = "{}".getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

}
