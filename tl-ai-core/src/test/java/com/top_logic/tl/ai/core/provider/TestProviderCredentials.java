/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicMessagesProvider;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.tl.ai.core.provider.local.LocalProviderConfig;
import com.top_logic.tl.ai.core.provider.openai.AzureOpenAIProviderConfig;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProviderConfig;

/**
 * Tests how the LLM providers authenticate their requests: with the <code>api-key</code> shorthand
 * in the header the provider's API expects, with configured <code>credentials</code>, or not at all.
 *
 * <p>
 * A local HTTP server records the headers of the requests the providers send.
 * </p>
 */
public class TestProviderCredentials extends TestCase {

	private static final String AUTHORIZATION = "authorization";

	private static final String X_API_KEY = "x-api-key";

	private static final String AZURE_API_KEY = "api-key";

	private HttpServer _server;

	/** Headers of the received requests, names in lower case. */
	private final List<Map<String, String>> _requests = new ArrayList<>();

	/** Decides the HTTP status of a request from its headers. */
	private Function<Map<String, String>, Integer> _status = headers -> HttpURLConnection.HTTP_BAD_REQUEST;

	/**
	 * Parsing provider configurations resolves their nested configuration types through the
	 * <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestProviderCredentials.class)));
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		_server.createContext("/", this::handle);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		ModelDiscoveryHelper.clearCache();
		super.tearDown();
	}

	private void handle(HttpExchange exchange) throws IOException {
		Map<String, String> headers = new TreeMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
		int status;
		synchronized (_requests) {
			_requests.add(headers);
			status = _status.apply(headers);
		}
		exchange.getRequestBody().readAllBytes();
		byte[] body = (status == HttpURLConnection.HTTP_OK ? "{\"data\":[{\"id\":\"m1\"}]}" : "{}")
			.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, body.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(body);
		}
	}

	private String baseUrl() {
		return "http://127.0.0.1:" + _server.getAddress().getPort();
	}

	/** The <code>api-key</code> attribute of Anthropic is sent as bare <code>x-api-key</code>. */
	public void testAnthropicApiKey() throws Exception {
		Map<String, String> headers = streamOnce(provider(AnthropicProviderConfig.class,
			"api-key='unencrypted:ant-key'"));

		assertEquals("ant-key", headers.get(X_API_KEY));
		assertNull(headers.get(AUTHORIZATION));
		assertEquals("2023-06-01", headers.get(AnthropicMessagesProvider.VERSION_HEADER));
	}

	/** The <code>api-key</code> attribute of OpenAI is sent as bearer token. */
	public void testOpenAIApiKey() throws Exception {
		Map<String, String> headers = streamOnce(provider(OpenAIProviderConfig.class,
			"api-key='unencrypted:oai-key'"));

		assertEquals("Bearer oai-key", headers.get(AUTHORIZATION));
	}

	/** Azure OpenAI sends its <code>api-key</code> as bearer token by default. */
	public void testAzureBearer() throws Exception {
		Map<String, String> headers = streamOnce(provider(AzureOpenAIProviderConfig.class,
			"api-key='unencrypted:az-key'"));

		assertEquals("Bearer az-key", headers.get(AUTHORIZATION));
		assertNull(headers.get(AZURE_API_KEY));
	}

	/** Azure OpenAI with the subscription-key scheme sends its key in the <code>api-key</code> header. */
	public void testAzureApiKeyScheme() throws Exception {
		Map<String, String> headers = streamOnce(provider(AzureOpenAIProviderConfig.class,
			"api-key='unencrypted:az-key' auth-scheme='API_KEY'"));

		assertEquals("az-key", headers.get(AZURE_API_KEY));
		assertNull(headers.get(AUTHORIZATION));
	}

	/** A provider without API key and credentials sends no credentials. */
	public void testNoApiKey() throws Exception {
		Map<String, String> headers = streamOnce(provider(OpenAIProviderConfig.class, ""));

		assertNull(headers.get(AUTHORIZATION));
	}

	/** An API key given by an unset environment variable means no API key. */
	public void testEmptyApiKey() throws Exception {
		Map<String, String> headers = streamOnce(provider(AnthropicProviderConfig.class,
			"api-key='unencrypted:'"));

		assertNull(headers.get(X_API_KEY));
	}

	/** Configured credentials define the header themselves. */
	public void testCredentials() throws Exception {
		Map<String, String> headers = streamOnce(provider(AnthropicProviderConfig.class, "",
			"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer'"
				+ " secret='unencrypted:gateway-token'/>"));

		assertEquals("Bearer gateway-token", headers.get(AUTHORIZATION));
		assertNull(headers.get(X_API_KEY));
	}

	/** A provider without API key accepts credentials too. */
	public void testCredentialsOfLocalProvider() throws Exception {
		Map<String, String> headers = streamOnce(provider(LocalProviderConfig.class, "",
			"<credentials class='" + HeaderCredentials.class.getName() + "' header='X-Token'"
				+ " secret='unencrypted:local-token'/>"));

		assertEquals("local-token", headers.get("x-token"));
	}

	/** Credentials and an API key at the same time are refused. */
	public void testCredentialsAndApiKey() throws Exception {
		try {
			provider(OpenAIProviderConfig.class, "api-key='unencrypted:oai-key'",
				"<credentials class='" + HeaderCredentials.class.getName() + "' scheme='Bearer'"
					+ " secret='unencrypted:gateway-token'/>");
			fail("Credentials and API key must not be accepted together.");
		} catch (RuntimeException ex) {
			assertTrue("Not refused for credentials and API key: " + ex,
				isRefused(ex, I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__PROVIDER.fill("test")));
		}
	}

