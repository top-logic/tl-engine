/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.oauth.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.GrantType;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.RefreshTokenGrant;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.client.ClientInformation;
import com.nimbusds.oauth2.sdk.client.ClientInformationResponse;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationErrorResponse;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationRequest;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationResponse;
import com.nimbusds.oauth2.sdk.client.RegistrationError;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.pkce.CodeChallenge;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.BearerTokenError;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;

import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.encryption.SecretTokens;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.oauth.server.model.OAuthAuthorizationCode;
import com.top_logic.oauth.server.model.OAuthClient;
import com.top_logic.oauth.server.model.OAuthGrant;
import com.top_logic.oauth.server.model.OAuthToken;
import com.top_logic.oauth.server.model.TlOauthServerFactory;
import com.top_logic.util.model.ModelService;

/**
 * The OAuth 2.1 authorization server of the application.
 *
 * <p>
 * Issues access tokens for the application's own endpoints (MCP, A2A, REST services) to public
 * clients, following the authorization profile of the Model Context Protocol:
 * </p>
 * <ul>
 * <li>Authorization code grant with PKCE, method <code>S256</code> only (RFC 7636).</li>
 * <li>Tokens bound to one resource of the application (RFC 8707).</li>
 * <li>Refresh tokens that rotate: an exchanged refresh token is invalid, and presenting it again
 * revokes the whole grant.</li>
 * <li>Clients identified by the URL of a client ID metadata document, or registered dynamically
 * (RFC 7591).</li>
 * <li>Authorization server metadata (RFC 8414).</li>
 * </ul>
 *
 * <p>
 * Tokens and codes are opaque random strings, see {@link SecretTokens}; only their SHA-256 is
 * stored. The HTTP endpoints are served by {@link OAuthServerServlet}; access tokens are accepted
 * by {@link OAuthAccessTokenAuthentication}.
 * </p>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	ModelService.Module.class,
})
@Label("OAuth authorization server")
public class OAuthAuthorizationServer extends ConfiguredManagedClass<OAuthAuthorizationServer.Config> {

	/**
	 * Context-relative path of the authorization server metadata (RFC 8414).
	 */
	public static final String METADATA_PATH = "/.well-known/oauth-authorization-server";

	/**
	 * Context-relative path under which the {@link #METADATA_PATH metadata} is served a second
	 * time, for clients that look for an OIDC discovery document by appending this path
	 * to the issuer.
	 */
	public static final String OPENID_CONFIGURATION_PATH = "/.well-known/openid-configuration";

	/**
	 * Context-relative path of the authorization endpoint.
	 */
	public static final String AUTHORIZATION_PATH = "/oauth/authorize";

	/**
	 * Context-relative path of the token endpoint.
	 */
	public static final String TOKEN_PATH = "/oauth/token";

	/**
	 * Context-relative path of the dynamic client registration endpoint (RFC 7591).
	 */
	public static final String REGISTRATION_PATH = "/oauth/register";

	/**
	 * Metadata parameter announcing that clients may identify themselves by the URL of a client ID
	 * metadata document.
	 */
	public static final String CLIENT_ID_METADATA_DOCUMENT_SUPPORTED = "client_id_metadata_document_supported";

	/** Prefix of an access token. */
	public static final String ACCESS_TOKEN_PREFIX = "tloat_";

	/** Prefix of a refresh token. */
	public static final String REFRESH_TOKEN_PREFIX = "tlort_";

	/** Prefix of an authorization code. */
	public static final String AUTHORIZATION_CODE_PREFIX = "tloac_";

	/**
	 * Prefix of the random value whose hash stands in {@link OAuthToken#getPredecessorHash()} of a
	 * token that replaced no other.
	 */
	private static final String NO_PREDECESSOR_PREFIX = "tlonp_";

	/** Prefix of the client ID of a dynamically registered client. */
	public static final String CLIENT_ID_PREFIX = "tlocl_";

	/**
	 * How stale {@link OAuthGrant#getLastUsed()} may be.
	 *
	 * <p>
	 * Writing it on every request would make every authenticated request a commit.
	 * </p>
	 */
	public static final Duration LAST_USED_GRANULARITY = Duration.ofMinutes(1);

	/**
	 * The grant types this server issues tokens for.
	 */
	public static final List<GrantType> GRANT_TYPES = List.of(GrantType.AUTHORIZATION_CODE, GrantType.REFRESH_TOKEN);

	/**
	 * Configuration of the {@link OAuthAuthorizationServer}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<OAuthAuthorizationServer> {

		/**
		 * Configuration name of {@link #getAccessTokenLifetime()}.
		 */
		String ACCESS_TOKEN_LIFETIME = "access-token-lifetime";

		/**
		 * Configuration name of {@link #getRefreshTokenLifetime()}.
		 */
		String REFRESH_TOKEN_LIFETIME = "refresh-token-lifetime";

		/**
		 * Configuration name of {@link #getAuthorizationCodeLifetime()}.
		 */
		String AUTHORIZATION_CODE_LIFETIME = "authorization-code-lifetime";

		/**
		 * Configuration name of {@link #getDynamicClientRegistration()}.
		 */
		String DYNAMIC_CLIENT_REGISTRATION = "dynamic-client-registration";

		/**
		 * Configuration name of {@link #getClientIdMetadataDocuments()}.
		 */
		String CLIENT_ID_METADATA_DOCUMENTS = "client-id-metadata-documents";

		/**
		 * Configuration name of {@link #getClientMetadataFetcher()}.
		 */
		String CLIENT_METADATA_FETCHER = "client-metadata-fetcher";

		/**
		 * Configuration name of {@link #getClientMetadataCacheSize()}.
		 */
		String CLIENT_METADATA_CACHE_SIZE = "client-metadata-cache-size";

		/**
		 * Configuration name of {@link #getClientMetadataCacheLifetime()}.
		 */
		String CLIENT_METADATA_CACHE_LIFETIME = "client-metadata-cache-lifetime";

		/**
		 * Configuration name of {@link #getMaxRegisteredClients()}.
		 */
		String MAX_REGISTERED_CLIENTS = "max-registered-clients";

		/**
		 * Configuration name of {@link #getUnusedClientLifetime()}.
		 */
		String UNUSED_CLIENT_LIFETIME = "unused-client-lifetime";

		/**
		 * Configuration name of {@link #getAbandonedClientLifetime()}.
		 */
		String ABANDONED_CLIENT_LIFETIME = "abandoned-client-lifetime";

		/**
		 * How long an access token is accepted after it was issued.
		 */
		@Name(ACCESS_TOKEN_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(DateUtil.HOUR_MILLIS)
		long getAccessTokenLifetime();

		/**
		 * How long a refresh token can be exchanged after it was issued.
		 *
		 * <p>
		 * Every exchange issues a new refresh token with this lifetime, so a client that is used at
		 * least once within this period stays signed in.
		 * </p>
		 */
		@Name(REFRESH_TOKEN_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(30 * DateUtil.DAY_MILLIS)
		long getRefreshTokenLifetime();

		/**
		 * How long an authorization code can be exchanged after it was issued.
		 */
		@Name(AUTHORIZATION_CODE_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(DateUtil.MINUTE_MILLIS)
		long getAuthorizationCodeLifetime();

		/**
		 * Whether clients may register themselves at the endpoint {@link #REGISTRATION_PATH}
		 * (RFC 7591).
		 *
		 * <p>
		 * Switched off, the endpoint answers <code>404</code> and the metadata names no
		 * registration endpoint. Clients registered before remain usable.
		 * </p>
		 */
		@Name(DYNAMIC_CLIENT_REGISTRATION)
		@BooleanDefault(true)
		boolean getDynamicClientRegistration();

		/**
		 * Whether clients may identify themselves by the <code>https</code> URL of a client ID
		 * metadata document, which is fetched by the {@link #getClientMetadataFetcher()}.
		 */
		@Name(CLIENT_ID_METADATA_DOCUMENTS)
		@BooleanDefault(true)
		boolean getClientIdMetadataDocuments();

		/**
		 * How a client ID metadata document is retrieved.
		 */
		@Name(CLIENT_METADATA_FETCHER)
		@ItemDefault
		@ImplementationClassDefault(HttpClientMetadataFetcher.class)
		PolymorphicConfiguration<? extends ClientMetadataFetcher> getClientMetadataFetcher();

		/**
		 * The number of client ID metadata documents kept in memory.
		 */
		@Name(CLIENT_METADATA_CACHE_SIZE)
		@IntDefault(1000)
		int getClientMetadataCacheSize();

		/**
		 * How long a fetched client ID metadata document is used before it is fetched again.
		 */
		@Name(CLIENT_METADATA_CACHE_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(DateUtil.HOUR_MILLIS)
		long getClientMetadataCacheLifetime();

		/**
		 * The number of dynamically registered clients beyond which no further client is
		 * registered.
		 *
		 * <p>
		 * Registration needs no authentication, so this bounds what an anonymous caller can store.
		 * Together with the clean-up of clients nobody authorized
		 * ({@link #getUnusedClientLifetime()}), it limits a flood of registrations to a temporary
		 * refusal of new clients.
		 * </p>
		 */
		@Name(MAX_REGISTERED_CLIENTS)
		@IntDefault(1000)
		int getMaxRegisteredClients();

		/**
		 * How long a dynamically registered client that was never authorized is kept after its
		 * registration.
		 *
		 * <p>
		 * A client registers itself immediately before it asks a person for authorization, so a
		 * client nobody authorized within this time is abandoned.
		 * </p>
		 */
		@Name(UNUSED_CLIENT_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(DateUtil.DAY_MILLIS)
		long getUnusedClientLifetime();

		/**
		 * How long a dynamically registered client without any remaining authorization is kept
		 * after it was last used (authorized, or a token refreshed).
		 *
		 * <p>
		 * An authorization ends when it is revoked or when its last refresh token expired, see
		 * {@link #getRefreshTokenLifetime()}.
		 * </p>
		 */
		@Name(ABANDONED_CLIENT_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(90 * DateUtil.DAY_MILLIS)
		long getAbandonedClientLifetime();

	}

	/**
	 * A token pair just issued for a grant.
	 *
	 * @param grant
	 *        The grant the tokens belong to.
	 * @param accessToken
	 *        The access token, which exists only here.
	 * @param refreshToken
	 *        The refresh token, which exists only here.
	 */
	private record IssuedTokens(OAuthGrant grant, String accessToken, String refreshToken) {
		@Override
		public String toString() {
			// Keeps the tokens out of anything that prints this record.
			return "IssuedTokens[" + grant + "]";
		}
	}

	private record CachedClient(OAuthClientInfo client, long fetchedAt) {
		// Pure value.
	}

	private final ClientMetadataFetcher _fetcher;

	private final Map<String, CachedClient> _clientCache;

	/**
	 * Creates a {@link OAuthAuthorizationServer} from configuration.
	 */
	public OAuthAuthorizationServer(InstantiationContext context, Config config) {
		super(context, config);
		_fetcher = context.getInstance(config.getClientMetadataFetcher());
		int cacheSize = config.getClientMetadataCacheSize();
		_clientCache = new LinkedHashMap<>(16, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, CachedClient> eldest) {
				return size() > cacheSize;
			}
		};
	}

	/**
	 * The running {@link OAuthAuthorizationServer}, or <code>null</code> if it is not active.
	 */
	public static OAuthAuthorizationServer getInstance() {
		Module module = Module.INSTANCE;
		return module.isActive() ? module.getImplementationInstance() : null;
	}

	// ---------------------------------------------------------------------------------------------
	// Metadata
	// ---------------------------------------------------------------------------------------------

	/**
	 * The issuer identifier: the absolute URL of the application, including its context path.
	 */
	public static Issuer issuer(DiscoveryContext context) {
		return new Issuer(context.applicationUrl());
	}

	/**
	 * The authorization server metadata (RFC 8414) as the client reaching the given context sees
	 * it.
	 */
	public AuthorizationServerMetadata metadata(DiscoveryContext context) {
		AuthorizationServerMetadata result = new AuthorizationServerMetadata(issuer(context));
		result.setAuthorizationEndpointURI(URI.create(context.url(AUTHORIZATION_PATH)));
		result.setTokenEndpointURI(URI.create(context.url(TOKEN_PATH)));
		if (getConfig().getDynamicClientRegistration()) {
			result.setRegistrationEndpointURI(URI.create(context.url(REGISTRATION_PATH)));
		}
		result.setResponseTypes(List.of(ResponseType.CODE));
		result.setGrantTypes(GRANT_TYPES);
		result.setCodeChallengeMethods(List.of(CodeChallengeMethod.S256));
		result.setTokenEndpointAuthMethods(List.of(ClientAuthenticationMethod.NONE));
		result.setCustomParameter(CLIENT_ID_METADATA_DOCUMENT_SUPPORTED,
			Boolean.valueOf(getConfig().getClientIdMetadataDocuments()));
		return result;
	}

	/**
	 * Whether tokens for {@code resource} may be issued: an absolute URI without fragment of this
	 * application, that is, the application URL or below it.
	 */
	public static boolean isProtectedResource(URI resource, DiscoveryContext context) {
		if (resource == null || !resource.isAbsolute() || resource.getRawFragment() != null) {
			return false;
		}
		String normalized = normalizeResource(resource.toString());
		String application = normalizeResource(context.applicationUrl());
		return normalized.equals(application) || normalized.startsWith(application + '/');
	}

	/**
	 * A resource URI in the form in which two of them are compared: without a trailing slash.
	 */
	public static String normalizeResource(String resource) {
		String result = resource;
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	// ---------------------------------------------------------------------------------------------
	// Clients
	// ---------------------------------------------------------------------------------------------

	/**
	 * Whether {@code clientId} is the URL of a client ID metadata document rather than the
	 * identifier of a registered client.
	 */
	public static boolean isMetadataDocumentClientId(String clientId) {
		return clientId.regionMatches(true, 0, RedirectUris.HTTPS + "://", 0, RedirectUris.HTTPS.length() + 3);
	}

	/**
	 * The client with the given identifier.
	 *
	 * <p>
	 * A client ID that is an <code>https</code> URL names a client ID metadata document, which is
	 * fetched (or taken from the cache) and checked; any other client ID names a dynamically
	 * registered client.
	 * </p>
	 *
	 * @throws OAuthException
	 *         <code>invalid_client</code> if there is no such client or its metadata is not
	 *         acceptable.
	 */
	public OAuthClientInfo resolveClient(String clientId) throws OAuthException {
		if (clientId == null || clientId.isEmpty()) {
			throw new OAuthException(OAuth2Error.INVALID_REQUEST, I18NConstants.ERROR_NO_CLIENT_ID);
		}
		if (isMetadataDocumentClientId(clientId)) {
			if (!getConfig().getClientIdMetadataDocuments()) {
				throw new OAuthException(OAuth2Error.INVALID_CLIENT,
					I18NConstants.ERROR_CLIENT_METADATA_DOCUMENTS_DISABLED__CLIENT.fill(clientId));
			}
			return metadataDocumentClient(clientId);
		}
		OAuthClient registered = findRegisteredClient(clientId);
		if (registered == null) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT, I18NConstants.ERROR_UNKNOWN_CLIENT__CLIENT.fill(clientId));
		}
		ClientMetadata metadata;
		try {
			metadata = ClientMetadata.parse(JSONObjectUtils.parse(registered.getMetadata()));
		} catch (ParseException ex) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT,
				I18NConstants.ERROR_INVALID_CLIENT_METADATA__CLIENT_PROBLEM.fill(clientId, ex.getMessage()));
		}
		return new OAuthClientInfo(clientId, metadata.getName(), metadata.getRedirectionURIs());
	}

	private OAuthClientInfo metadataDocumentClient(String clientId) throws OAuthException {
		long now = System.currentTimeMillis();
		synchronized (_clientCache) {
			CachedClient cached = _clientCache.get(clientId);
			if (cached != null && now - cached.fetchedAt() < getConfig().getClientMetadataCacheLifetime()) {
				return cached.client();
			}
		}
		OAuthClientInfo client = fetchMetadataDocumentClient(clientId);
		synchronized (_clientCache) {
			_clientCache.put(clientId, new CachedClient(client, now));
		}
		return client;
	}

	private OAuthClientInfo fetchMetadataDocumentClient(String clientId) throws OAuthException {
		URI documentUrl;
		try {
			documentUrl = new URI(clientId);
		} catch (URISyntaxException ex) {
			throw invalidClientId(clientId);
		}
		String path = documentUrl.getRawPath();
		if (!RedirectUris.HTTPS.equalsIgnoreCase(documentUrl.getScheme()) || documentUrl.getHost() == null
			|| documentUrl.getRawUserInfo() != null || documentUrl.getRawFragment() != null
			|| path == null || path.isEmpty() || "/".equals(path)
			|| !documentUrl.normalize().toString().equals(clientId)) {
			throw invalidClientId(clientId);
		}

		String document = _fetcher.fetch(documentUrl);

		ClientInformation information;
		try {
			JSONObject json = JSONObjectUtils.parse(document);
			information = ClientInformation.parse(json);
		} catch (ParseException ex) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT,
				I18NConstants.ERROR_INVALID_CLIENT_METADATA__CLIENT_PROBLEM.fill(clientId, ex.getMessage()));
		}
		if (!clientId.equals(information.getID().getValue())) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT,
				I18NConstants.ERROR_CLIENT_ID_MISMATCH__URL_CLIENT.fill(clientId, information.getID().getValue()));
		}
		if (information.getSecret() != null) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT,
				I18NConstants.ERROR_CLIENT_METADATA_WITH_SECRET__CLIENT.fill(clientId));
		}
		ClientMetadata metadata = information.getMetadata();
		ResKey problem = checkPublicClient(metadata);
		if (problem != null) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT,
				I18NConstants.ERROR_INVALID_CLIENT_METADATA__CLIENT_PROBLEM.fill(clientId, problem));
		}
		return new OAuthClientInfo(clientId, metadata.getName(), metadata.getRedirectionURIs());
	}

	private static OAuthException invalidClientId(String clientId) {
		return new OAuthException(OAuth2Error.INVALID_CLIENT,
			I18NConstants.ERROR_INVALID_METADATA_DOCUMENT_URL__CLIENT.fill(clientId));
	}

	/**
	 * Why {@code metadata} does not describe a public client of this server.
	 *
	 * @return The reason, or <code>null</code> if it does.
	 */
	private static ResKey checkPublicClient(ClientMetadata metadata) {
		ClientAuthenticationMethod authMethod = metadata.getTokenEndpointAuthMethod();
		if (authMethod != null && !ClientAuthenticationMethod.NONE.equals(authMethod)) {
			return I18NConstants.ERROR_ONLY_PUBLIC_CLIENTS__METHOD.fill(authMethod.getValue());
		}
		Set<GrantType> grantTypes = metadata.getGrantTypes();
		if (grantTypes != null) {
			if (!grantTypes.contains(GrantType.AUTHORIZATION_CODE) || !GRANT_TYPES.containsAll(grantTypes)) {
				return I18NConstants.ERROR_UNSUPPORTED_GRANT_TYPES__TYPES.fill(grantTypes.toString());
			}
		}
		Set<ResponseType> responseTypes = metadata.getResponseTypes();
		if (responseTypes != null && !Set.of(ResponseType.CODE).containsAll(responseTypes)) {
			return I18NConstants.ERROR_UNSUPPORTED_RESPONSE_TYPES__TYPES.fill(responseTypes.toString());
		}
		return RedirectUris.checkRegistered(metadata.getRedirectionURIs());
	}

	private static OAuthClient findRegisteredClient(String clientId) {
		Iterator<KnowledgeItem> hits = kb().getObjectsByAttribute(TlOauthServerFactory.KO_NAME_O_AUTH_CLIENT,
			OAuthClient.CLIENT_ID_ATTR, clientId);
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	/**
	 * Registers a client (RFC 7591).
	 *
	 * <p>
	 * Only public clients are registered: <code>token_endpoint_auth_method</code> is
	 * <code>none</code> (and set to it if missing), the grant types are
	 * <code>authorization_code</code> and <code>refresh_token</code>, the response type is
	 * <code>code</code>, and every redirect URI satisfies {@link RedirectUris#checkRegistered(URI)}.
	 * </p>
	 *
	 * @return A {@link ClientInformationResponse} with the new client ID, or a
	 *         {@link ClientRegistrationErrorResponse}.
	 */
	public ClientRegistrationResponse register(ClientRegistrationRequest request) {
		ClientMetadata metadata = request.getClientMetadata();
		if (metadata.getTokenEndpointAuthMethod() == null) {
			metadata.setTokenEndpointAuthMethod(ClientAuthenticationMethod.NONE);
		}
		if (metadata.getGrantTypes() == null) {
			metadata.setGrantTypes(Set.copyOf(GRANT_TYPES));
		}
		if (metadata.getResponseTypes() == null) {
			metadata.setResponseTypes(Set.of(ResponseType.CODE));
		}
		ResKey redirectProblem = RedirectUris.checkRegistered(metadata.getRedirectionURIs());
		if (redirectProblem != null) {
			return registrationError(RegistrationError.INVALID_REDIRECT_URI, redirectProblem);
		}
		ResKey problem = checkPublicClient(metadata);
		if (problem != null) {
			return registrationError(RegistrationError.INVALID_CLIENT_METADATA, problem);
		}

		String clientId = SecretTokens.newToken(CLIENT_ID_PREFIX);
		Date now = new Date();
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_REGISTER_CLIENT)) {
			int max = getConfig().getMaxRegisteredClients();
			if (kb().getAllKnowledgeObjects(TlOauthServerFactory.KO_NAME_O_AUTH_CLIENT).size() >= max) {
				Logger.warn("Refused a client registration: " + max + " clients are registered already.",
					OAuthAuthorizationServer.class);
				return registrationError(
					OAuth2Error.TEMPORARILY_UNAVAILABLE.setHTTPStatusCode(HTTPResponse.SC_SERVICE_UNAVAILABLE),
					I18NConstants.ERROR_TOO_MANY_CLIENTS);
			}
			OAuthClient client = TlOauthServerFactory.getInstance().createOAuthClient();
			client.setClientId(clientId);
			client.setClientName(metadata.getName());
			client.setMetadata(metadata.toJSONObject().toJSONString());
			client.setRegisteredAt(now);
			tx.commit();
		}
		Logger.info("Registered OAuth client '" + clientId + "' (" + metadata.getName() + ").",
			OAuthAuthorizationServer.class);
		return new ClientInformationResponse(new ClientInformation(new ClientID(clientId), now, metadata, null), true);
	}

	private static ClientRegistrationErrorResponse registrationError(ErrorObject error, ResKey reason) {
		return new ClientRegistrationErrorResponse(new OAuthException(error, reason).toErrorObject());
	}

	// ---------------------------------------------------------------------------------------------
	// Authorization
	// ---------------------------------------------------------------------------------------------

	/**
	 * Checks an authorization request before the person is asked.
	 *
	 * <p>
	 * Required: a known client, a <code>redirect_uri</code> matching one the client registered
	 * ({@link RedirectUris#matches(java.util.Collection, URI)}), <code>response_type=code</code>, a
	 * PKCE challenge with method <code>S256</code>, and exactly one <code>resource</code> of this
	 * application ({@link #isProtectedResource(URI, DiscoveryContext)}).
	 * </p>
	 *
	 * @param request
	 *        The parsed request.
	 * @param context
	 *        The application as the client reaches it.
	 * @return The accepted request, for {@link #issueCode(ValidatedAuthorization, Person)}.
	 * @throws OAuthException
	 *         If the request is refused; {@link OAuthException#isRedirectable()} tells whether
	 *         the error may be sent to the redirect URI.
	 */
	public ValidatedAuthorization validateAuthorizationRequest(AuthorizationRequest request,
			DiscoveryContext context) throws OAuthException {
		ClientID clientId = request.getClientID();
		OAuthClientInfo client = resolveClient(clientId == null ? null : clientId.getValue());

		URI redirectUri = request.getRedirectionURI();
		if (redirectUri == null) {
			throw new OAuthException(OAuth2Error.INVALID_REQUEST, I18NConstants.ERROR_NO_REDIRECT_URI);
		}
		if (!RedirectUris.matches(client.redirectUris(), redirectUri)) {
			throw new OAuthException(OAuth2Error.INVALID_REQUEST,
				I18NConstants.ERROR_REDIRECT_URI_NOT_REGISTERED__URI_CLIENT.fill(redirectUri.toString(),
					client.clientId()));
		}

		// From here on the client and its redirect URI are trusted, so an error goes back there.
		if (!ResponseType.CODE.equals(request.getResponseType())) {
			throw new OAuthException(OAuth2Error.UNSUPPORTED_RESPONSE_TYPE,
				I18NConstants.ERROR_UNSUPPORTED_RESPONSE_TYPE__TYPE.fill(String.valueOf(request.getResponseType())),
				true);
		}
		CodeChallenge challenge = request.getCodeChallenge();
		if (challenge == null) {
			throw new OAuthException(OAuth2Error.INVALID_REQUEST, I18NConstants.ERROR_NO_CODE_CHALLENGE, true);
		}
		if (!CodeChallengeMethod.S256.equals(request.getCodeChallengeMethod())) {
			throw new OAuthException(OAuth2Error.INVALID_REQUEST,
				I18NConstants.ERROR_UNSUPPORTED_CODE_CHALLENGE_METHOD__METHOD
					.fill(String.valueOf(request.getCodeChallengeMethod())),
				true);
		}
		List<URI> resources = request.getResources();
		if (resources == null || resources.size() != 1) {
			throw new OAuthException(OAuth2Error.INVALID_TARGET, I18NConstants.ERROR_NOT_ONE_RESOURCE, true);
		}
		URI resource = resources.get(0);
		if (!isProtectedResource(resource, context)) {
			throw new OAuthException(OAuth2Error.INVALID_TARGET,
				I18NConstants.ERROR_UNKNOWN_RESOURCE__RESOURCE.fill(resource.toString()), true);
		}
		return new ValidatedAuthorization(client, redirectUri, challenge, resource, request.getScope(),
			request.getState());
	}

	/**
	 * Issues the authorization code for an accepted request, after {@code owner} agreed to it.
	 *
	 * <p>
	 * Commits in a transaction of its own, or as part of the caller's if one is open.
	 * </p>
	 *
	 * @return The code to send to the {@link ValidatedAuthorization#redirectUri()}. It exists only
	 *         here; what is stored is its hash.
	 */
	public String issueCode(ValidatedAuthorization authorization, Person owner) {
		String code = SecretTokens.newToken(AUTHORIZATION_CODE_PREFIX);
		Instant now = Instant.now();
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_ISSUE_CODE)) {
			OAuthAuthorizationCode stored = TlOauthServerFactory.getInstance().createOAuthAuthorizationCode();
			stored.setCodeHash(SecretTokens.hash(code));
			stored.setOwner(owner);
			stored.setClientId(authorization.client().clientId());
			stored.setClientName(authorization.client().clientName());
			stored.setRedirectUri(authorization.redirectUri().toString());
			stored.setCodeChallenge(authorization.codeChallenge().getValue());
			stored.setResource(authorization.resource().toString());
			stored.setScope(scopeValue(authorization.scope()));
			stored.setExpiresAt(Date.from(now.plusMillis(getConfig().getAuthorizationCodeLifetime())));
			tx.commit();
		}
		return code;
	}

	// ---------------------------------------------------------------------------------------------
	// Token endpoint
	// ---------------------------------------------------------------------------------------------

	/**
	 * Answers a request to the token endpoint.
	 *
	 * @return An {@link AccessTokenResponse} or a {@link TokenErrorResponse}.
	 */
	public TokenResponse token(TokenRequest request) {
		try {
			IssuedTokens issued;
			Scope scope;
			AuthorizationGrant grant = request.getAuthorizationGrant();
			ClientID clientId = checkPublicClient(request);
			if (grant instanceof AuthorizationCodeGrant codeGrant) {
				issued = exchangeCode(codeGrant, clientId.getValue(), request.getResources());
				scope = scope(issued.grant().getScope());
			} else if (grant instanceof RefreshTokenGrant refreshGrant) {
				issued = refresh(refreshGrant, clientId.getValue(), request.getResources(), request.getScope());
				scope = request.getScope() != null ? request.getScope() : scope(issued.grant().getScope());
			} else {
				throw new OAuthException(OAuth2Error.UNSUPPORTED_GRANT_TYPE,
					I18NConstants.ERROR_UNSUPPORTED_GRANT_TYPE__TYPE.fill(grant.getType().getValue()));
			}
			long lifetimeSeconds = getConfig().getAccessTokenLifetime() / 1000;
			return new AccessTokenResponse(new Tokens(
				new BearerAccessToken(issued.accessToken(), lifetimeSeconds, scope),
				new RefreshToken(issued.refreshToken())));
		} catch (OAuthException ex) {
			Logger.info("Refused a token request: " + ex.description(), OAuthAuthorizationServer.class);
			return new TokenErrorResponse(ex.toErrorObject());
		} catch (KnowledgeBaseException | KnowledgeBaseRuntimeException ex) {
			// Another exchange of the same code or refresh token committed first.
			Logger.info("Refused a token request whose grant was exchanged concurrently: " + ex.getMessage(),
				OAuthAuthorizationServer.class);
			return new TokenErrorResponse(
				new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_CONCURRENT_EXCHANGE).toErrorObject());
		}
	}

	/**
	 * Records the use of a dynamically registered client, at most once per
	 * {@link #LAST_USED_GRANULARITY}, within the caller's transaction.
	 */
	private static void touchRegisteredClient(String clientId, Instant now) {
		if (isMetadataDocumentClientId(clientId)) {
			return;
		}
		OAuthClient client = findRegisteredClient(clientId);
		if (client == null) {
			return;
		}
		Date lastUsed = client.getLastUsed();
		if (lastUsed == null || Duration.between(lastUsed.toInstant(), now).compareTo(LAST_USED_GRANULARITY) >= 0) {
			client.setLastUsed(Date.from(now));
		}
	}

	private static ClientID checkPublicClient(TokenRequest request) throws OAuthException {
		if (request.getClientAuthentication() != null) {
			throw new OAuthException(OAuth2Error.INVALID_CLIENT, I18NConstants.ERROR_CLIENT_AUTHENTICATION_NOT_SUPPORTED);
		}
		ClientID clientId = request.getClientID();
		if (clientId == null) {
			throw new OAuthException(OAuth2Error.INVALID_REQUEST, I18NConstants.ERROR_NO_CLIENT_ID);
		}
		return clientId;
	}

	private IssuedTokens exchangeCode(AuthorizationCodeGrant codeGrant, String clientId, List<URI> resources)
			throws OAuthException {
		String presented = codeGrant.getAuthorizationCode().getValue();
		if (!SecretTokens.hasFormat(AUTHORIZATION_CODE_PREFIX, presented)) {
			throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_UNKNOWN_CODE);
		}
		Instant now = Instant.now();
		String codeHash = SecretTokens.hash(presented);
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_EXCHANGE_CODE)) {
			OAuthAuthorizationCode code = findCode(codeHash);
			if (code == null) {
				OAuthGrant redeemed = findGrantByCode(codeHash);
				if (redeemed == null) {
					throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_UNKNOWN_CODE);
				}
				// A code presented a second time was intercepted, or the first exchange was: the
				// tokens issued for it are not to be trusted (RFC 6749, section 4.1.2).
				Logger.warn("Authorization code of client '" + redeemed.getClientId() + "' presented again, "
					+ "revoking the authorization it created.", OAuthAuthorizationServer.class);
				redeemed.tDelete();
				tx.commit();
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_CODE_REUSED);
			}
			if (isExpired(code.getExpiresAt(), now)) {
				code.tDelete();
				tx.commit();
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_CODE_EXPIRED);
			}
			if (!clientId.equals(code.getClientId())) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_CODE_OF_OTHER_CLIENT);
			}
			URI redirectUri = codeGrant.getRedirectionURI();
			if (redirectUri != null && !redirectUri.toString().equals(code.getRedirectUri())) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_REDIRECT_URI_MISMATCH);
			}
			CodeVerifier verifier = codeGrant.getCodeVerifier();
			if (verifier == null) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_NO_CODE_VERIFIER);
			}
			String computed = CodeChallenge.compute(CodeChallengeMethod.S256, verifier).getValue();
			if (!SecretTokens.constantTimeEquals(computed, code.getCodeChallenge())) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_CODE_VERIFIER_MISMATCH);
			}
			checkResource(resources, code.getResource());

			OAuthGrant grant = TlOauthServerFactory.getInstance().createOAuthGrant();
			grant.setOwner(code.getOwner());
			grant.setClientId(code.getClientId());
			grant.setClientName(code.getClientName());
			grant.setResource(code.getResource());
			grant.setScope(code.getScope());
			grant.setCreatedAt(Date.from(now));
			grant.setLastUsed(Date.from(now));
			// The code hash is unique among the grants: of two exchanges of the same code running at
			// the same time, the database refuses the commit of all but the first, see
			// token(TokenRequest). A code yields at most one token pair.
			grant.setCodeHash(codeHash);
			code.tDelete();
			touchRegisteredClient(grant.getClientId(), now);
			IssuedTokens result = issueTokens(grant, now, null);
			tx.commit();
			return result;
		}
	}

	private IssuedTokens refresh(RefreshTokenGrant refreshGrant, String clientId, List<URI> resources,
			Scope requestedScope) throws OAuthException {
		String presented = refreshGrant.getRefreshToken().getValue();
		if (!SecretTokens.hasFormat(REFRESH_TOKEN_PREFIX, presented)) {
			throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_UNKNOWN_REFRESH_TOKEN);
		}
		Instant now = Instant.now();
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_REFRESH)) {
			OAuthToken token = findToken(SecretTokens.hash(presented));
			if (token == null || !token.getRefresh()) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_UNKNOWN_REFRESH_TOKEN);
			}
			OAuthGrant grant = token.getGrant();
			if (token.getRetired()) {
				// Either the client or an attacker holds a refresh token that was already exchanged;
				// which one cannot be told, so neither keeps access.
				Logger.warn("Exchanged refresh token of client '" + grant.getClientId() + "' presented again, "
					+ "revoking the authorization.", OAuthAuthorizationServer.class);
				grant.tDelete();
				tx.commit();
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_REFRESH_TOKEN_REUSED);
			}
			if (isExpired(token.getExpiresAt(), now)) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_REFRESH_TOKEN_EXPIRED);
			}
			if (!clientId.equals(grant.getClientId())) {
				throw new OAuthException(OAuth2Error.INVALID_GRANT, I18NConstants.ERROR_REFRESH_TOKEN_OF_OTHER_CLIENT);
			}
			checkResource(resources, grant.getResource());
			if (requestedScope != null) {
				Scope granted = scope(grant.getScope());
				if (granted == null ? !requestedScope.isEmpty() : !granted.containsAll(requestedScope)) {
					throw new OAuthException(OAuth2Error.INVALID_SCOPE,
						I18NConstants.ERROR_SCOPE_EXCEEDS_GRANT__SCOPE.fill(requestedScope.toString()));
				}
			}

			token.setRetired(true);
			grant.setLastUsed(Date.from(now));
			touchRegisteredClient(grant.getClientId(), now);
			// The new refresh token names the old one as its predecessor, which is unique: of two
			// exchanges of one refresh token at the same time, the database refuses the commit of all
			// but the first.
			IssuedTokens result = issueTokens(grant, now, token.getTokenHash());
			tx.commit();
			return result;
		}
	}

	private static void checkResource(List<URI> resources, String granted) throws OAuthException {
		if (resources == null || resources.isEmpty()) {
			return;
		}
		if (resources.size() != 1
			|| !normalizeResource(resources.get(0).toString()).equals(normalizeResource(granted))) {
			throw new OAuthException(OAuth2Error.INVALID_TARGET,
				I18NConstants.ERROR_RESOURCE_MISMATCH__RESOURCES.fill(resources.toString()));
		}
	}

	/**
	 * Issues an access and a refresh token for {@code grant}.
	 *
	 * @param replacedRefreshHash
	 *        The hash of the refresh token the new refresh token replaces, or <code>null</code> for
	 *        the first tokens of a grant.
	 */
	private IssuedTokens issueTokens(OAuthGrant grant, Instant now, String replacedRefreshHash) {
		String access = SecretTokens.newToken(ACCESS_TOKEN_PREFIX);
		String refresh = SecretTokens.newToken(REFRESH_TOKEN_PREFIX);
		createToken(grant, access, false, null, now.plusMillis(getConfig().getAccessTokenLifetime()));
		createToken(grant, refresh, true, replacedRefreshHash,
			now.plusMillis(getConfig().getRefreshTokenLifetime()));
		return new IssuedTokens(grant, access, refresh);
	}

	private static void createToken(OAuthGrant grant, String plain, boolean refresh, String predecessorHash,
			Instant expiresAt) {
		OAuthToken token = TlOauthServerFactory.getInstance().createOAuthToken();
		String hash = SecretTokens.hash(plain);
		token.setGrant(grant);
		token.setTokenHash(hash);
		// A token without predecessor gets a random value that matches no token, so that the unique
		// column holds no empty values, which some databases count as equal.
		token.setPredecessorHash(
			predecessorHash == null ? SecretTokens.hash(SecretTokens.newToken(NO_PREDECESSOR_PREFIX)) : predecessorHash);
		token.setRefresh(refresh);
		token.setRetired(false);
		token.setExpiresAt(Date.from(expiresAt));
	}

	// ---------------------------------------------------------------------------------------------
	// Resource access
	// ---------------------------------------------------------------------------------------------

	/**
	 * The grant whose access token is presented at {@code resource}.
	 *
	 * <p>
	 * Records the use on the grant at most once per {@link #LAST_USED_GRANULARITY}. Must run in an
	 * interaction that may read every grant and commit, e.g. a system interaction.
	 * </p>
	 *
	 * @param presented
	 *        The bearer token of the request.
	 * @param resource
	 *        The resource URI of the endpoint the request is for.
	 * @throws OAuthException
	 *         {@link BearerTokenError#INVALID_TOKEN} if the token is unknown, revoked, expired or
	 *         issued for another resource.
	 */
	public OAuthGrant authenticate(String presented, String resource) throws OAuthException {
		if (!SecretTokens.hasFormat(ACCESS_TOKEN_PREFIX, presented)) {
			throw new OAuthException(BearerTokenError.INVALID_TOKEN, I18NConstants.ERROR_NOT_AN_ACCESS_TOKEN);
		}
		OAuthToken token = findToken(SecretTokens.hash(presented));
		if (token == null || token.getRefresh()) {
			throw new OAuthException(BearerTokenError.INVALID_TOKEN, I18NConstants.ERROR_UNKNOWN_ACCESS_TOKEN);
		}
		Instant now = Instant.now();
		if (isExpired(token.getExpiresAt(), now)) {
			throw new OAuthException(BearerTokenError.INVALID_TOKEN, I18NConstants.ERROR_ACCESS_TOKEN_EXPIRED);
		}
		OAuthGrant grant = token.getGrant();
		if (!normalizeResource(grant.getResource()).equals(normalizeResource(resource))) {
			throw new OAuthException(BearerTokenError.INVALID_TOKEN,
				I18NConstants.ERROR_ACCESS_TOKEN_FOR_OTHER_RESOURCE__RESOURCE.fill(resource));
		}
		try {
			touch(grant, now);
		} catch (RuntimeException ex) {
			// A last-used stamp that could not be written - two requests racing on one grant, say -
			// is no reason to refuse a valid token.
			Logger.warn("Could not record the use of the OAuth authorization of client '" + grant.getClientId()
				+ "'.", ex, OAuthAuthorizationServer.class);
		}
		return grant;
	}

	private static void touch(OAuthGrant grant, Instant now) {
		Date lastUsed = grant.getLastUsed();
		if (lastUsed != null && Duration.between(lastUsed.toInstant(), now).compareTo(LAST_USED_GRANULARITY) < 0) {
			return;
		}
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_RECORD_USE)) {
			grant.setLastUsed(Date.from(now));
			tx.commit();
		}
	}

	/**
	 * Revokes a grant: its access and refresh tokens are refused from now on.
	 *
	 * <p>
	 * Commits in a transaction of its own, or as part of the caller's if one is open.
	 * </p>
	 */
	public void revoke(OAuthGrant grant) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_REVOKE)) {
			grant.tDelete();
			tx.commit();
		}
	}

	// ---------------------------------------------------------------------------------------------
	// Clean-up
	// ---------------------------------------------------------------------------------------------

	/**
	 * What one {@link OAuthAuthorizationServer#cleanUp(Instant) clean-up} removed.
	 *
	 * @param codes
	 *        Expired authorization codes.
	 * @param tokens
	 *        Expired access and refresh tokens, exchanged refresh tokens included.
	 * @param grants
	 *        Grants without any token that is still valid.
	 * @param clients
	 *        Dynamically registered clients without a grant, see
	 *        {@link Config#getUnusedClientLifetime()} and {@link Config#getAbandonedClientLifetime()}.
	 */
	public record CleanUpResult(int codes, int tokens, int grants, int clients) {
		// Pure value.
	}

	/**
	 * Removes what can no longer be used at {@code now}.
	 *
	 * <ul>
	 * <li>Authorization codes whose lifetime ended.</li>
	 * <li>Access and refresh tokens whose lifetime ended. An exchanged refresh token is kept until
	 * then, so that presenting it again is recognized and revokes its grant.</li>
	 * <li>Grants without a token that is still valid: neither the client nor anybody else can use
	 * them any more.</li>
	 * <li>Dynamically registered clients without a grant: a client that was never used after
	 * {@link Config#getUnusedClientLifetime()} from its registration, any other after
	 * {@link Config#getAbandonedClientLifetime()} from its last use.</li>
	 * </ul>
	 *
	 * <p>
	 * Run periodically by the {@link OAuthCleanUpTask}. Must run in an interaction that may read and
	 * delete every object of the authorization server, e.g. a system interaction.
	 * </p>
	 */
	public CleanUpResult cleanUp(Instant now) {
		int codes = 0;
		int tokens = 0;
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_CLEAN_UP)) {
			for (OAuthAuthorizationCode code : this.<OAuthAuthorizationCode> all(
				TlOauthServerFactory.KO_NAME_O_AUTH_AUTHORIZATION_CODE)) {
				if (isExpired(code.getExpiresAt(), now)) {
					code.tDelete();
					codes++;
				}
			}
			for (OAuthToken token : this.<OAuthToken> all(TlOauthServerFactory.KO_NAME_O_AUTH_TOKEN)) {
				if (isExpired(token.getExpiresAt(), now)) {
					token.tDelete();
					tokens++;
				}
			}
			tx.commit();
		}

		int grants = 0;
		Set<String> clientsWithGrant = new HashSet<>();
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_CLEAN_UP)) {
			Set<OAuthGrant> grantsWithToken = new HashSet<>();
			for (OAuthToken token : this.<OAuthToken> all(TlOauthServerFactory.KO_NAME_O_AUTH_TOKEN)) {
				grantsWithToken.add(token.getGrant());
			}
			for (OAuthGrant grant : this.<OAuthGrant> all(TlOauthServerFactory.KO_NAME_O_AUTH_GRANT)) {
				if (grantsWithToken.contains(grant)) {
					clientsWithGrant.add(grant.getClientId());
				} else {
					grant.tDelete();
					grants++;
				}
			}
			tx.commit();
		}

		int clients = 0;
		Config config = getConfig();
		try (Transaction tx = kb().beginTransaction(I18NConstants.TX_CLEAN_UP)) {
			for (OAuthClient client : this.<OAuthClient> all(TlOauthServerFactory.KO_NAME_O_AUTH_CLIENT)) {
				if (clientsWithGrant.contains(client.getClientId())) {
					continue;
				}
				Date lastUsed = client.getLastUsed();
				Instant removeAt = lastUsed == null
					? client.getRegisteredAt().toInstant().plusMillis(config.getUnusedClientLifetime())
					: lastUsed.toInstant().plusMillis(config.getAbandonedClientLifetime());
				if (!now.isBefore(removeAt)) {
					client.tDelete();
					clients++;
				}
			}
			tx.commit();
		}
		return new CleanUpResult(codes, tokens, grants, clients);
	}

	private <T> List<T> all(String table) {
		List<T> result = new ArrayList<>();
		for (KnowledgeObject item : kb().getAllKnowledgeObjects(table)) {
			result.add(item.getWrapper());
		}
		return result;
	}

	// ---------------------------------------------------------------------------------------------
	// Utilities
	// ---------------------------------------------------------------------------------------------

	private static OAuthToken findToken(String hash) {
		Iterator<KnowledgeItem> hits = kb().getObjectsByAttribute(TlOauthServerFactory.KO_NAME_O_AUTH_TOKEN,
			OAuthToken.TOKEN_HASH_ATTR, hash);
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	private static OAuthGrant findGrantByCode(String codeHash) {
		Iterator<KnowledgeItem> hits = kb().getObjectsByAttribute(TlOauthServerFactory.KO_NAME_O_AUTH_GRANT,
			OAuthGrant.CODE_HASH_ATTR, codeHash);
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	private static OAuthAuthorizationCode findCode(String hash) {
		Iterator<KnowledgeItem> hits = kb().getObjectsByAttribute(
			TlOauthServerFactory.KO_NAME_O_AUTH_AUTHORIZATION_CODE, OAuthAuthorizationCode.CODE_HASH_ATTR, hash);
		return hits.hasNext() ? hits.next().getWrapper() : null;
	}

	private static boolean isExpired(Date expiresAt, Instant now) {
		return expiresAt == null || !now.isBefore(expiresAt.toInstant());
	}

	private static String scopeValue(Scope scope) {
		return scope == null || scope.isEmpty() ? null : scope.toString();
	}

	private static Scope scope(String value) {
		return value == null || value.isBlank() ? null : Scope.parse(value);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Module for instantiation of the {@link OAuthAuthorizationServer}.
	 */
	public static class Module extends TypedRuntimeModule<OAuthAuthorizationServer> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<OAuthAuthorizationServer> getImplementation() {
			return OAuthAuthorizationServer.class;
		}
	}

}
