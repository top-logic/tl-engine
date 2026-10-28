/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.embed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntUnaryOperator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
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
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.autoconf.InAppServiceConfigStore;

/**
 * Tests how {@link OpenAiCompatibleEmbedder} authenticates its requests: with the
 * <code>api-key</code> shorthand as bearer token, in the attribute form and referenced by its
 * variable, with configured <code>credentials</code>, and once more with fresh credentials after a
 * rejection.
 */
public class TestOpenAiCompatibleEmbedderCredentials extends TestCase {

	/** The system property holding the API key of {@link #testEnvironmentApiKey()}. */
	private static final String VARIABLE = "test_embed_api_key";

	/** The API key in {@link #VARIABLE}. */
	private static final String API_KEY = "embed-0123456789";

	private HttpServer _server;

	private final List<String> _authorizations = new CopyOnWriteArrayList<>();

	private final List<String> _gatewayHeaders = new CopyOnWriteArrayList<>();

	/** The status of the answer to the request with the given number, counted from 1. */
	private IntUnaryOperator _status = n -> 200;

	/**
	 * Parsing embedder configurations resolves the nested configuration types of the secret sources
	 * and credentials through the <code>TypeIndex</code>, which needs the module system running.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(
			ServiceTestSetup.withThreadContext(new TestSuite(TestOpenAiCompatibleEmbedderCredentials.class)), SecretStoreService.Module.INSTANCE));
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
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** The <code>api-key</code> attribute is sent as bearer token. */
	public void testAttributeApiKey() throws Exception {
		embedder("api-key='unencrypted:old-key'", "").embedQuery("q");

		assertEquals(List.of("Bearer old-key"), _authorizations);
	}

	/** An API key referenced by its variable is read from the variable for each request. */
	public void testEnvironmentApiKey() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		Embedder embedder = embedder("",
			"<api-key class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>");
		embedder.embedQuery("q");
		System.setProperty(VARIABLE, "rotated");
		embedder.embedQuery("q");