	/** Model discovery uses the credentials, together with the provider's additional headers. */
	public void testDiscoveryHeaders() throws Exception {
		_status = headers -> HttpURLConnection.HTTP_OK;

		List<String> models = provider(AnthropicProviderConfig.class, "api-key='unencrypted:ant-key'").discoverModels();

		assertEquals(List.of("m1"), models);
		Map<String, String> headers = _requests.get(0);
		assertEquals("ant-key", headers.get(X_API_KEY));
		assertEquals("2023-06-01", headers.get(AnthropicMessagesProvider.VERSION_HEADER));
	}

	/** A rejected request is repeated once with fresh credentials. */
	public void testStreamRetriesWithFreshCredentials() throws Exception {
		RotatingCredentials credentials = new RotatingCredentials();
		_status = headers -> ("Bearer token-1".equals(headers.get(AUTHORIZATION)))
			? HttpURLConnection.HTTP_BAD_REQUEST
			: HttpURLConnection.HTTP_UNAUTHORIZED;

		streamOnce(new OpenAIProviderStub(credentials));

		assertEquals(1, credentials._invalidations);
		assertEquals(2, _requests.size());
		assertEquals("Bearer token-0", _requests.get(0).get(AUTHORIZATION));
		assertEquals("Bearer token-1", _requests.get(1).get(AUTHORIZATION));
	}

	/** Credentials rejected again are not tried a third time. */
	public void testStreamRetriesOnlyOnce() throws Exception {
		RotatingCredentials credentials = new RotatingCredentials();
		_status = headers -> HttpURLConnection.HTTP_UNAUTHORIZED;

		try {
			new OpenAIProviderStub(credentials).stream(request(), event -> {
				// Not observed.
			});
			fail("A rejected request must fail.");
		} catch (ProviderException ex) {
			assertEquals(ProviderException.Kind.AUTHENTICATION, ex.kind());
		}

		assertEquals(2, _requests.size());
		assertEquals(2, credentials._invalidations);
	}

	/** Model discovery repeats a rejected request once with fresh credentials. */
	public void testDiscoveryRetriesWithFreshCredentials() {
		RotatingCredentials credentials = new RotatingCredentials();
		_status = headers -> ("Bearer token-1".equals(headers.get(AUTHORIZATION)))
			? HttpURLConnection.HTTP_OK
			: HttpURLConnection.HTTP_UNAUTHORIZED;

		List<String> models = ModelDiscoveryHelper.fetchModels(credentials, baseUrl());

		assertEquals(List.of("m1"), models);
		assertEquals(1, credentials._invalidations);
		assertEquals(2, _requests.size());
	}

	/**
	 * Whether a {@link ConfigurationError} with the given message is among the causes of the given
	 * exception thrown by the instantiation of a provider.
	 */
	private static boolean isRefused(Throwable ex, ResKey message) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConfigurationError error && message.equals(error.getErrorKey())) {
				return true;
			}
		}
		return false;
	}

	private LLMProviderConfig provider(Class<? extends LLMProviderConfig> type, String attributes)
			throws ConfigurationException {
		return provider(type, attributes, "");
	}

	private LLMProviderConfig provider(Class<? extends LLMProviderConfig> type, String attributes, String content)
			throws ConfigurationException {
		String xml = "<provider xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + type.getName() + "$Config'"
			+ " class='" + type.getName() + "' name='test' base-url='" + baseUrl() + "' models='m1' "
			+ attributes + ">" + content + "</provider>";
		LLMProviderConfig.Config config =
			(LLMProviderConfig.Config) TypedConfiguration.parse(CharacterContents.newContent(xml));
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * Sends a request through the given provider and returns the headers the server received.
	 */
	private Map<String, String> streamOnce(LLMProviderConfig provider) {
		return streamOnce(provider.createProvider());
	}

	private Map<String, String> streamOnce(LLMProvider provider) {
		try {
			provider.stream(request(), event -> {
				// Not observed.
			});
		} catch (ProviderException ex) {
			// The server answers with an error, only the request is of interest.
		}
		assertFalse("No request received.", _requests.isEmpty());
		return _requests.get(_requests.size() - 1);
	}

	private static ProviderRequest request() {
		return new ProviderRequest("m1", null, List.of(), List.of(), 100);
	}

	/**
	 * Credentials sending a bearer token that is replaced by the next one on each invalidation.
	 */
	private static final class RotatingCredentials implements CredentialProvider {

		int _invalidations;

		@Override
		public void authenticate(com.top_logic.basic.credential.RequestAuthentication request) {
			request.setHeader(CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER + " token-" + _invalidations);
		}

		@Override
		public void invalidate() {
			_invalidations++;
		}

	}

	/**
	 * Provider against the local server with given credentials, mapping error responses to
	 * {@link ProviderException}s by their status.
	 */
	private final class OpenAIProviderStub extends AbstractStreamingProvider {

		OpenAIProviderStub(CredentialProvider credentials) {
			super(credentials, baseUrl(), defaultHttpClient());
		}

		@Override
		protected String endpointPath(ProviderRequest request) {
			return "/v1/chat/completions";
		}

		@Override
		protected String buildRequestJson(ProviderRequest request) {
			return "{}";
		}

		@Override
		protected void processStream(InputStream input, StreamHandler handler) {
			// Not reached.
		}

		@Override
		protected void handleErrorResponse(HttpResponse<InputStream> response) throws ProviderException {
			drainResponseBody(response);
			throw new ProviderException(ErrorClassifier.classifyByStatus(response.statusCode()),
				response.statusCode(), "HTTP " + response.statusCode());
		}

	}

}
