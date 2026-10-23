/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tl.ai.core.tools.web.RecordingServer.Reply;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Tests how {@link BraveSearchEngine} and {@link TavilySearchEngine} authenticate their requests:
 * with the <code>api-key</code> shorthand in the header of the engine's API, with configured
 * <code>credentials</code>, and once more with fresh credentials after a rejection.
 */
public class TestSearchEngineCredentials extends TestCase {

	/** The system property holding the API key of {@link #testEnvironmentApiKey()}. */
	private static final String VARIABLE = "test_search_engine_api_key";

	/** The API key in {@link #VARIABLE}. */
	private static final String API_KEY = "brave-0123456789";

	private static final String BRAVE_OK = "{\"web\":{\"results\":[]}}";

	private static final String TAVILY_OK = "{\"results\":[]}";

	/**
	 * Parsing engine configurations resolves the nested configuration types of the secret sources
	 * and credentials through the <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.withThreadContext(new TestSuite(TestSearchEngineCredentials.class)));
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** The <code>api-key</code> attribute of Brave is sent as bare subscription token. */
	public void testBraveApiKey() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(BRAVE_OK))) {
			engine(BraveSearchEngine.class, api, "api-key='unencrypted:brave-key'", "").search(query());

			assertEquals("brave-key", header(api, 0, BraveSearchEngine.API_KEY_HEADER));
			assertNull(header(api, 0, CredentialProvider.AUTHORIZATION));
		}
	}

	/** The <code>api-key</code> attribute of Tavily is sent as bearer token. */
	public void testTavilyApiKey() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(TAVILY_OK))) {
			engine(TavilySearchEngine.class, api, "api-key='unencrypted:tavily-key'", "").search(query());

			assertEquals("Bearer tavily-key", header(api, 0, CredentialProvider.AUTHORIZATION));
		}
	}

	/** An API key referenced by its variable is read from the variable. */
	public void testEnvironmentApiKey() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		try (RecordingServer api = RecordingServer.start(target -> json(BRAVE_OK))) {
			engine(BraveSearchEngine.class, api, "",
				"<api-key name='" + VARIABLE + "'/>")
				.search(query());

			assertEquals(API_KEY, header(api, 0, BraveSearchEngine.API_KEY_HEADER));
		}
	}

	/** Configured credentials define the header themselves. */
	public void testCredentials() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(BRAVE_OK))) {
			engine(BraveSearchEngine.class, api, "",
				"<credentials class='" + HeaderCredentials.class.getName() + "' header='X-Gateway'"
					+ " secret='unencrypted:gateway-token'/>").search(query());

			assertEquals("gateway-token", header(api, 0, "X-Gateway"));
			assertNull(header(api, 0, BraveSearchEngine.API_KEY_HEADER));
		}
	}

	/** An engine without API key and credentials sends no credentials. */
	public void testNoCredentials() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(TAVILY_OK))) {
			engine(TavilySearchEngine.class, api, "", "").search(query());

			assertNull(header(api, 0, CredentialProvider.AUTHORIZATION));
		}
	}

	/** Credentials and an API key at the same time are refused. */
	public void testCredentialsAndApiKey() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> json(BRAVE_OK))) {
			try {
				engine(BraveSearchEngine.class, api, "api-key='unencrypted:brave-key'",
					"<credentials class='" + HeaderCredentials.class.getName() + "' header='X-Gateway'"
						+ " secret='unencrypted:gateway-token'/>");
				fail("Credentials and API key must not be accepted together.");
			} catch (RuntimeException ex) {
				assertTrue("Not refused for credentials and API key: " + ex,
					isRefused(ex, I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__OWNER.fill("Brave")));
			}
		}
	}

	/** A rejected request is repeated once with fresh credentials. */
	public void testRetryWithFreshCredentials() throws Exception {
		RotatingCredentials credentials = new RotatingCredentials();
		AtomicInteger requests = new AtomicInteger();
		try (RecordingServer api = RecordingServer.start(
			target -> requests.getAndIncrement() == 0 ? unauthorized() : json(BRAVE_OK))) {
			HttpResponse<String> response = http(credentials).send(request(api));

			assertEquals(200, response.statusCode());
			assertEquals(1, credentials._invalidations);
			assertEquals(2, api.contactCount());
			assertEquals("Bearer token-0", header(api, 0, CredentialProvider.AUTHORIZATION));
			assertEquals("Bearer token-1", header(api, 1, CredentialProvider.AUTHORIZATION));
		}
	}

	/** Credentials rejected again are not tried a third time; the rejection is the answer. */
	public void testRetryOnlyOnce() throws Exception {
		RotatingCredentials credentials = new RotatingCredentials();
		try (RecordingServer api = RecordingServer.start(target -> unauthorized())) {
			HttpResponse<String> response = http(credentials).send(request(api));

			assertEquals(401, response.statusCode());
			assertEquals(2, api.contactCount());
			assertEquals(2, credentials._invalidations);
		}
	}

	/** A rejected search fails with the status of the rejection. */
	public void testRejectedSearchFails() throws Exception {
		try (RecordingServer api = RecordingServer.start(target -> unauthorized())) {
			try {
				engine(BraveSearchEngine.class, api, "api-key='unencrypted:wrong-key'", "").search(query());
				fail("A rejected search must fail.");
			} catch (IOException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains("HTTP 401"));
			}
			assertEquals(2, api.contactCount());
		}
	}

	/**
	 * The service editor stores an API key referenced by its variable in the form it was read,
	 * holding only the name of the variable.
	 */
	public void testEnvironmentApiKeyStorage() throws IOException, ConfigurationException {
		System.setProperty(VARIABLE, API_KEY);
		String stored = """
			<?xml version="1.0" encoding="utf-8" ?>

			<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
				xmlns:config="http://www.top-logic.com/ns/config/6.0"
			>
				<services>
					<config service-class="com.top_logic.tl.ai.core.tools.web.WebToolsService"
						config:override="true"
					>
						<instance class="com.top_logic.tl.ai.core.tools.web.WebToolsService">
							<search-engine class="com.top_logic.tl.ai.core.tools.web.BraveSearchEngine">
								<api-key class="com.top_logic.basic.credential.EnvironmentSecretSource"
									name="%s"
								/>
							</search-engine>
						</instance>
					</config>
				</services>
			</application>""".formatted(VARIABLE);
		File file = BasicTestCase.createNamedTestFile(getName() + InAppServiceConfigStore.FILE_SUFFIX);
		try {
			FileUtilities.writeStringToFile(stored, file, StringServices.UTF8);
			ApplicationConfig.Config config = InAppServiceConfigStore.read(file);
			String written = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG, config,
				InAppServiceConfigStore.OVERRIDE_SERVICE);

			assertTrue(written, written.contains("name=\"" + VARIABLE + "\""));
			assertFalse(written, written.contains(API_KEY));

			FileUtilities.writeStringToFile(written, file, StringServices.UTF8);
			String rewritten = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG,
				InAppServiceConfigStore.read(file), InAppServiceConfigStore.OVERRIDE_SERVICE);
			assertEquals(written, rewritten);

			WebToolsService.Config<?> service = (WebToolsService.Config<?>) InAppServiceConfigStore.read(file)
				.getServices().get(WebToolsService.class).getInstance();
			EnvironmentSecretSource.Config apiKey = (EnvironmentSecretSource.Config)
				((BraveSearchEngine.Config) service.getSearchEngine()).getApiKey();
			assertEquals(VARIABLE, apiKey.getName());
		} finally {
			file.delete();
		}
	}

	private static WebSearchQuery query() {
		return WebSearchQuery.of("q", 5);
	}

	private static WebSearchEngine engine(Class<? extends WebSearchEngine> type, RecordingServer api,
			String attributes, String content) throws ConfigurationException {
		String baseUrl = type == BraveSearchEngine.class ? api.origin() + "/res/v1/web/search" : api.origin();
		String xml = "<search-engine xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + type.getName() + "$Config'"
			+ " class='" + type.getName() + "' base-url='" + baseUrl + "' "
			+ attributes + ">" + content + "</search-engine>";
		Object config = TypedConfiguration.parse(CharacterContents.newContent(xml));
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<? extends WebSearchEngine> engineConfig =
			(PolymorphicConfiguration<? extends WebSearchEngine>) config;
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(engineConfig);
	}

	private static SearchHttp http(CredentialProvider credentials) {
		return new SearchHttp("Test", HttpClient.newHttpClient(), credentials, duration -> {
			// No waits in this test.
		}, Clock.systemUTC());
	}

	private static HttpRequest.Builder request(RecordingServer api) {
		return HttpRequest.newBuilder(URI.create(api.origin() + "/search")).GET();
	}

	/**
	 * The value of the header with the given name in the request with the given index, ignoring the
	 * case of the name.
	 */
	private static String header(RecordingServer api, int index, String name) {
		List<String> heads = api.heads();
		assertTrue("Only " + heads.size() + " requests received.", heads.size() > index);
		String prefix = name.toLowerCase(Locale.ROOT) + ":";
		for (String line : heads.get(index).split("\n")) {
			if (line.toLowerCase(Locale.ROOT).startsWith(prefix)) {
				return line.substring(prefix.length()).strip();
			}
		}
		return null;
	}

	/**
	 * Whether a {@link ConfigurationError} with the given message is among the causes of the given
	 * exception.
	 */
	private static boolean isRefused(Throwable ex, ResKey message) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConfigurationError error && message.equals(error.getErrorKey())) {
				return true;
			}
		}
		return false;
	}

	private static Reply json(String body) {
		return new Reply(200, "OK", List.of("Content-Type: application/json"), body);
	}

	private static Reply unauthorized() {
		return new Reply(401, "Unauthorized", List.of("Content-Type: application/json"), "{\"error\":\"denied\"}");
	}

	/**
	 * Credentials sending a bearer token that is replaced by the next one on each invalidation.
	 */
	private static final class RotatingCredentials implements CredentialProvider {

		int _invalidations;

		@Override
		public void authenticate(RequestAuthentication request) {
			request.setHeader(CredentialProvider.AUTHORIZATION, CredentialProvider.BEARER + " token-" + _invalidations);
		}

		@Override
		public void invalidate() {
			_invalidations++;
		}

	}

}
