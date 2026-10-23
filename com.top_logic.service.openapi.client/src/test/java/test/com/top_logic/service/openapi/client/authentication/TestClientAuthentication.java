/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.client.authentication;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.LiteralSecretSource;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.authentication.apikey.ClientAPIKeySecret;
import com.top_logic.service.openapi.client.authentication.config.ClientAuthentication;
import com.top_logic.service.openapi.client.authentication.http.basic.ClientBasicAuthSecret;
import com.top_logic.service.openapi.client.authentication.oauth.ClientCredentialSecret;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.client.registry.impl.call.uri.UriBuilder;
import com.top_logic.service.openapi.common.authentication.oauth.DefaultURIProvider;
import com.top_logic.service.openapi.common.authentication.oauth.OpenIDURIProvider;

/**
 * Tests the authentication of requests of the {@link ServiceMethodRegistry} against a mock server
 * that also acts as OAuth authorization server.
 */
@SuppressWarnings("javadoc")
public class TestClientAuthentication extends TestCase {

	private static final String DOMAIN = "test-domain";

	private static final String API_PATH = "/api";

	private static final String TOKEN_PATH = "/token";

	private static final String ISSUER_PATH = "/realms/test";

	private static final String CLIENT_ID = "my-app";

	private static final String CLIENT_SECRET = "client-s3cr3t";

	private static final String USER = "technical-user";

	private static final String PASSWORD = "user-s3cr3t";

	private static final String API_KEY = "api-k3y";

	private static final String HEADER = "X-API-Key";

	private static final String VARIABLE = "TL_TEST_OPENAPI_CLIENT_SECRET";

	private static final String AUTHORIZATION = "authorization";

	private static final int UNAUTHORIZED = 401;

	/**
	 * A request received by the mock server.
	 */
	static final class Received {

		final Map<String, String> _headers;

		final String _query;

		final Map<String, String> _parameters;

		Received(Map<String, String> headers, String query, Map<String, String> parameters) {
			_headers = headers;
			_query = query;
			_parameters = parameters;
		}

		String header(String name) {
			return _headers.get(name.toLowerCase());
		}

	}

	private HttpServer _server;

	private ExecutorService _executor;

	private String _baseUrl;

	private final List<Received> _tokenRequests = new CopyOnWriteArrayList<>();

	private final List<Received> _apiRequests = new CopyOnWriteArrayList<>();

	private volatile int _discoveryRequests;

	private volatile Integer _expiresIn;

	private volatile int _apiStatus;

