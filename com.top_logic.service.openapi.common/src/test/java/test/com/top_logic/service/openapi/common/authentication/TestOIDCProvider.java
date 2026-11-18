/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.common.authentication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.nimbusds.common.contenttype.ContentType;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionErrorResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionRequest;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.auth.PlainClientSecret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.oauth2.sdk.util.URLUtils;
import com.nimbusds.openid.connect.sdk.SubjectType;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * OpenID provider for tests, running in the test JVM on a free loopback port.
 *
 * <p>
 * The provider serves the OpenID discovery document below its {@link #getIssuerUrl() issuer}, a
 * token endpoint for the grant type <code>client_credentials</code> and a token introspection
 * endpoint. Clients are registered with {@link #addClient(String, String, String)}; a client
 * authenticates with its secret either by HTTP basic authentication or by form parameters. The
 * introspection of a token issued to a client reports the account registered for that client as
 * <code>username</code>. The caller of the introspection endpoint must authenticate as a
 * registered client.
 * </p>
 *
 * <p>
 * All protocol messages are created and parsed with the Nimbus OAuth 2.0 SDK.
 * </p>
 */
public class TestOIDCProvider {

	/**
	 * Default path of the {@link #getIssuerUrl() issuer} below the root of the server.
	 */
	public static final String DEFAULT_ISSUER_PATH = "/realms/test";

	/**
	 * Path of the OpenID discovery document relative to the issuer.
	 */
	public static final String DISCOVERY_PATH = "/.well-known/openid-configuration";

	/**
	 * Path of the token endpoint relative to the issuer.
	 */
	public static final String TOKEN_PATH = "/token";

	/**
	 * Path of the token introspection endpoint relative to the issuer.
	 */
	public static final String INTROSPECTION_PATH = "/introspect";

	/**
	 * Path of the (empty) JSON web key set relative to the issuer.
	 */
	public static final String JWKS_PATH = "/jwks";

	/**
	 * Prefix of the issued access token values, followed by the number of the token.
	 */
	public static final String TOKEN_PREFIX = "token-";

	/**
	 * Default lifetime of an issued token in seconds.
	 */
	public static final long DEFAULT_TOKEN_LIFETIME = 3600;

	/**
	 * A client registered at the provider.
	 */
	private static final class RegisteredClient {

		final String _secret;

		final String _account;

		RegisteredClient(String secret, String account) {
			_secret = secret;
			_account = account;
		}

	}

	/**
	 * An access token issued by the provider.
	 */
	private static final class IssuedToken {

		final ClientID _clientId;

		final String _account;

		final Date _issueTime;

		final Date _expirationTime;

		IssuedToken(ClientID clientId, String account, Date issueTime, Date expirationTime) {
			_clientId = clientId;
			_account = account;
			_issueTime = issueTime;
			_expirationTime = expirationTime;
		}

		boolean isActive() {
			return _expirationTime == null || _expirationTime.after(new Date());
		}

	}

	private final String _issuerPath;

	private final Map<String, RegisteredClient> _clients = new ConcurrentHashMap<>();

	private final Map<String, IssuedToken> _tokens = new ConcurrentHashMap<>();

	private final AtomicInteger _tokenCount = new AtomicInteger();

	private final AtomicInteger _discoveryRequests = new AtomicInteger();

	private final List<Map<String, String>> _tokenRequests = new CopyOnWriteArrayList<>();

	private volatile long _tokenLifetime = DEFAULT_TOKEN_LIFETIME;

	private HttpServer _server;

	private ExecutorService _executor;

	private String _issuerUrl;

	/**
	 * Creates a {@link TestOIDCProvider} with the issuer path {@link #DEFAULT_ISSUER_PATH}.
	 */
	public TestOIDCProvider() {
		this(DEFAULT_ISSUER_PATH);
	}

	/**
	 * Creates a {@link TestOIDCProvider}.
	 *
	 * @param issuerPath
	 *        Path of the issuer below the root of the server, starting with a slash.
	 */
	public TestOIDCProvider(String issuerPath) {
		_issuerPath = issuerPath;
	}

	/**
	 * Registers a client.
	 *
	 * @param clientId
	 *        The ID the client authenticates with.
	 * @param secret
	 *        The secret the client authenticates with.
	 * @param account
	 *        The name of the account reported as <code>username</code> by the introspection of
	 *        tokens issued to this client. May be <code>null</code>.
	 * @return This instance for call chaining.
	 */
	public TestOIDCProvider addClient(String clientId, String secret, String account) {
		_clients.put(clientId, new RegisteredClient(secret, account));
		return this;
	}

	/**
	 * Sets the lifetime of tokens issued from now on.
	 *
	 * @param seconds
	 *        The lifetime in seconds. A value of <code>0</code> issues tokens without lifetime, the
	 *        token response then contains no <code>expires_in</code>.
	 */
	public void setTokenLifetime(long seconds) {
		_tokenLifetime = seconds;
	}

	/**
	 * Starts the server on a free port of the loopback interface.
	 *
	 * <p>
	 * All threads of the server are daemon threads, so a running provider does not keep the JVM
	 * alive.
	 * </p>
	 */
	public void start() throws IOException {
		_server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		_executor = Executors.newCachedThreadPool(TestOIDCProvider::daemonThread);
		_server.setExecutor(_executor);
		_issuerUrl = "http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":"
			+ _server.getAddress().getPort() + _issuerPath;
		_server.createContext(_issuerPath + DISCOVERY_PATH, handler(this::discovery));
		_server.createContext(_issuerPath + TOKEN_PATH, handler(this::token));
		_server.createContext(_issuerPath + INTROSPECTION_PATH, handler(this::introspection));
		_server.createContext(_issuerPath + JWKS_PATH, handler(this::jwks));

		// The dispatcher thread of the server inherits the daemon flag of the thread starting it.
		Thread starter = daemonThread(_server::start);
		starter.start();
		try {
			starter.join();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while starting the OpenID provider.", ex);
		}
	}

	private static Thread daemonThread(Runnable task) {
		Thread thread = new Thread(task, TestOIDCProvider.class.getSimpleName());
		thread.setDaemon(true);
		return thread;
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		if (_server != null) {
			_server.stop(0);
			_executor.shutdownNow();
			_server = null;
			_executor = null;
		}
	}

	/**
	 * Whether the server is {@link #start() started} and not yet {@link #stop() stopped}.
	 */
	public boolean isRunning() {
		return _server != null;
	}

	/**
	 * The URL of the issuer, the discovery document is served at
	 * <code>[issuer-url]/.well-known/openid-configuration</code>.
	 */
	public String getIssuerUrl() {
		return _issuerUrl;
	}

	/**
	 * The URL of the token endpoint.
	 */
	public String getTokenEndpointUrl() {
		return _issuerUrl + TOKEN_PATH;
	}

	/**
	 * The URL of the token introspection endpoint.
	 */
	public String getIntrospectionEndpointUrl() {
		return _issuerUrl + INTROSPECTION_PATH;
	}

	/**
	 * The number of requests of the discovery document received so far.
	 */
	public int getDiscoveryRequestCount() {
		return _discoveryRequests.get();
	}

	/**
	 * The form parameters of all requests the token endpoint received so far, in the order of
	 * their arrival.
	 * 
	 * <p>
	 * Of a parameter given several times, the first value is reported.
	 * </p>
	 */
	public List<Map<String, String>> getTokenRequests() {
		return Collections.unmodifiableList(_tokenRequests);
	}

	private HTTPResponse discovery(HTTPRequest request) {
		_discoveryRequests.incrementAndGet();
		OIDCProviderMetadata metadata = new OIDCProviderMetadata(new Issuer(_issuerUrl),
			List.of(SubjectType.PUBLIC), URI.create(_issuerUrl + JWKS_PATH));
		metadata.setTokenEndpointURI(URI.create(getTokenEndpointUrl()));
		metadata.setIntrospectionEndpointURI(URI.create(getIntrospectionEndpointUrl()));
		metadata.setGrantTypes(List.of(GrantType.CLIENT_CREDENTIALS));
		List<ClientAuthenticationMethod> authMethods =
			List.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC, ClientAuthenticationMethod.CLIENT_SECRET_POST);
		metadata.setTokenEndpointAuthMethods(authMethods);
		metadata.setIntrospectionEndpointAuthMethods(authMethods);
		return json(metadata.toJSONObject().toJSONString());
	}

	private HTTPResponse jwks(HTTPRequest request) {
		return json(new JWKSet().toString(true));
	}

	private HTTPResponse token(HTTPRequest request) {
		_tokenRequests.add(formParameters(request));
		TokenRequest tokenRequest;
		try {
			tokenRequest = TokenRequest.parse(request);
		} catch (ParseException ex) {
			return new TokenErrorResponse(error(ex)).toHTTPResponse();
		}
		RegisteredClient client = authenticate(tokenRequest.getClientAuthentication());
		if (client == null) {
			return new TokenErrorResponse(OAuth2Error.INVALID_CLIENT).toHTTPResponse();
		}
		if (!(tokenRequest.getAuthorizationGrant() instanceof ClientCredentialsGrant)) {
			return new TokenErrorResponse(OAuth2Error.UNSUPPORTED_GRANT_TYPE).toHTTPResponse();
		}

		long lifetime = _tokenLifetime;
		Date now = new Date();
		Date expirationTime = lifetime > 0 ? new Date(now.getTime() + lifetime * 1000) : null;
		String value = TOKEN_PREFIX + _tokenCount.incrementAndGet();
		ClientID clientId = tokenRequest.getClientAuthentication().getClientID();
		_tokens.put(value, new IssuedToken(clientId, client._account, now, expirationTime));

		BearerAccessToken accessToken = new BearerAccessToken(value, lifetime, null);
		return new AccessTokenResponse(new Tokens(accessToken, null)).toHTTPResponse();
	}

	private HTTPResponse introspection(HTTPRequest request) {
		TokenIntrospectionRequest introspectionRequest;
		try {
			introspectionRequest = TokenIntrospectionRequest.parse(request);
		} catch (ParseException ex) {
			return new TokenIntrospectionErrorResponse(error(ex)).toHTTPResponse();
		}
		if (authenticate(introspectionRequest.getClientAuthentication()) == null) {
			return new TokenIntrospectionErrorResponse(OAuth2Error.INVALID_CLIENT).toHTTPResponse();
		}

		IssuedToken token = _tokens.get(introspectionRequest.getToken().getValue());
		if (token == null || !token.isActive()) {
			return new TokenIntrospectionSuccessResponse.Builder(false).build().toHTTPResponse();
		}
		return new TokenIntrospectionSuccessResponse.Builder(true)
			.issuer(new Issuer(_issuerUrl))
			.clientID(token._clientId)
			.username(token._account)
			.tokenType(AccessTokenType.BEARER)
			.issueTime(token._issueTime)
			.expirationTime(token._expirationTime)
			.build()
			.toHTTPResponse();
	}

	/**
	 * The registered client authenticated by the given client authentication, <code>null</code>
	 * if the authentication is missing or does not match a registered client.
	 */
	private RegisteredClient authenticate(ClientAuthentication authentication) {
		if (!(authentication instanceof PlainClientSecret)) {
			return null;
		}
		RegisteredClient client = _clients.get(authentication.getClientID().getValue());
		if (client == null) {
			return null;
		}
		String secret = ((PlainClientSecret) authentication).getClientSecret().getValue();
		if (!client._secret.equals(secret)) {
			return null;
		}
		return client;
	}

	private static Map<String, String> formParameters(HTTPRequest request) {
		Map<String, String> result = new LinkedHashMap<>();
		URLUtils.parseParameters(request.getBody())
			.forEach((name, values) -> result.put(name, values.isEmpty() ? null : values.get(0)));
		return Collections.unmodifiableMap(result);
	}

	private static ErrorObject error(ParseException ex) {
		ErrorObject error = ex.getErrorObject();
		return error != null ? error : OAuth2Error.INVALID_REQUEST.appendDescription(": " + ex.getMessage());
	}

	private static HTTPResponse json(String content) {
		HTTPResponse response = new HTTPResponse(HTTPResponse.SC_OK);
		response.setEntityContentType(ContentType.APPLICATION_JSON);
		response.setBody(content);
		return response;
	}

	/**
	 * Adapts a protocol endpoint working on Nimbus messages to the JDK HTTP server.
	 */
	private HttpHandler handler(Endpoint endpoint) {
		return exchange -> {
			try {
				send(exchange, endpoint.handle(toRequest(exchange)));
			} catch (RuntimeException ex) {
				ErrorObject error = OAuth2Error.SERVER_ERROR.appendDescription(": " + ex.getMessage());
				send(exchange, error.toHTTPResponse());
			} finally {
				exchange.close();
			}
		};
	}

	private HTTPRequest toRequest(HttpExchange exchange) throws IOException {
		URI uri = URI.create("http://" + InetAddress.getLoopbackAddress().getHostAddress() + ":"
			+ _server.getAddress().getPort() + exchange.getRequestURI().toString());
		HTTPRequest request = new HTTPRequest(HTTPRequest.Method.valueOf(exchange.getRequestMethod()), uri);
		exchange.getRequestHeaders()
			.forEach((name, values) -> request.setHeader(name, values.toArray(new String[values.size()])));
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		if (!body.isEmpty()) {
			request.setBody(body);
		}
		return request;
	}

	private static void send(HttpExchange exchange, HTTPResponse response) throws IOException {
		response.getHeaderMap().forEach((name, values) -> exchange.getResponseHeaders().put(name, values));
		String body = response.getBody();
		byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(response.getStatusCode(), bytes.length == 0 ? -1 : bytes.length);
		if (bytes.length > 0) {
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(bytes);
			}
		}
	}

	/**
	 * A protocol endpoint of the provider.
	 */
	private interface Endpoint {

		/**
		 * Creates the response to the given request.
		 */
		HTTPResponse handle(HTTPRequest request);

	}

}
