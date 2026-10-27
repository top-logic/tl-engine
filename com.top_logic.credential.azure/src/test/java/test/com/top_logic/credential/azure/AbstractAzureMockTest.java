/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.azure;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.basic.credential.SecretStore;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * Base class for tests against a mock of Azure endpoints (managed identity endpoints, Key Vault).
 */
@SuppressWarnings("javadoc")
public abstract class AbstractAzureMockTest extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	static {
		// Read once by the Azure SDK, when it first creates an HTTP client.
		System.setProperty(com.azure.core.util.Configuration.PROPERTY_AZURE_HTTP_CLIENT_IMPLEMENTATION,
			ImdsRedirectHttpClientProvider.class.getName());
	}

	/**
	 * Path of the token endpoint of the instance metadata service.
	 */
	protected static final String IMDS_PATH = "/metadata/identity/oauth2/token";

	public interface Config extends ConfigurationItem {

		String CREDENTIALS = "credentials";

		String SECRET = "secret";

		String STORE = "store";

		@Name(CREDENTIALS)
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		@Name(SECRET)
		PolymorphicConfiguration<? extends SecretSource> getSecret();

		@Name(STORE)
		PolymorphicConfiguration<? extends SecretStore> getStore();

	}

	/**
	 * A request received by the mock server.
	 */
	static final class Received {

		final String _path;

		final Map<String, String> _query;

		final Map<String, String> _headers;

		Received(String path, Map<String, String> query, Map<String, String> headers) {
			_path = path;
			_query = query;
			_headers = headers;
		}

		String header(String name) {
			return _headers.get(name.toLowerCase());
		}

		String parameter(String name) {
			return _query.get(name);
		}

	}

	/**
	 * Response of a mock endpoint.
	 */
	interface Responder {
		/**
		 * @param count
		 *        Number of the request to this endpoint, starting with 1.
		 * @param request
		 *        The received request.
		 * @return HTTP status and body.
		 */
		Object[] respond(int count, Received request);
	}

	private HttpServer _server;

	private ExecutorService _executor;

	private String _baseUrl;

	private final Map<String, List<Received>> _requests = new ConcurrentHashMap<>();

	private final Map<String, Responder> _responders = new ConcurrentHashMap<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_executor = Executors.newCachedThreadPool();
		_server.setExecutor(_executor);
		_baseUrl = "http://localhost:" + _server.getAddress().getPort();
		_server.createContext("/", this::handle);
		_server.start();
		ImdsRedirectHttpClientProvider.setTarget(_baseUrl);
	}

	@Override
	protected void tearDown() throws Exception {
		_server.stop(0);
		_executor.shutdownNow();
		ImdsRedirectHttpClientProvider.setTarget(null);
		super.tearDown();
	}

	private void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		if (path.length() > 1 && path.endsWith("/")) {
			// The Azure SDK requests the current version of a secret with an empty version segment.
			path = path.substring(0, path.length() - 1);
		}
		Map<String, String> headers = new HashMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
		Received request = new Received(path, parseQuery(exchange.getRequestURI().getRawQuery()), headers);
		List<Received> received = _requests.computeIfAbsent(path, x -> new CopyOnWriteArrayList<>());
		received.add(request);
		Responder responder = _responders.get(path);
		Object[] response =
			responder == null ? new Object[] { 404, "{}" } : responder.respond(received.size(), request);
		byte[] bytes = ((String) response[1]).getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(((Integer) response[0]).intValue(), bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	private static Map<String, String> parseQuery(String query) {
		Map<String, String> result = new LinkedHashMap<>();
		if (query == null || query.isEmpty()) {
			return result;
		}
		for (String pair : query.split("&")) {
			int sep = pair.indexOf('=');
			result.put(URLDecoder.decode(pair.substring(0, sep), StandardCharsets.UTF_8),
				URLDecoder.decode(pair.substring(sep + 1), StandardCharsets.UTF_8));
		}
		return result;
	}

	/**
	 * Installs the response of the mock endpoint at the given path.
	 */
	protected void respond(String path, Responder responder) {
		_responders.put(path, responder);
	}

	/**
	 * The requests received at the given path.
	 */
	protected List<Received> requests(String path) {
		List<Received> result = _requests.get(path);
		return result == null ? Collections.emptyList() : result;
	}

	/**
	 * Redirects the requests to the instance metadata service to the given base URL.
	 */
	protected static void redirectImds(String baseUrl) {
		ImdsRedirectHttpClientProvider.setTarget(baseUrl);
	}

	/**
	 * A resource identifier unique to the running test.
	 *
	 * <p>
	 * The Azure Identity library caches the tokens of the managed identity for the whole JVM by
	 * resource, so each test requests tokens for its own resource.
	 * </p>
	 */
	protected String uniqueResource(String resource) {
		return resource + "/" + getName();
	}

	/**
	 * URL of the given path at the mock server.
	 */
	protected String url(String path) {
		return _baseUrl + path;
	}

	/**
	 * Reads a credential provider configuration.
	 */
	protected CredentialProvider credentials(String xml) throws Exception {
		Config config = read("<config>" + xml + "</config>");
		return getInstance(config.getCredentials());
	}

	/**
	 * Reads a secret source configuration.
	 */
	protected SecretSource secret(String xml) throws Exception {
		Config config = read("<config>" + xml + "</config>");
		return getInstance(config.getSecret());
	}

	/**
	 * Reads a secret store configuration.
	 */
	protected SecretStore store(String xml) throws Exception {
		Config config = read("<config>" + xml + "</config>");
		return getInstance(config.getStore());
	}

	/**
	 * The {@link CredentialProvider#AUTHORIZATION} header set by the given provider.
	 */
	protected static String authenticate(CredentialProvider provider) {
		Map<String, String> headers = new ConcurrentHashMap<>();
		provider.authenticate(headers::put);
		assertEquals(Collections.singleton(CredentialProvider.AUTHORIZATION), headers.keySet());
		return headers.get(CredentialProvider.AUTHORIZATION);
	}

	/**
	 * Asserts that no message in the cause chain of the given exception contains the given text.
	 */
	protected static void assertNotRevealed(Throwable ex, String text) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			assertTrue("Revealed in message: " + message, message == null || !message.contains(text));
			if (cause instanceof I18NRuntimeException) {
				String key = String.valueOf(((I18NRuntimeException) cause).getErrorKey());
				assertTrue("Revealed in message key: " + key, !key.contains(text));
			}
		}
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

}
