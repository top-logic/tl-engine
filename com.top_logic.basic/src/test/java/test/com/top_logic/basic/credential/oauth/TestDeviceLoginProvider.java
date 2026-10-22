/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.credential.oauth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import junit.framework.Test;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.oauth.DeviceLoginProvider;
import com.top_logic.basic.credential.oauth.FileRefreshTokenStore;
import com.top_logic.basic.credential.oauth.I18NConstants;
import com.top_logic.basic.credential.oauth.OAuthErrorException;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;

/**
 * Test case for {@link DeviceLoginProvider} and {@link FileRefreshTokenStore} against a mock
 * authorization server.
 */
@SuppressWarnings("javadoc")
public class TestDeviceLoginProvider extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	private static final String CLIENT_ID = "dev-client";

	private static final String REALM_PATH = "/realms/test";

	private static final String TOKEN_PATH = "/token";

	private static final String DEVICE_PATH = "/device";

	private static final String VERIFY_PATH = "/verify";

	private static final String GRANT_DEVICE = "urn:ietf:params:oauth:grant-type:device_code";

	private static final String GRANT_REFRESH = "refresh_token";

	/**
	 * Prefix of every secret value issued by the mock server.
	 */
	private static final String SECRET = "s3cret-";

	public interface Config extends ConfigurationItem {

		String CREDENTIALS = "credentials";

		@Name(CREDENTIALS)
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

	}

	/**
	 * {@link DeviceLoginProvider} recording the requested polling intervals without waiting for
	 * them.
	 */
	public static class TestingProvider extends DeviceLoginProvider {

		final List<Long> _waits = new CopyOnWriteArrayList<>();

		public TestingProvider(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected void waitBeforePoll(long millis) throws InterruptedException {
			_waits.add(Long.valueOf(millis));
			Thread.sleep(5);
		}

	}

	private HttpServer _server;

	private ExecutorService _executor;

	private String _baseUrl;

	private Path _dir;

	private Path _tokenFile;

	private final List<Map<String, String>> _deviceRequests = new CopyOnWriteArrayList<>();

	private final List<Map<String, String>> _devicePolls = new CopyOnWriteArrayList<>();

	private final List<Map<String, String>> _refreshRequests = new CopyOnWriteArrayList<>();

	/**
	 * Responses to device code polls, by number of the poll (starting with 1).
	 */
	private volatile Function<Integer, Object[]> _pollResponder;

	/**
	 * Responses to refresh requests, by number of the request (starting with 1).
	 */
	private volatile Function<Integer, Object[]> _refreshResponder;

	private volatile long _deviceDelay;

	private CollectingLogListener _log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dir = Files.createTempDirectory("TestDeviceLoginProvider");
		_tokenFile = _dir.resolve("store").resolve("token.json");
		_pollResponder = count -> ok("access-" + count, "refresh-" + count);
		_refreshResponder = count -> ok("access-r" + count, "refresh-r" + count);
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_executor = Executors.newCachedThreadPool();
		_server.setExecutor(_executor);
		_baseUrl = "http://localhost:" + _server.getAddress().getPort();
		_server.createContext(REALM_PATH + "/.well-known/openid-configuration", this::handleDiscovery);
		_server.createContext(DEVICE_PATH, this::handleDevice);
		_server.createContext(TOKEN_PATH, this::handleToken);
		_server.start();
		_log = new CollectingLogListener(EnumSet.allOf(Level.class), true);
	}

	@Override
	protected void tearDown() throws Exception {
		_log.deactivate();
		// A login still pending ends when the server is gone.
		_server.stop(0);
		awaitLoginEnd();
		_executor.shutdownNow();
		try (Stream<Path> files = Files.walk(_dir)) {
			files.sorted(Comparator.reverseOrder()).forEach(file -> file.toFile().delete());
		}
		super.tearDown();
	}

	private void handleDiscovery(HttpExchange exchange) throws IOException {
		Map<String, Object> metadata = new LinkedHashMap<>();
		metadata.put("issuer", issuer());
		metadata.put("token_endpoint", _baseUrl + TOKEN_PATH);
		metadata.put("device_authorization_endpoint", _baseUrl + DEVICE_PATH);
		send(exchange, 200, JSON.toString(metadata));
	}

	private void handleDevice(HttpExchange exchange) throws IOException {
		_deviceRequests.add(parseForm(exchange));
		int count = _deviceRequests.size();
		if (_deviceDelay > 0) {
			try {
				Thread.sleep(_deviceDelay);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("device_code", SECRET + "device-" + count);
		json.put("user_code", "CODE-" + count);
		json.put("verification_uri", _baseUrl + VERIFY_PATH);
		json.put("verification_uri_complete", _baseUrl + VERIFY_PATH + "?user_code=CODE-" + count);
		json.put("expires_in", Integer.valueOf(600));
		json.put("interval", Integer.valueOf(1));
		send(exchange, 200, JSON.toString(json));
	}

	private void handleToken(HttpExchange exchange) throws IOException {
		Map<String, String> form = parseForm(exchange);
		Object[] response;
		String grantType = form.get("grant_type");
		if (GRANT_DEVICE.equals(grantType)) {
			_devicePolls.add(form);
			response = _pollResponder.apply(Integer.valueOf(_devicePolls.size()));
		} else if (GRANT_REFRESH.equals(grantType)) {
			_refreshRequests.add(form);
			response = _refreshResponder.apply(Integer.valueOf(_refreshRequests.size()));
		} else {
			response = error("unsupported_grant_type");
		}
		send(exchange, ((Integer) response[0]).intValue(), (String) response[1]);
	}

	private static Map<String, String> parseForm(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
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

	private static void send(HttpExchange exchange, int status, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	private static Object[] ok(String accessToken, String refreshToken) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("access_token", SECRET + accessToken);
		json.put("token_type", "Bearer");
		json.put("expires_in", Integer.valueOf(300));
		if (refreshToken != null) {
			json.put("refresh_token", SECRET + refreshToken);
		}
		return new Object[] { Integer.valueOf(200), JSON.toString(json) };
	}

	private static Object[] error(String code) {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("error", code);
		return new Object[] { Integer.valueOf(400), JSON.toString(json) };
	}

	private String issuer() {
		return _baseUrl + REALM_PATH;
	}

	public void testDeviceLogin() throws Exception {
		_pollResponder = count -> count.intValue() <= 2 ? error("authorization_pending") : ok("access-1", "refresh-1");
		TestingProvider provider = provider(true);

		I18NRuntimeException instruction = assertLoginRequired(provider, 1);
		Map<String, String> deviceRequest = _deviceRequests.get(0);
		assertEquals(CLIENT_ID, deviceRequest.get("client_id"));
		assertEquals("openid offline_access", deviceRequest.get("scope"));
		assertEquals(1, countLogged("CODE-1"));

		awaitLoginEnd();
		assertEquals(3, _devicePolls.size());
		assertEquals(SECRET + "device-1", _devicePolls.get(0).get("device_code"));
		assertEquals(CLIENT_ID, _devicePolls.get(0).get("client_id"));
		assertEquals(Collections.nCopies(3, Long.valueOf(1000)), provider._waits);

		assertEquals("Bearer " + SECRET + "access-1", authenticate(provider));
		assertEquals(SECRET + "refresh-1", storedRefreshToken());
		assertEquals(1, _deviceRequests.size());
		assertEquals(0, _refreshRequests.size());

		// Invalidation keeps the refresh token.
		provider.invalidate();
		assertEquals("Bearer " + SECRET + "access-r1", authenticate(provider));
		assertEquals(SECRET + "refresh-1", _refreshRequests.get(0).get("refresh_token"));
		assertEquals(SECRET + "refresh-r1", storedRefreshToken());
		assertEquals(1, _deviceRequests.size());

		assertNoSecret(instruction);
		assertNoSecretLogged();
	}

	public void testPendingLoginRepeatsInstruction() throws Exception {
		_pollResponder = count -> error("authorization_pending");
		TestingProvider provider = provider(false);

		I18NRuntimeException first = assertLoginRequired(provider, 1);
		I18NRuntimeException second = assertLoginRequired(provider, 1);
		assertEquals(first.getErrorKey(), second.getErrorKey());
		assertEquals(1, _deviceRequests.size());
		assertEquals(1, countLogged("CODE-1"));
	}

	public void testSingleLoginUnderConcurrency() throws Exception {
		_pollResponder = count -> error("authorization_pending");
		_deviceDelay = 100;
		TestingProvider provider = provider(false);

		int threads = 8;
		ExecutorService callers = Executors.newFixedThreadPool(threads);
		try {
			CountDownLatch start = new CountDownLatch(1);
			List<Future<Object>> results = new ArrayList<>();
			for (int n = 0; n < threads; n++) {
				results.add(callers.submit(() -> {
					start.await();
					try {
						return authenticate(provider);
					} catch (I18NRuntimeException ex) {
						return ex.getErrorKey();
					}
				}));
			}
			start.countDown();
			for (Future<Object> result : results) {
				assertEquals(instructionKey(1), result.get(10, TimeUnit.SECONDS));
			}
		} finally {
			callers.shutdownNow();
		}
		assertEquals(1, _deviceRequests.size());
		assertEquals(1, countLogged("CODE-1"));
	}

	public void testSlowDown() throws Exception {
		_pollResponder = count -> {
			switch (count.intValue()) {
				case 1:
					return error("slow_down");
				case 2:
					return error("authorization_pending");
				default:
					return ok("access-1", "refresh-1");
			}
		};
		TestingProvider provider = provider(false);

		assertLoginRequired(provider, 1);
		awaitLoginEnd();
		assertEquals(List.of(Long.valueOf(1000), Long.valueOf(6000), Long.valueOf(6000)), provider._waits);
		assertEquals("Bearer " + SECRET + "access-1", authenticate(provider));
	}

	public void testAccessDenied() throws Exception {
		checkFailedLogin("access_denied");
	}

	public void testExpiredToken() throws Exception {
		checkFailedLogin("expired_token");
	}

	private void checkFailedLogin(String error) throws Exception {
		_pollResponder = count -> count.intValue() == 1 ? error(error) : error("authorization_pending");
		TestingProvider provider = provider(false);

		assertLoginRequired(provider, 1);
		awaitLoginEnd();
		assertEquals(1, _devicePolls.size());

		// The failed login is followed by a new one.
		assertLoginRequired(provider, 2);
		assertEquals(2, _deviceRequests.size());
		assertEquals(1, countLogged("CODE-2"));
	}

	public void testStoredRefreshToken() throws Exception {
		TestingProvider first = provider(true);
		assertLoginRequired(first, 1);
		awaitLoginEnd();
		assertEquals(SECRET + "refresh-1", storedRefreshToken());

		// Restart: the stored refresh token is used without a new login.
		TestingProvider second = provider(true);
		assertEquals("Bearer " + SECRET + "access-r1", authenticate(second));
		assertEquals(1, _deviceRequests.size());
		assertEquals(SECRET + "refresh-1", _refreshRequests.get(0).get("refresh_token"));
		assertEquals(GRANT_REFRESH, _refreshRequests.get(0).get("grant_type"));
		assertEquals(CLIENT_ID, _refreshRequests.get(0).get("client_id"));

		// The rotated refresh token is persisted.
		assertEquals(SECRET + "refresh-r1", storedRefreshToken());

		// A token response without refresh token keeps the stored one.
		_refreshResponder = count -> ok("access-r" + count, null);
		second.invalidate();
		assertEquals("Bearer " + SECRET + "access-r2", authenticate(second));
		assertEquals(SECRET + "refresh-r1", _refreshRequests.get(1).get("refresh_token"));
		assertEquals(SECRET + "refresh-r1", storedRefreshToken());

		assertNoSecretLogged();
	}

	public void testRejectedRefreshToken() throws Exception {
		_refreshResponder = count -> error("invalid_grant");
		storeRefreshToken("revoked");

		TestingProvider provider = provider(true);
		I18NRuntimeException instruction = assertLoginRequired(provider, 1);
		assertEquals(1, _refreshRequests.size());
		assertFalse(Files.exists(_tokenFile));

		awaitLoginEnd();
		assertEquals("Bearer " + SECRET + "access-1", authenticate(provider));
		assertEquals(SECRET + "refresh-1", storedRefreshToken());

		assertNoSecret(instruction);
		assertNoSecretLogged();
	}

	public void testRefreshServerError() throws Exception {
		_refreshResponder = count -> error("invalid_client");
		storeRefreshToken("valid");

		TestingProvider provider = provider(true);
		try {
			authenticate(provider);
			fail("Server error not reported.");
		} catch (OAuthErrorException ex) {
			assertEquals("invalid_client", ex.getError());
			assertNoSecret(ex);
		}
		// No login started, the refresh token is kept.
		assertEquals(0, _deviceRequests.size());
		assertEquals(SECRET + "valid", storedRefreshToken());
	}

	public void testForeignStoreIgnored() throws Exception {
		Files.createDirectories(_tokenFile.getParent());
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("server", issuer());
		json.put("client_id", "other-client");
		json.put("refresh_token", SECRET + "foreign");
		Files.writeString(_tokenFile, JSON.toString(json));

		TestingProvider provider = provider(true);
		assertLoginRequired(provider, 1);
		assertEquals(0, _refreshRequests.size());
	}

	public void testFilePermissions() throws Exception {
		TestingProvider provider = provider(true);
		assertLoginRequired(provider, 1);
		awaitLoginEnd();
		assertTrue(Files.exists(_tokenFile));
		if (!_tokenFile.getFileSystem().supportedFileAttributeViews().contains("posix")) {
			return;
		}
		assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(_tokenFile)));
		assertEquals("rwx------",
			PosixFilePermissions.toString(Files.getPosixFilePermissions(_tokenFile.getParent())));
		try (Stream<Path> files = Files.list(_tokenFile.getParent())) {
			assertEquals("No temporary file left.", 1, files.count());
		}
	}

	public void testDefaultFile() {
		Path file = FileRefreshTokenStore.defaultFile("https://login.top-logic.com/realms/dev", "my-app");
		assertEquals(Path.of(System.getProperty("user.home"), ".config", "toplogic", "oauth",
			"login.top-logic.com_realms_dev__my-app.json"), file);
		assertFalse(file.equals(FileRefreshTokenStore.defaultFile("https://login.top-logic.com/realms/dev", "other")));
	}

	public void testNoDeviceEndpoint() {
		try {
			Config config = read("<config><credentials class='" + DeviceLoginProvider.class.getName() + "' client-id='"
				+ CLIENT_ID + "' token-url='" + _baseUrl + TOKEN_PATH + "'/></config>");
			getInstance(config.getCredentials());
			fail("Missing device authorization endpoint not reported.");
		} catch (Throwable ex) {
			assertTrue(String.valueOf(ex.getMessage()),
				causedBy(ex, I18NConstants.ERROR_NO_DEVICE_AUTHORIZATION_ENDPOINT));
		}
	}

	public void testExplicitDeviceEndpoint() throws Exception {
		Config config = read("<config><credentials class='" + TestingProvider.class.getName() + "' client-id='"
			+ CLIENT_ID + "' token-url='" + _baseUrl + TOKEN_PATH + "' device-authorization-url='" + _baseUrl
			+ DEVICE_PATH + "'/></config>");
		TestingProvider provider = (TestingProvider) getInstance(config.getCredentials());
		assertLoginRequired(provider, 1);
		awaitLoginEnd();
		assertEquals("Bearer " + SECRET + "access-1", authenticate(provider));
	}

	private TestingProvider provider(boolean withStore) throws ConfigurationException {
		String store = withStore
			? "<refresh-token-store class='" + FileRefreshTokenStore.class.getName() + "' path='" + _tokenFile + "'/>"
			: "";
		Config config = read("<config><credentials class='" + TestingProvider.class.getName() + "' client-id='"
			+ CLIENT_ID + "' issuer='" + issuer() + "' scope='openid offline_access'>" + store
			+ "</credentials></config>");
		return (TestingProvider) getInstance(config.getCredentials());
	}

	private void storeRefreshToken(String token) throws ConfigurationException {
		Config config = read("<config><credentials class='" + TestingProvider.class.getName() + "' client-id='"
			+ CLIENT_ID + "' issuer='" + issuer() + "'><refresh-token-store class='"
			+ FileRefreshTokenStore.class.getName() + "' path='" + _tokenFile + "'/></credentials></config>");
		DeviceLoginProvider.Config providerConfig = (DeviceLoginProvider.Config) config.getCredentials();
		getInstance(providerConfig.getRefreshTokenStore()).store(issuer(), CLIENT_ID, SECRET + token);
	}

	private String storedRefreshToken() throws IOException {
		Map<?, ?> json = (Map<?, ?>) parseJson(Files.readString(_tokenFile));
		assertEquals(issuer(), json.get("server"));
		assertEquals(CLIENT_ID, json.get("client_id"));
		return (String) json.get("refresh_token");
	}

	private static Object parseJson(String content) {
		try {
			return JSON.fromString(content);
		} catch (JSON.ParseException ex) {
			throw new AssertionError(ex);
		}
	}

	private I18NRuntimeException assertLoginRequired(CredentialProvider provider, int login) {
		try {
			authenticate(provider);
			fail("Login not required.");
			return null;
		} catch (I18NRuntimeException ex) {
			assertEquals(instructionKey(login), ex.getErrorKey());
			return ex;
		}
	}

	private Object instructionKey(int login) {
		return I18NConstants.ERROR_DEVICE_LOGIN_REQUIRED__URL_CODE_CLIENT.fill(
			_baseUrl + VERIFY_PATH + "?user_code=CODE-" + login, "CODE-" + login, CLIENT_ID);
	}

	private static String authenticate(CredentialProvider provider) {
		Map<String, String> headers = new ConcurrentHashMap<>();
		provider.authenticate(headers::put);
		assertEquals(Collections.singleton("Authorization"), headers.keySet());
		return headers.get("Authorization");
	}

	/**
	 * Waits until the background threads of all device logins have ended.
	 */
	private static void awaitLoginEnd() throws InterruptedException {
		for (Thread thread : Thread.getAllStackTraces().keySet()) {
			if (thread.getName().equals("DeviceLogin-" + CLIENT_ID)) {
				thread.join(10000);
				assertFalse("Device login does not end.", thread.isAlive());
			}
		}
	}

	private int countLogged(String text) {
		int result = 0;
		for (LogEntry entry : _log.getLogEntries()) {
			if (entry.getPriority() == Level.WARN && entry.getMessage().contains(text)) {
				result++;
			}
		}
		return result;
	}

	private void assertNoSecretLogged() {
		for (LogEntry entry : _log.getLogEntries()) {
			assertFalse("Secret in log: " + entry.getMessage(), entry.getMessage().contains(SECRET));
			if (entry.getException().hasValue()) {
				assertNoSecret(entry.getException().get());
			}
		}
	}

	private static void assertNoSecret(Throwable ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			String message = cause.getMessage();
			assertTrue("Secret in message: " + message, message == null || !message.contains(SECRET));
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

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestDeviceLoginProvider.class);
	}

}