	private CloseableHttpClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_expiresIn = Integer.valueOf(3600);
		_apiStatus = 200;
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_executor = Executors.newCachedThreadPool();
		_server.setExecutor(_executor);
		_baseUrl = "http://localhost:" + _server.getAddress().getPort();
		_server.createContext(TOKEN_PATH, this::handleToken);
		_server.createContext(API_PATH, this::handleApi);
		_server.createContext(ISSUER_PATH + "/.well-known/openid-configuration", this::handleDiscovery);
		_server.start();
		_client = HttpClients.createDefault();
	}

	@Override
	protected void tearDown() throws Exception {
		_client.close();
		_server.stop(0);
		_executor.shutdownNow();
		System.clearProperty(VARIABLE);
		super.tearDown();
	}

	public void testAPIKeyHeader() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		SecurityEnhancer enhancer = enhancer(apiKeyConfig("header", "", environmentKey()));

		assertEquals(200, call(enhancer));
		assertEquals(API_KEY, lastApiRequest().header(HEADER));

		System.setProperty(VARIABLE, "rotated");
		assertEquals(200, call(enhancer));
		assertEquals("A rotated key must be used without restart.", "rotated", lastApiRequest().header(HEADER));
	}

	public void testAPIKeyQuery() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		SecurityEnhancer enhancer = enhancer(apiKeyConfig("query", "", environmentKey()));

		assertEquals(200, call(enhancer));
		assertEquals(HEADER + "=" + API_KEY, lastApiRequest()._query);
	}

	public void testAPIKeyCookie() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		SecurityEnhancer enhancer = enhancer(apiKeyConfig("cookie", "", environmentKey()));

		assertEquals(200, call(enhancer));
		assertEquals(HEADER + "=" + API_KEY, lastApiRequest().header("Cookie"));
	}

	public void testAPIKeyAttributeForm() throws Exception {
		SecurityEnhancer enhancer =
			enhancer(apiKeyConfig("header", "api-key='" + ConfigurationEncryption.encrypt(API_KEY) + "'", ""));

		assertEquals(200, call(enhancer));
		assertEquals(API_KEY, lastApiRequest().header(HEADER));
	}

	public void testWriteAPIKeyReference() throws Exception {
		System.setProperty(VARIABLE, API_KEY);
		ServiceMethodRegistry.Config<?> config = parse(apiKeyConfig("header", "", environmentKey()));

		String xml = toXML(config);
		assertFalse("Secret written: " + xml, xml.contains(API_KEY));
		assertTrue("Reference not written: " + xml, xml.contains(VARIABLE));

		ClientAPIKeySecret copy = (ClientAPIKeySecret) parse(xml).getSecrets().get(DOMAIN);
		assertEquals(VARIABLE, ((EnvironmentSecretSource.Config) copy.getAPIKey()).getName());
	}

	public void testWriteAPIKeyLiteralEncrypted() throws Exception {
		ServiceMethodRegistry.Config<?> config =
			parse(apiKeyConfig("header", "api-key='unencrypted:" + API_KEY + "'", ""));

		String xml = toXML(config);
		assertFalse("Secret written in plain text: " + xml, xml.contains(API_KEY));
		assertTrue("Not written as encrypted attribute: " + xml,
			xml.contains("api-key=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));
	}

	public void testBasicAuthAttributeForm() throws Exception {
		SecurityEnhancer enhancer = enhancer(basicConfig("password='unencrypted:" + PASSWORD + "'", ""));

		assertEquals(200, call(enhancer));
		assertEquals(basic(USER, PASSWORD), lastApiRequest().header(AUTHORIZATION));
	}

	public void testBasicAuthEncryptedAttributeForm() throws Exception {
		SecurityEnhancer enhancer =
			enhancer(basicConfig("password='" + ConfigurationEncryption.encrypt(PASSWORD) + "'", ""));

		assertEquals(200, call(enhancer));
		assertEquals(basic(USER, PASSWORD), lastApiRequest().header(AUTHORIZATION));
	}

	public void testBasicAuthEnvironmentReadAtUse() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		SecurityEnhancer enhancer = enhancer(basicConfig("", environmentPassword()));

		assertEquals(200, call(enhancer));
		assertEquals(basic(USER, PASSWORD), lastApiRequest().header(AUTHORIZATION));

		System.setProperty(VARIABLE, "rotated");
		assertEquals(200, call(enhancer));
		assertEquals("A rotated password must be used without restart.", basic(USER, "rotated"),
			lastApiRequest().header(AUTHORIZATION));
	}

	public void testOAuthTokenReused() throws Exception {
		SecurityEnhancer enhancer = enhancer(oauthConfig(tokenUrlProvider(), clientSecretAttribute()));

		assertEquals(200, call(enhancer));
		assertEquals(200, call(enhancer));

		assertEquals("The token must be fetched once and reused.", 1, _tokenRequests.size());
		Received tokenRequest = _tokenRequests.get(0);
		assertEquals("client_credentials", tokenRequest._parameters.get("grant_type"));
		assertEquals(CLIENT_ID, tokenRequest._parameters.get("client_id"));
		assertEquals(CLIENT_SECRET, tokenRequest._parameters.get("client_secret"));
		assertEquals("Bearer token-1", lastApiRequest().header(AUTHORIZATION));
	}

	public void testOAuthRenewedAfterMargin() throws Exception {
		_expiresIn = Integer.valueOf(2);
		SecurityEnhancer enhancer = enhancer(oauthConfig(tokenUrlProvider(), clientSecretAttribute()));

		assertEquals(200, call(enhancer));
		assertEquals(200, call(enhancer));
		assertEquals(1, _tokenRequests.size());

		// A token living 2s is renewed after half of its lifetime.
		Thread.sleep(1200);
		assertEquals(200, call(enhancer));
		assertEquals(2, _tokenRequests.size());
		assertEquals("Bearer token-2", lastApiRequest().header(AUTHORIZATION));
	}

	public void testOAuthUnauthorizedFetchesNewToken() throws Exception {
		SecurityEnhancer enhancer = enhancer(oauthConfig(tokenUrlProvider(), clientSecretAttribute()));

		assertEquals(200, call(enhancer));
		_apiStatus = UNAUTHORIZED;
		assertEquals(UNAUTHORIZED, call(enhancer));
		assertEquals(1, _tokenRequests.size());

		_apiStatus = 200;
		assertEquals(200, call(enhancer));
		assertEquals("A rejected token must be replaced.", 2, _tokenRequests.size());
		assertEquals("Bearer token-2", lastApiRequest().header(AUTHORIZATION));
	}

	public void testOAuthEnvironmentSecret() throws Exception {
		System.setProperty(VARIABLE, CLIENT_SECRET);
		SecurityEnhancer enhancer = enhancer(oauthConfig(tokenUrlProvider(),
			"<client-secret client-id='" + CLIENT_ID + "'>" + environmentPassword() + "</client-secret>"));

		assertEquals(200, call(enhancer));
		assertEquals(CLIENT_SECRET, _tokenRequests.get(0)._parameters.get("client_secret"));
	}

	public void testOAuthIssuerDiscovery() throws Exception {
		String provider = "<uri-provider class='" + OpenIDURIProvider.class.getName() + "' open-id-issuer='"
			+ _baseUrl + ISSUER_PATH + "'/>";
		SecurityEnhancer enhancer = enhancer(oauthConfig(provider, clientSecretAttribute()));

		assertEquals(200, call(enhancer));
		assertEquals(1, _discoveryRequests);
		assertEquals(1, _tokenRequests.size());
		assertEquals("Bearer token-1", lastApiRequest().header(AUTHORIZATION));
	}

	public void testWriteEnvironmentReference() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		ServiceMethodRegistry.Config<?> config = parse(basicConfig("", environmentPassword()));

		String xml = toXML(config);
		assertFalse("Secret written: " + xml, xml.contains(PASSWORD));
		assertTrue("Variable not written: " + xml, xml.contains(VARIABLE));

		ClientBasicAuthSecret copy = (ClientBasicAuthSecret) parse(xml).getSecrets().get(DOMAIN);
		assertEquals(VARIABLE, ((EnvironmentSecretSource.Config) copy.getLogin().getPassword()).getName());
	}

	public void testWriteLiteralEncrypted() throws Exception {
		ServiceMethodRegistry.Config<?> config =
			parse(oauthConfig(tokenUrlProvider(), clientSecretAttribute()));

		String xml = toXML(config);
		assertFalse("Secret written in plain text: " + xml, xml.contains(CLIENT_SECRET));
		assertTrue("Not written as encrypted attribute: " + xml,
			xml.contains("password=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		ClientCredentialSecret copy = (ClientCredentialSecret) parse(xml).getSecrets().get(DOMAIN);
		LiteralSecretSource.Config password = (LiteralSecretSource.Config) copy.getClientSecret().getPassword();
		assertEquals(CLIENT_SECRET, password.getValue());
	}

	private static String apiKeyConfig(String position, String keyAttribute, String keyElement) {
		return registry(
			"<api-key-authentication domain='" + DOMAIN + "' parameter-name='" + HEADER + "' position='"
				+ position + "'/>",
			"<api-key-client-secret domain='" + DOMAIN + "' " + keyAttribute + ">" + keyElement
				+ "</api-key-client-secret>");
	}

	private static String environmentKey() {
		// The OpenAPI key preselects a literal, so a key from the environment names its class.
		return "<api-key class='" + EnvironmentSecretSource.class.getName() + "' name='" + VARIABLE + "'/>";
	}

	private String basicConfig(String passwordAttribute, String passwordElement) {
		return registry(
			"<basic-authentication domain='" + DOMAIN + "'/>",
			"<basic-auth-client-secret domain='" + DOMAIN + "'>"
				+ "<login user='" + USER + "' " + passwordAttribute + ">" + passwordElement + "</login>"
				+ "</basic-auth-client-secret>");
	}

	private String oauthConfig(String uriProvider, String clientSecret) {
		return registry(
			"<client-credentials-authentication domain='" + DOMAIN + "'>" + uriProvider
				+ "</client-credentials-authentication>",
			"<client-credentials-client-secret domain='" + DOMAIN + "'>" + clientSecret
				+ "</client-credentials-client-secret>");
	}

	private String tokenUrlProvider() {
		return "<uri-provider class='" + DefaultURIProvider.class.getName() + "' token-url='" + _baseUrl
			+ TOKEN_PATH + "'/>";
	}

	private static String clientSecretAttribute() {
		return "<client-secret client-id='" + CLIENT_ID + "' password='unencrypted:" + CLIENT_SECRET + "'/>";
	}

	private static String environmentPassword() {
		return "<password name='" + VARIABLE + "'/>";
	}

	private static String registry(String authentication, String secret) {
		return "<instance class='" + ServiceMethodRegistry.class.getName() + "'>"
			+ "<authentications>" + authentication + "</authentications>"
			+ "<secrets>" + secret + "</secrets>"
			+ "</instance>";
	}

	private static ServiceMethodRegistry.Config<?> parse(String xml) throws ConfigurationException {
		return (ServiceMethodRegistry.Config<?>) TypedConfiguration.parse("instance",
			ServiceMethodRegistry.Config.class, CharacterContents.newContent(xml));
	}

	private static String toXML(ServiceMethodRegistry.Config<?> config) throws Exception {
		StringWriter buffer = new StringWriter();
		try (ConfigurationWriter writer = new ConfigurationWriter(buffer)) {
			writer.write("instance", ServiceMethodRegistry.Config.class, config);
		}
		return buffer.toString();
	}

	/**
	 * The {@link SecurityEnhancer} of the authentication {@link #DOMAIN} of the given registry
	 * configuration, created as the registry does for its methods.
	 */
	private static SecurityEnhancer enhancer(String xml) throws ConfigurationException {
		ServiceMethodRegistry.Config<?> config = parse(xml);
		ServiceMethodRegistry registry =
			(ServiceMethodRegistry) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		ClientAuthentication.Config<?> authentication = config.getAuthentications().get(DOMAIN);
		return TypedConfigUtil.createInstance(authentication).createSecurityEnhancer(registry);
	}

	/**
	 * Sends a request to the {@link #API_PATH} as the registry does.
	 *
	 * @return The status of the response.
	 */
	private int call(SecurityEnhancer enhancer) throws Exception {
		UriBuilder urlBuilder = new UriBuilder(_baseUrl + API_PATH);
		enhancer.enhanceUrl(urlBuilder);
		BasicClassicHttpRequest request = new BasicClassicHttpRequest("GET", urlBuilder.build());
		enhancer.enhanceRequest(_client, request);
		return _client.execute(request, response -> {
			enhancer.handleResponse(response);
			return Integer.valueOf(response.getCode());
		}).intValue();
	}

	private Received lastApiRequest() {
		return _apiRequests.get(_apiRequests.size() - 1);
	}

	private static String basic(String user, String password) {
		return "Basic "
			+ Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
	}

	private void handleApi(HttpExchange exchange) throws IOException {
		_apiRequests.add(received(exchange));
		send(exchange, _apiStatus, "{}");
	}

	private void handleToken(HttpExchange exchange) throws IOException {
		_tokenRequests.add(received(exchange));
		Map<String, Object> token = new HashMap<>();
		token.put("access_token", "token-" + _tokenRequests.size());
		token.put("token_type", "Bearer");
		if (_expiresIn != null) {
			token.put("expires_in", _expiresIn);
		}
		send(exchange, 200, JSON.toString(token));
	}

	private void handleDiscovery(HttpExchange exchange) throws IOException {
		_discoveryRequests++;
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("issuer", _baseUrl + ISSUER_PATH);
		metadata.put("token_endpoint", _baseUrl + TOKEN_PATH);
		send(exchange, 200, JSON.toString(metadata));
	}

	private static Received received(HttpExchange exchange) throws IOException {
		Map<String, String> headers = new HashMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		return new Received(headers, exchange.getRequestURI().getRawQuery(), parseForm(body));
	}

	private static Map<String, String> parseForm(String body) {
		Map<String, String> result = new HashMap<>();
		if (body.isEmpty()) {
			return result;
		}
		for (String pair : body.split("&")) {
			int separator = pair.indexOf('=');
			result.put(URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8),
				URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8));
		}
		return result;
	}

	private static void send(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	/**
	 * Reading a configuration resolves its types through the <code>TypeIndex</code>.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestClientAuthentication.class));
	}

}
