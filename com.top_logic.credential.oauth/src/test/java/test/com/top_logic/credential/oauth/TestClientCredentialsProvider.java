/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.credential.oauth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.EnvironmentSecretSource;
import com.top_logic.basic.credential.FileSecretSource;
import com.top_logic.credential.oauth.ClientAssertionAuthentication;
import com.top_logic.credential.oauth.ClientCredentialsProvider;
import com.top_logic.credential.oauth.ClientSecretAuthentication;
import com.top_logic.credential.oauth.I18NConstants;
import com.top_logic.credential.oauth.JwsAlgorithm;
import com.top_logic.credential.oauth.OAuthErrorException;
import com.top_logic.credential.oauth.PrivateKeyJwtAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;

/**
 * Test case for {@link ClientCredentialsProvider} against a mock authorization server.
 */
@SuppressWarnings("javadoc")
public class TestClientCredentialsProvider extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	private static final String CLIENT_ID = "my-app";

	private static final String TOKEN_PATH = "/token";

	private static final String REALM_PATH = "/realms/test";

	private static final String SECRET_VARIABLE = "test.client.credentials.secret";

	public interface Config extends ConfigurationItem {

		String CREDENTIALS = "credentials";

		@Name(CREDENTIALS)
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

	}

	/**
	 * A request received by the mock server.
	 */
	static final class Received {

		final Map<String, String> _headers;

		final Map<String, String> _parameters;

		Received(Map<String, String> headers, Map<String, String> parameters) {
			_headers = headers;
			_parameters = parameters;
		}

		String header(String name) {
			return _headers.get(name.toLowerCase());
		}

		String parameter(String name) {
			return _parameters.get(name);
		}

	}

	/**
	 * Response of the mock token endpoint.
	 */
	interface TokenResponder {
		/**
		 * @param count
		 *        Number of the token request, starting with 1.
		 * @return HTTP status and body.
		 */
		Object[] respond(int count);
	}

	private HttpServer _server;

	private ExecutorService _executor;

	private String _baseUrl;

	private final List<Received> _tokenRequests = new CopyOnWriteArrayList<>();

	private final AtomicInteger _discoveryRequests = new AtomicInteger();

	private volatile TokenResponder _responder;

	private volatile long _tokenDelay;

	private volatile String _announcedIssuer;

	private Path _dir;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.clearProperty(SECRET_VARIABLE);
		_dir = Files.createTempDirectory("TestClientCredentialsProvider");
		_responder = count -> new Object[] { 200, tokenJson("token-" + count, "Bearer", Integer.valueOf(3600)) };
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_executor = Executors.newCachedThreadPool();
		_server.setExecutor(_executor);
		_baseUrl = "http://localhost:" + _server.getAddress().getPort();
		_announcedIssuer = _baseUrl + REALM_PATH;
		_server.createContext(TOKEN_PATH, this::handleToken);
		_server.createContext(REALM_PATH + "/.well-known/openid-configuration", this::handleDiscovery);
		_server.start();
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(SECRET_VARIABLE);
		_server.stop(0);
		_executor.shutdownNow();
		try (Stream<Path> files = Files.walk(_dir)) {
			files.sorted(Comparator.reverseOrder()).forEach(file -> file.toFile().delete());
		}
		super.tearDown();
	}

	private void handleToken(HttpExchange exchange) throws IOException {
		Map<String, String> headers = new HashMap<>();
		exchange.getRequestHeaders().forEach((name, values) -> headers.put(name.toLowerCase(), values.get(0)));
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		_tokenRequests.add(new Received(headers, parseForm(body)));
		int count = _tokenRequests.size();
		if (_tokenDelay > 0) {
			try {
				Thread.sleep(_tokenDelay);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		Object[] response = _responder.respond(count);
		send(exchange, ((Integer) response[0]).intValue(), (String) response[1]);
	}

	private void handleDiscovery(HttpExchange exchange) throws IOException {
		_discoveryRequests.incrementAndGet();
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("issuer", _announcedIssuer);
		metadata.put("token_endpoint", tokenUrl());
		send(exchange, 200, JSON.toString(metadata));
	}

	private static void send(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	private static Map<String, String> parseForm(String body) {
		Map<String, String> result = new LinkedHashMap<>();
		if (body.isEmpty()) {
			return result;
		}
		for (String pair : body.split("&")) {
			int sep = pair.indexOf('=');
			result.put(URLDecoder.decode(pair.substring(0, sep), StandardCharsets.UTF_8),
				URLDecoder.decode(pair.substring(sep + 1), StandardCharsets.UTF_8));
		}
		return result;
	}

	private static String tokenJson(String token, String type, Integer expiresIn) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("access_token", token);
		if (type != null) {
			json.put("token_type", type);
		}
		if (expiresIn != null) {
			json.put("expires_in", expiresIn);
		}
		return JSON.toString(json);
	}

	private String tokenUrl() {
		return _baseUrl + TOKEN_PATH;
	}

	public void testClientSecretBasic() throws ConfigurationException {
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "' scope='llm read'",
			"<client-authentication class='" + ClientSecretAuthentication.class.getName()
				+ "' method='basic' secret='unencrypted:s3cr:et'/>"
				+ "<parameters><parameter name='audience' value='llm-gateway'/></parameters>");

		assertEquals("Bearer token-1", authenticate(provider));

		assertEquals(1, _tokenRequests.size());
		Received request = _tokenRequests.get(0);
		assertEquals("Basic " + base64("my-app:s3cr%3Aet"), request.header("Authorization"));
		assertTrue(request.header("Content-Type"), request.header("Content-Type").startsWith("application/x-www-form-urlencoded"));
		Map<String, String> expected = new LinkedHashMap<>();
		expected.put("grant_type", "client_credentials");
		expected.put("scope", "llm read");
		expected.put("audience", "llm-gateway");
		assertEquals(expected, request._parameters);
	}

	public void testClientSecretPost() throws ConfigurationException {
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'",
			"<client-authentication class='" + ClientSecretAuthentication.class.getName()
				+ "' method='post' secret='unencrypted:s3cr:et'/>");

		assertEquals("Bearer token-1", authenticate(provider));

		Received request = _tokenRequests.get(0);
		assertNull(request.header("Authorization"));
		Map<String, String> expected = new LinkedHashMap<>();
		expected.put("grant_type", "client_credentials");
		expected.put("client_id", CLIENT_ID);
		expected.put("client_secret", "s3cr:et");
		assertEquals(expected, request._parameters);
	}

	public void testPublicClientRejected() {
		try {
			provider("token-url='" + tokenUrl() + "'", "");
			fail("Client credentials grant without client authentication accepted.");
		} catch (Throwable ex) {
			assertTrue(String.valueOf(ex.getMessage()),
				causedBy(ex, I18NConstants.ERROR_CLIENT_AUTHENTICATION_REQUIRED__CLIENT.fill(CLIENT_ID)));
		}
	}

	public void testPrivateKeyJwtRS256() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		checkPrivateKeyJwt(JwsAlgorithm.RS256, generator.generateKeyPair());
	}

	public void testPrivateKeyJwtES256() throws Exception {
		checkPrivateKeyJwt(JwsAlgorithm.ES256, ecKeyPair("secp256r1"));
	}

	public void testPrivateKeyJwtPS384() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		checkPrivateKeyJwt(JwsAlgorithm.PS384, generator.generateKeyPair());
	}

	public void testPrivateKeyJwtES384() throws Exception {
		checkPrivateKeyJwt(JwsAlgorithm.ES384, ecKeyPair("secp384r1"));
	}

	public void testPrivateKeyJwkRS256() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		KeyPair keyPair = generator.generateKeyPair();
		JWK jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
			.privateKey((RSAPrivateKey) keyPair.getPrivate())
			.build();
		checkPrivateKeyJwt(JwsAlgorithm.RS256, keyPair, writeKey(jwk.toJSONString()), "key-1");
	}

	public void testPrivateKeyJwkKeyId() throws Exception {
		KeyPair keyPair = ecKeyPair("secp256r1");
		JWK jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
			.privateKey((ECPrivateKey) keyPair.getPrivate())
			.keyID("jwk-key")
			.build();
		checkPrivateKeyJwt(JwsAlgorithm.ES256, keyPair, writeKey(jwk.toJSONString()), null);
	}

	public void testPrivateKeyJwkWithoutPrivatePart() throws Exception {
		KeyPair keyPair = ecKeyPair("secp256r1");
		JWK jwk = new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic()).build();
		checkInvalidKey(JwsAlgorithm.ES256, writeKey(jwk.toJSONString()));
	}

	public void testPrivateKeyWrongCurve() throws Exception {
		checkInvalidKey(JwsAlgorithm.ES256, writePem(ecKeyPair("secp384r1")));
	}

	private static KeyPair ecKeyPair(String curve) throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
		generator.initialize(new ECGenParameterSpec(curve));
		return generator.generateKeyPair();
	}

	public void testPrivateKeyWrongType() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		checkInvalidKey(JwsAlgorithm.ES256, writePem(generator.generateKeyPair()));
	}

	private void checkInvalidKey(JwsAlgorithm algorithm, Path keyFile) throws Exception {
		CredentialProvider provider = privateKeyProvider(algorithm, keyFile, "key-1");

		try {
			authenticate(provider);
			fail("Key accepted for " + algorithm.name() + ".");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_PRIVATE_KEY__ALGORITHM.fill(algorithm.name()), ex.getErrorKey());
		}
		assertEquals(0, _tokenRequests.size());
	}

	private void checkPrivateKeyJwt(JwsAlgorithm algorithm, KeyPair keyPair) throws Exception {
		checkPrivateKeyJwt(algorithm, keyPair, writePem(keyPair), "key-1");
	}

	private void checkPrivateKeyJwt(JwsAlgorithm algorithm, KeyPair keyPair, Path keyFile, String keyId)
			throws Exception {
		CredentialProvider provider = privateKeyProvider(algorithm, keyFile, keyId);

		long before = System.currentTimeMillis() / 1000;
		assertEquals("Bearer token-1", authenticate(provider));
		provider.invalidate();
		assertEquals("Bearer token-2", authenticate(provider));
		long after = System.currentTimeMillis() / 1000;

		assertEquals(2, _tokenRequests.size());
		Set<Object> ids = new HashSet<>();
		for (Received request : _tokenRequests) {
			assertEquals("client_credentials", request.parameter("grant_type"));
			assertEquals(CLIENT_ID, request.parameter("client_id"));
			assertEquals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
				request.parameter("client_assertion_type"));
			SignedJWT jwt = SignedJWT.parse(request.parameter("client_assertion"));
			assertTrue("Invalid signature.", jwt.verify(verifier(keyPair.getPublic())));
			assertEquals(algorithm.getAlgorithm(), jwt.getHeader().getAlgorithm());
			assertEquals(keyId != null ? keyId : "jwk-key", jwt.getHeader().getKeyID());

			JWTClaimsSet claims = jwt.getJWTClaimsSet();
			assertEquals(CLIENT_ID, claims.getIssuer());
			assertEquals(CLIENT_ID, claims.getSubject());
			assertEquals(List.of(tokenUrl()), claims.getAudience());
			long iat = claims.getIssueTime().getTime() / 1000;
			long exp = claims.getExpirationTime().getTime() / 1000;
			assertTrue(before <= iat && iat <= after);
			assertEquals(60, exp - iat);
			String jti = claims.getJWTID();
			assertTrue(jti != null && !jti.isEmpty());
			ids.add(jti);
		}
		assertEquals("Assertion identifiers must be unique.", 2, ids.size());
	}

	private CredentialProvider privateKeyProvider(JwsAlgorithm algorithm, Path keyFile, String keyId)
			throws ConfigurationException {
		return provider("token-url='" + tokenUrl() + "'",
			"<client-authentication class='" + PrivateKeyJwtAuthentication.class.getName()
				+ "' algorithm='" + algorithm.name() + "'" + (keyId != null ? " key-id='" + keyId + "'" : "") + ">"
				+ "<private-key class='" + FileSecretSource.class.getName() + "' path='" + keyFile + "'/>"
				+ "</client-authentication>");
	}

	private Path writePem(KeyPair keyPair) throws IOException {
		return writeKey("-----BEGIN PRIVATE KEY-----\n"
			+ Base64.getMimeEncoder(64, new byte[] { '\n' }).encodeToString(keyPair.getPrivate().getEncoded())
			+ "\n-----END PRIVATE KEY-----\n");
	}

	private Path writeKey(String content) throws IOException {
		Path keyFile = _dir.resolve("key.pem");
		Files.write(keyFile, content.getBytes(StandardCharsets.US_ASCII));
		return keyFile;
	}

	private static JWSVerifier verifier(PublicKey key) throws Exception {
		if (key instanceof RSAPublicKey) {
			return new RSASSAVerifier((RSAPublicKey) key);
		}
		return new ECDSAVerifier((ECPublicKey) key);
	}

	public void testClientAssertionFromFile() throws Exception {
		Path assertionFile = _dir.resolve("sa-token");
		Files.write(assertionFile, "assertion-1\n".getBytes(StandardCharsets.UTF_8));
		CredentialProvider provider = assertionProvider(assertionFile);

		assertEquals("Bearer token-1", authenticate(provider));

		Files.write(assertionFile, "assertion-2-rotated\n".getBytes(StandardCharsets.UTF_8));
		provider.invalidate();
		assertEquals("Bearer token-2", authenticate(provider));

		assertEquals(2, _tokenRequests.size());
		Received first = _tokenRequests.get(0);
		assertEquals(CLIENT_ID, first.parameter("client_id"));
		assertEquals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer", first.parameter("client_assertion_type"));
		assertEquals("assertion-1", first.parameter("client_assertion"));
		assertEquals("Rotated assertion file must be read.", "assertion-2-rotated",
			_tokenRequests.get(1).parameter("client_assertion"));
	}

	private CredentialProvider assertionProvider(Path assertionFile) throws ConfigurationException {
		return provider("token-url='" + tokenUrl() + "'",
			"<client-authentication class='" + ClientAssertionAuthentication.class.getName() + "'>"
				+ "<assertion class='" + FileSecretSource.class.getName() + "' path='" + assertionFile + "'/>"
				+ "</client-authentication>");
	}

	public void testClientSecretAvailability() throws ConfigurationException {
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'",
			"<client-authentication class='" + ClientSecretAuthentication.class.getName() + "' method='post'>"
				+ "<secret class='" + EnvironmentSecretSource.class.getName() + "' name='" + SECRET_VARIABLE + "'/>"
				+ "</client-authentication>");
		assertFalse(provider.isAvailable());

		System.setProperty(SECRET_VARIABLE, "provided-later");
		assertTrue("A client secret provided later must make the same provider available.", provider.isAvailable());
		assertEquals("Bearer token-1", authenticate(provider));
		assertEquals("provided-later", _tokenRequests.get(0).parameter("client_secret"));
		assertEquals("The availability check must not contact the authorization server.", 1, _tokenRequests.size());
	}

	public void testClientAssertionAvailability() throws Exception {
		Path assertionFile = _dir.resolve("sa-token");
		CredentialProvider provider = assertionProvider(assertionFile);
		assertFalse(provider.isAvailable());

		Files.write(assertionFile, "assertion-1\n".getBytes(StandardCharsets.UTF_8));
		assertTrue(provider.isAvailable());
		assertEquals(0, _tokenRequests.size());
	}

	public void testPrivateKeyAvailability() throws Exception {
		Path keyFile = _dir.resolve("missing-key.pem");
		CredentialProvider provider = privateKeyProvider(JwsAlgorithm.RS256, keyFile, null);
		assertFalse(provider.isAvailable());

		Files.write(keyFile, "not a key".getBytes(StandardCharsets.UTF_8));
		assertTrue("A present but invalid key is reported on use, not hidden.", provider.isAvailable());
	}

	public void testDiscovery() throws ConfigurationException {
		CredentialProvider provider = provider("issuer='" + _baseUrl + REALM_PATH + "/'", secretPost());

		assertEquals("Bearer token-1", authenticate(provider));
		provider.invalidate();
		assertEquals("Bearer token-2", authenticate(provider));

		assertEquals(2, _tokenRequests.size());
		assertEquals("Discovery document must be cached.", 1, _discoveryRequests.get());
	}

	public void testDiscoveryIssuerMismatch() throws ConfigurationException {
		_announcedIssuer = "https://evil.example.com/realms/test";
		CredentialProvider provider = provider("issuer='" + _baseUrl + REALM_PATH + "'", secretPost());

		try {
			authenticate(provider);
			fail("Foreign issuer accepted.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_ISSUER_MISMATCH__EXPECTED_ACTUAL.fill(_baseUrl + REALM_PATH,
				_announcedIssuer), ex.getErrorKey());
		}
		assertEquals(0, _tokenRequests.size());
	}

	public void testCaching() throws ConfigurationException {
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		assertEquals("Bearer token-1", authenticate(provider));
		assertEquals("Bearer token-1", authenticate(provider));
		assertEquals(1, _tokenRequests.size());
	}

	public void testRenewalBeforeExpiry() throws Exception {
		_responder = count -> new Object[] { 200, tokenJson("token-" + count, "Bearer", Integer.valueOf(1)) };
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		assertEquals("Bearer token-1", authenticate(provider));
		assertEquals("Bearer token-1", authenticate(provider));

		// The token lives 1 s and is renewed after half its lifetime.
		Thread.sleep(700);
		assertEquals("Bearer token-2", authenticate(provider));
		assertEquals(2, _tokenRequests.size());
	}

	public void testInvalidate() throws ConfigurationException {
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		assertEquals("Bearer token-1", authenticate(provider));
		provider.invalidate();
		assertEquals("Bearer token-2", authenticate(provider));
		assertEquals("Bearer token-2", authenticate(provider));
		assertEquals(2, _tokenRequests.size());
	}

	public void testConcurrentSingleFlight() throws Exception {
		_tokenDelay = 300;
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		int threads = 10;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			CountDownLatch start = new CountDownLatch(1);
			List<Future<String>> results = new ArrayList<>();
			for (int n = 0; n < threads; n++) {
				results.add(pool.submit(() -> {
					start.await();
					return authenticate(provider);
				}));
			}
			start.countDown();
			for (Future<String> result : results) {
				assertEquals("Bearer token-1", result.get(10, TimeUnit.SECONDS));
			}
		} finally {
			pool.shutdownNow();
		}
		assertEquals("Concurrent requests must share one token request.", 1, _tokenRequests.size());
	}

	public void testErrorResponse() throws Exception {
		_responder = count -> new Object[] { 400,
			"{\"error\":\"invalid_client\",\"error_description\":\"Invalid client credentials\"}" };
		Path assertionFile = _dir.resolve("sa-token");
		Files.write(assertionFile, "secret-assertion-value".getBytes(StandardCharsets.UTF_8));

		CredentialProvider secretProvider = provider("token-url='" + tokenUrl() + "'",
			"<client-authentication class='" + ClientSecretAuthentication.class.getName()
				+ "' method='post' secret='unencrypted:super-secret-value'/>");
		checkError(secretProvider, "super-secret-value");
		checkError(assertionProvider(assertionFile), "secret-assertion-value");
	}

	private static void checkError(CredentialProvider provider, String secret) {
		try {
			authenticate(provider);
			fail("Error response not reported.");
		} catch (OAuthErrorException ex) {
			assertEquals(400, ex.getStatus());
			assertEquals("invalid_client", ex.getError());
			assertTrue("Error code missing: " + ex.getMessage(), ex.getMessage().contains("invalid_client"));
			assertNoSecret(ex, secret);
		}
	}

	public void testErrorWithoutErrorObject() throws ConfigurationException {
		_responder = count -> new Object[] { 503, "<html>Service unavailable</html>" };
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		try {
			authenticate(provider);
			fail("Error response not reported.");
		} catch (OAuthErrorException ex) {
			assertEquals(503, ex.getStatus());
			assertNull(ex.getError());
			assertEquals(I18NConstants.ERROR_REQUEST_FAILED__URL_STATUS.fill(tokenUrl(), Integer.valueOf(503)),
				ex.getErrorKey());
		}
	}

	public void testMalformedResponse() throws ConfigurationException {
		_responder = count -> new Object[] { 200, "{\"access_token\":\"leaked-token-value\", broken" };
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		try {
			authenticate(provider);
			fail("Malformed response accepted.");
		} catch (I18NRuntimeException ex) {
			assertEquals(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(tokenUrl()), ex.getErrorKey());
			assertNoSecret(ex, "leaked-token-value");
		}
	}

	public void testMissingExpiresIn() throws ConfigurationException {
		_responder = count -> new Object[] { 200, tokenJson("token-" + count, "bearer", null) };
		CredentialProvider provider = provider("token-url='" + tokenUrl() + "'", secretPost());

		assertEquals("Bearer token-1", authenticate(provider));
		assertEquals("Token without lifetime must be cached for the default lifetime.", "Bearer token-1",
			authenticate(provider));
		assertEquals(1, _tokenRequests.size());
	}

	public void testMissingExpiresInShortDefault() throws Exception {
		_responder = count -> new Object[] { 200, tokenJson("token-" + count, null, null) };
		CredentialProvider provider =
			provider("token-url='" + tokenUrl() + "' default-token-lifetime='1s'", secretPost());

		assertEquals("Bearer token-1", authenticate(provider));
		Thread.sleep(700);
		assertEquals("Bearer token-2", authenticate(provider));
	}

	public void testNoTokenEndpoint() {
		try {
			provider("", secretPost());
			fail("Missing token endpoint not reported.");
		} catch (Throwable ex) {
			assertTrue(String.valueOf(ex.getMessage()), causedBy(ex, I18NConstants.ERROR_NO_TOKEN_ENDPOINT));
		}
	}

	public void testAmbiguousTokenEndpoint() {
		try {
			provider("token-url='" + tokenUrl() + "' issuer='" + _baseUrl + REALM_PATH + "'", secretPost());
			fail("Ambiguous token endpoint not reported.");
		} catch (Throwable ex) {
			assertTrue(String.valueOf(ex.getMessage()), causedBy(ex, I18NConstants.ERROR_AMBIGUOUS_TOKEN_ENDPOINT));
		}
	}

	private static boolean causedBy(Throwable ex, Object key) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof I18NRuntimeException && key.equals(((I18NRuntimeException) cause).getErrorKey())) {
				return true;
			}
		}
		return false;
	}

	private static String secretPost() {
		return "<client-authentication class='" + ClientSecretAuthentication.class.getName()
			+ "' method='post' secret='unencrypted:s3cr3t'/>";
	}

	private CredentialProvider provider(String attributes, String content) throws ConfigurationException {
		Config config = read("<config><credentials class='" + ClientCredentialsProvider.class.getName()
			+ "' client-id='" + CLIENT_ID + "' " + attributes + ">" + content + "</credentials></config>");
		return getInstance(config.getCredentials());
	}

	private static String authenticate(CredentialProvider provider) {
		Map<String, String> headers = new ConcurrentHashMap<>();
		provider.authenticate(headers::put);
		assertEquals(Collections.singleton("Authorization"), headers.keySet());
		return headers.get("Authorization");
	}

	private static String base64(String value) {
		return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
	}

	private static void assertNoSecret(Throwable ex, String secret) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			assertTrue("Secret in message: " + message, message == null || !message.contains(secret));
		}
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestClientCredentialsProvider.class);
	}

}
