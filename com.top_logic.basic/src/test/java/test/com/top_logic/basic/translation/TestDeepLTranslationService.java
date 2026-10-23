/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.translation;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntFunction;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.I18NConstants;
import com.top_logic.basic.translation.DeepLTranslationService;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.tools.resources.translate.Translator;
import com.top_logic.tools.resources.translate.deepl.DeepLTranslator;

/**
 * Tests how {@link DeepLTranslationService} authenticates its requests against a mock DeepL API:
 * with the <code>api-key</code> shorthand, with configured <code>credentials</code>, and once more
 * with fresh credentials after a rejection.
 */
@SuppressWarnings("javadoc")
public class TestDeepLTranslationService extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** The system property holding the API key of the tests referencing a variable. */
	private static final String VARIABLE = "test_deepl_translation_api_key";

	/** The API key in {@link #VARIABLE}. */
	private static final String API_KEY = "deepl-0123456789";

	private static final String TRANSLATE_PATH = "/translate";

	private static final String TRANSLATION = "Hallo";

	private static final String TRANSLATION_OK =
		"{\"translations\":[{\"detected_source_language\":\"EN\",\"text\":\"" + TRANSLATION + "\"}]}";

	public interface Config extends ConfigurationItem {

		String SERVICE = "service";

		@Name(SERVICE)
		PolymorphicConfiguration<? extends TranslationService<?>> getService();

	}

	/**
	 * {@link DeepLTranslationService} giving access to the {@link Translator} it creates.
	 */
	public static class ExposedService extends DeepLTranslationService {

		public ExposedService(InstantiationContext context, Config config) {
			super(context, config);
		}

		Translator translator() {
			return createTranslator();
		}

	}

	/**
	 * Mock of the DeepL API recording the <code>Authorization</code> header of each request.
	 */
	private static final class MockApi implements AutoCloseable {

		private final HttpServer _server;

		final List<String> _authorizations = new CopyOnWriteArrayList<>();

		/**
		 * @param status
		 *        Status of the answer to the request with the given index, starting with 0.
		 */
		MockApi(IntFunction<Integer> status) throws IOException {
			_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
			_server.createContext(TRANSLATE_PATH, exchange -> handle(exchange, status));
			_server.start();
		}

		private void handle(HttpExchange exchange, IntFunction<Integer> status) throws IOException {
			exchange.getRequestBody().readAllBytes();
			int index = _authorizations.size();
			String authorization = exchange.getRequestHeaders().getFirst(DeepLTranslator.AUTHORIZATION);
			_authorizations.add(authorization == null ? "" : authorization);
			int code = status.apply(index);
			byte[] body = (code == 200 ? TRANSLATION_OK : "{\"message\":\"denied\"}").getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(code, body.length);
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(body);
			}
		}

		String origin() {
			return "http://" + _server.getAddress().getHostString() + ":" + _server.getAddress().getPort();
		}

		@Override
		public void close() {
			_server.stop(0);
		}

	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	/** The <code>api-key</code> attribute is sent with the DeepL scheme. */
	public void testApiKeyAttribute() throws Exception {
		try (MockApi api = new MockApi(index -> 200)) {
			assertEquals(TRANSLATION, translate(api, "api-key='unencrypted:attribute-key'", ""));

			assertEquals(List.of(DeepLTranslator.AUTH_SCHEME + " attribute-key"), api._authorizations);
		}
	}

	/** An API key referenced by its variable is read from the variable holding the plain key. */
	public void testEnvironmentApiKey() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		try (MockApi api = new MockApi(index -> 200)) {
			assertEquals(TRANSLATION, translate(api, "", environmentApiKey()));

			assertEquals(List.of(DeepLTranslator.AUTH_SCHEME + " " + API_KEY), api._authorizations);
		}
	}

	/** Configured credentials define the header themselves. */
	public void testCredentials() throws Exception {
		try (MockApi api = new MockApi(index -> 200)) {
			assertEquals(TRANSLATION, translate(api, "",
				"<credentials class='" + HeaderCredentials.class.getName() + "'"
					+ " header='" + DeepLTranslator.AUTHORIZATION + "' scheme='Bearer'"
					+ " secret='unencrypted:gateway-token'/>"));

			assertEquals(List.of("Bearer gateway-token"), api._authorizations);
		}
	}

	/** A service without API key and credentials sends no credentials. */
	public void testNoCredentials() throws Exception {
		try (MockApi api = new MockApi(index -> 200)) {
			assertEquals(TRANSLATION, translate(api, "", ""));

			assertEquals(List.of(""), api._authorizations);
		}
	}

	/** Credentials and an API key at the same time are refused. */
	public void testCredentialsAndApiKey() throws Exception {
		try (MockApi api = new MockApi(index -> 200)) {
			try {
				service(api, "api-key='unencrypted:attribute-key'",
					"<credentials class='" + HeaderCredentials.class.getName() + "' header='X-Gateway'"
						+ " secret='unencrypted:gateway-token'/>");
				fail("Credentials and API key must not be accepted together.");
			} catch (RuntimeException ex) {
				assertTrue("Not refused for credentials and API key: " + ex,
					isRefused(ex, I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__OWNER.fill("DeepL")));
			}
		}
	}

	/**
	 * A rejected request is repeated once, with the key read again from its variable at the moment
	 * the request is repeated.
	 */
	public void testRetryWithFreshCredentials() throws Exception {
		System.setProperty(VARIABLE, "revoked-key");
		try (MockApi api = new MockApi(index -> {
			if (index == 0) {
				System.setProperty(VARIABLE, API_KEY);
				return 401;
			}
			return 200;
		})) {
			assertEquals(TRANSLATION, translate(api, "", environmentApiKey()));

			assertEquals(List.of(DeepLTranslator.AUTH_SCHEME + " revoked-key", DeepLTranslator.AUTH_SCHEME + " " + API_KEY),
				api._authorizations);
		}
	}

	/** Credentials rejected again are not tried a third time; the translation fails. */
	public void testRetryOnlyOnce() throws Exception {
		try (MockApi api = new MockApi(index -> 401)) {
			try {
				translate(api, "api-key='unencrypted:wrong-key'", "");
				fail("A rejected translation must fail.");
			} catch (RuntimeException ex) {
				assertTrue(ex.getMessage(), ex.getMessage().contains("HTTP/401"));
			}
			assertEquals(2, api._authorizations.size());
		}
	}

	/**
	 * A configuration referencing the API key by its variable is written in the form it was read,
	 * holding only the name of the variable.
	 */
	public void testEnvironmentApiKeyStorage() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		try (MockApi api = new MockApi(index -> 200)) {
			Config config = read(serviceXml(api, "", environmentApiKey()));

			String written = toXML(config);
			assertTrue(written, written.contains("name=\"" + VARIABLE + "\""));
			assertTrue(written, written.contains(EnvironmentSecretSource.class.getName()));
			assertFalse(written, written.contains(API_KEY));

			Config reread = read(written);
			assertEquals(written, toXML(reread));
			assertEquals(TRANSLATION, ((ExposedService) getInstance(reread.getService())).translator()
				.translate("Hello", "en", "de"));
		}
	}

	private static String environmentApiKey() {
		return "<api-key class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>";
	}

	private String translate(MockApi api, String attributes, String content) throws ConfigurationException {
		return service(api, attributes, content).translator().translate("Hello", "en", "de");
	}

	private ExposedService service(MockApi api, String attributes, String content) throws ConfigurationException {
		Config config = read(serviceXml(api, attributes, content));
		return (ExposedService) getInstance(config.getService());
	}

	private static String serviceXml(MockApi api, String attributes, String content) {
		return "<" + ROOT_TAG + "><" + Config.SERVICE + " class='" + ExposedService.class.getName() + "'"
			+ " api-host='" + api.origin() + "' " + attributes + ">" + content + "</" + Config.SERVICE + "></"
			+ ROOT_TAG + ">";
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

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestDeepLTranslationService.class);
	}

}