		assertEquals(List.of("Bearer " + API_KEY, "Bearer rotated"), _authorizations);
	}

	/** An API key referenced by an unset variable fails the request, not the start. */
	public void testUnsetEnvironmentApiKey() throws Exception {
		Embedder embedder = embedder("",
			"<api-key class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>");
		try {
			embedder.embedQuery("q");
			fail("A request without its key must fail.");
		} catch (EmbeddingException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().startsWith("No credentials"));
		}
		assertEquals(List.of(), _authorizations);
	}

	/** Configured credentials define the header themselves. */
	public void testCredentials() throws Exception {
		embedder("", "<credentials class='" + HeaderCredentials.class.getName() + "' header='X-Gateway'"
			+ " secret='unencrypted:gateway-token'/>").embedQuery("q");

		assertEquals(List.of("gateway-token"), _gatewayHeaders);
		assertEquals(List.of("null"), _authorizations);
	}

	/** An embedder without API key and credentials sends no credentials. */
	public void testNoCredentials() throws Exception {
		embedder("", "").embedQuery("q");

		assertEquals(List.of("null"), _authorizations);
	}

	/** Credentials and an API key at the same time are refused. */
	public void testCredentialsAndApiKey() throws Exception {
		try {
			embedder("api-key='unencrypted:key'", "<credentials class='" + HeaderCredentials.class.getName()
				+ "' header='X-Gateway' secret='unencrypted:gateway-token'/>");
			fail("Credentials and API key must not be accepted together.");
		} catch (RuntimeException ex) {
			assertTrue("Not refused for credentials and API key: " + ex, isRefused(ex));
		}
	}

	/** A rejected request is repeated once with fresh credentials, not counted as retry. */
	public void testRetryWithFreshCredentials() throws Exception {
		_status = n -> n == 1 ? 401 : 200;
		embedder("api-key='unencrypted:key' retries='0'", "").embedQuery("q");

		assertEquals(2, _authorizations.size());
	}

	/** Credentials rejected again are not tried a third time. */
	public void testRetryOnlyOnce() throws Exception {
		_status = n -> 401;
		try {
			embedder("api-key='unencrypted:wrong-key' retries='3'", "").embedQuery("q");
			fail("A rejected request must fail.");
		} catch (EmbeddingException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("HTTP 401"));
		}
		assertEquals(2, _authorizations.size());
	}

	/**
	 * The service editor stores an API key referenced by its secret name in the form it was read,
	 * holding only the name of the secret.
	 */
	public void testEnvironmentApiKeyStorage() throws IOException, ConfigurationException {
		System.setProperty(VARIABLE, API_KEY);
		String stored = """
			<?xml version="1.0" encoding="utf-8" ?>

			<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
				xmlns:config="http://www.top-logic.com/ns/config/6.0"
			>
				<services>
					<config service-class="com.top_logic.tl.ai.index.AiIndexService"
						config:override="true"
					>
						<instance class="com.top_logic.tl.ai.index.AiIndexService">
							<embedder class="com.top_logic.tl.ai.index.embed.OpenAiCompatibleEmbedder"
								base-url="https://embed.example.eu"
								dimensions="4"
								model="m"
							>
								<api-key name="%s"/>
							</embedder>
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
			// An element without class is a NamedSecretSource, the default secret source.
			assertTrue(written, written.contains("<api-key name=\"" + VARIABLE + "\"/>"));
			assertFalse(written, written.contains(API_KEY));

			FileUtilities.writeStringToFile(written, file, StringServices.UTF8);
			String rewritten = InAppServiceConfigStore.toXml(InAppServiceConfigStore.APPLICATION_TAG,
				InAppServiceConfigStore.read(file), InAppServiceConfigStore.OVERRIDE_SERVICE);
			assertEquals(written, rewritten);
		} finally {
			file.delete();
		}
	}

	private Embedder embedder(String attributes, String content) throws ConfigurationException {
		String type = OpenAiCompatibleEmbedder.class.getName();
		String xml = "<embedder xmlns:config='http://www.top-logic.com/ns/config/6.0'"
			+ " config:interface='" + type + "$Config'"
			+ " class='" + type + "' base-url='http://127.0.0.1:" + _server.getAddress().getPort() + "'"
			+ " model='m' dimensions='2' retry-delay-millis='1' "
			+ attributes + ">" + content + "</embedder>";
		Object config = TypedConfiguration.parse(CharacterContents.newContent(xml));
		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<? extends Embedder> embedderConfig =
			(PolymorphicConfiguration<? extends Embedder>) config;
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(embedderConfig);
	}

	/**
	 * Whether a {@link ConfigurationError} for credentials and API key is among the causes of the
	 * given exception.
	 */
	private boolean isRefused(Throwable ex) {
		ResKey message = I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__OWNER
			.fill("http://127.0.0.1:" + _server.getAddress().getPort() + "/v1/embeddings");
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof ConfigurationError error && message.equals(error.getErrorKey())) {
				return true;
			}
		}
		return false;
	}

	private void handle(HttpExchange exchange) throws IOException {
		try (InputStream in = exchange.getRequestBody()) {
			in.readAllBytes();
		}
		_authorizations.add(String.valueOf(exchange.getRequestHeaders().getFirst(CredentialProvider.AUTHORIZATION)));
		String gateway = exchange.getRequestHeaders().getFirst("X-Gateway");
		if (gateway != null) {
			_gatewayHeaders.add(gateway);
		}
		int status = _status.applyAsInt(_authorizations.size());
		String body = status == 200
			? "{\"object\":\"list\",\"data\":[{\"object\":\"embedding\",\"index\":0,\"embedding\":[1.0,2.0]}]}"
			: "{\"error\":\"status " + status + "\"}";
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

}
