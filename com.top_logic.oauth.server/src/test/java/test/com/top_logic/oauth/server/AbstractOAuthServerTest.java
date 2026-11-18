/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.oauth.server;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.AuthorizationRequest;
import com.nimbusds.oauth2.sdk.OAuth2Error;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.as.AuthorizationServerMetadata;
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod;
import com.nimbusds.oauth2.sdk.client.ClientInformationResponse;
import com.nimbusds.oauth2.sdk.client.ClientMetadata;
import com.nimbusds.oauth2.sdk.client.ClientRegistrationRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;

import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.accesstoken.SecretTokens;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.oauth.server.ClientMetadataFetcher;
import com.top_logic.oauth.server.I18NConstants;
import com.top_logic.oauth.server.OAuthAuthorizationServer;
import com.top_logic.oauth.server.OAuthException;
import com.top_logic.oauth.server.OAuthServerServlet;
import com.top_logic.oauth.server.ProtectedResources;
import com.top_logic.oauth.server.ValidatedAuthorization;
import com.top_logic.oauth.server.model.OAuthAuthorizationCode;
import com.top_logic.oauth.server.model.OAuthGrant;
import com.top_logic.oauth.server.model.OAuthPendingAuthorization;
import com.top_logic.oauth.server.model.OAuthToken;
import com.top_logic.oauth.server.model.TlOauthServerFactory;
import com.top_logic.util.model.ModelService;

/**
 * Base class of the tests of the {@link OAuthAuthorizationServer}: drives the
 * {@link OAuthServerServlet} through a servlet runner, against a knowledge base with the model of
 * the authorization server.
 */
public abstract class AbstractOAuthServerTest extends BasicTestCase {

	/** Media type of a form-encoded request body. */
	protected static final String FORM_MEDIA_TYPE = "application/x-www-form-urlencoded";

	/** Media type of a JSON request body. */
	protected static final String JSON_MEDIA_TYPE = "application/json";

	/** The host the servlet runner answers for. */
	private static final String BASE_URL = "http://localhost";

	/**
	 * {@link ClientMetadataFetcher} answering from {@link #DOCUMENTS} instead of the network.
	 */
	public static class StaticFetcher extends AbstractConfiguredInstance<StaticFetcher.Config>
			implements ClientMetadataFetcher {

		/** The documents by URL. */
		public static final Map<String, String> DOCUMENTS = new ConcurrentHashMap<>();

		/**
		 * Configuration of {@link StaticFetcher}.
		 */
		public interface Config extends PolymorphicConfiguration<StaticFetcher> {
			// No properties.
		}

		/**
		 * Creates a {@link StaticFetcher} from configuration.
		 */
		@CalledByReflection
		public StaticFetcher(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public String fetch(URI documentUrl) throws OAuthException {
			String document = DOCUMENTS.get(documentUrl.toString());
			if (document == null) {
				throw new OAuthException(OAuth2Error.INVALID_CLIENT,
					I18NConstants.ERROR_CLIENT_METADATA_STATUS__URL_STATUS.fill(documentUrl.toString(),
						Integer.valueOf(404)));
			}
			return document;
		}
	}

	/** The redirect URI of the test clients, on the loopback interface. */
	protected static final URI REDIRECT_URI = URI.create("http://127.0.0.1:33418/callback");

	/** The path of the protected resource the tokens are for. */
	protected static final String RESOURCE_PATH = "/mcp";

	private ServletUnitClient _client;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ServletRunner runner = new ServletRunner();
		for (String path : new String[] { OAuthAuthorizationServer.METADATA_PATH,
			OAuthAuthorizationServer.OPENID_CONFIGURATION_PATH, OAuthAuthorizationServer.AUTHORIZATION_PATH,
			OAuthAuthorizationServer.TOKEN_PATH, OAuthAuthorizationServer.REGISTRATION_PATH }) {
			runner.registerServlet(path, OAuthServerServlet.class.getName());
		}
		runner.registerServlet(ProtectedResources.METADATA_PATH + "/*", OAuthServerServlet.class.getName());
		_client = runner.newClient();
		_client.setExceptionsThrownOnErrorStatus(false);
		StaticFetcher.DOCUMENTS.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		_client = null;
		StaticFetcher.DOCUMENTS.clear();
		super.tearDown();
	}

	/**
	 * The running authorization server.
	 */
	protected static OAuthAuthorizationServer server() {
		OAuthAuthorizationServer result = OAuthAuthorizationServer.getInstance();
		assertNotNull("The authorization server must run.", result);
		return result;
	}

	/**
	 * Sends a GET request to the given context-relative path.
	 */
	protected WebResponse get(String path) throws Exception {
		return _client.getResponse(new GetMethodWebRequest(BASE_URL + path));
	}

	/**
	 * Sends a POST request with the given body to the given context-relative path.
	 */
	protected WebResponse post(String path, String body, String contentType) throws Exception {
		WebRequest request = new PostMethodWebRequest(BASE_URL + path,
			new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), contentType);
		return _client.getResponse(request);
	}

	/**
	 * The metadata the server publishes.
	 */
	protected AuthorizationServerMetadata metadata() throws Exception {
		WebResponse response = get(OAuthAuthorizationServer.METADATA_PATH);
		assertEquals(200, response.getResponseCode());
		return AuthorizationServerMetadata.parse(response.getText());
	}

	/**
	 * The application as the servlet runner's requests reach it.
	 */
	protected DiscoveryContext context() throws Exception {
		return new DiscoveryContext(metadata().getIssuer().getValue());
	}

	/**
	 * Sends a token request.
	 */
	protected TokenResponse token(AuthorizationGrant grant, String clientId, URI resource) throws Exception {
		TokenRequest request = new TokenRequest.Builder(tokenEndpoint(), new ClientID(clientId), grant)
			.resources(resource == null ? new URI[0] : new URI[] { resource })
			.build();
		HTTPRequest httpRequest = request.toHTTPRequest();
		WebResponse response = post(OAuthAuthorizationServer.TOKEN_PATH, httpRequest.getBody(), FORM_MEDIA_TYPE);
		return TokenResponse.parse(JSONObjectUtils.parse(response.getText()));
	}

	/**
	 * Exchanges a code with the given verifier and redirect URI.
	 */
	protected TokenResponse exchange(String code, String clientId, URI redirectUri, CodeVerifier verifier,
			URI resource) throws Exception {
		return token(new AuthorizationCodeGrant(new AuthorizationCode(code), redirectUri, verifier), clientId,
			resource);
	}

	/**
	 * The token endpoint URL.
	 */
	protected URI tokenEndpoint() {
		return URI.create(BASE_URL + OAuthAuthorizationServer.TOKEN_PATH);
	}

	/**
	 * The resource URI of a context-relative path.
	 */
	protected URI resource(String path) throws Exception {
		return URI.create(context().url(path));
	}

	/**
	 * Lets the root account authorize the given client for {@code resource}.
	 *
	 * @return The authorization code.
	 */
	protected String authorize(String clientId, URI redirectUri, CodeVerifier verifier, URI resource)
			throws Exception {
		AuthorizationRequest request = new AuthorizationRequest.Builder(ResponseType.CODE, new ClientID(clientId))
			.redirectionURI(redirectUri)
			.codeChallenge(verifier, CodeChallengeMethod.S256)
			.resource(resource)
			.build();
		ValidatedAuthorization authorization = server().validateAuthorizationRequest(request, context());
		return server().issueCode(authorization, root());
	}

	/**
	 * The account that authorizes clients in the tests.
	 */
	protected static Person root() {
		return PersonManager.getManager().getRoot();
	}

	/**
	 * Moves the expiry of the stored access or refresh token into the past.
	 */
	protected static void expireToken(String plain) {
		OAuthToken token = find(TlOauthServerFactory.KO_NAME_O_AUTH_TOKEN, OAuthToken.TOKEN_HASH_ATTR, plain);
		try (Transaction tx = kb().beginTransaction()) {
			token.setExpiresAt(past());
			tx.commit();
		}
	}

	/**
	 * Moves the expiry of the stored authorization code into the past.
	 */
	protected static void expireCode(String plain) {
		OAuthAuthorizationCode code = find(TlOauthServerFactory.KO_NAME_O_AUTH_AUTHORIZATION_CODE,
			OAuthAuthorizationCode.CODE_HASH_ATTR, plain);
		try (Transaction tx = kb().beginTransaction()) {
			code.setExpiresAt(past());
			tx.commit();
		}
	}

	/**
	 * Moves the expiry of the stored pending authorization into the past.
	 */
	protected static void expirePending(String plain) {
		OAuthPendingAuthorization pending = find(TlOauthServerFactory.KO_NAME_O_AUTH_PENDING_AUTHORIZATION,
			OAuthPendingAuthorization.ID_HASH_ATTR, plain);
		try (Transaction tx = kb().beginTransaction()) {
			pending.setExpiresAt(past());
			tx.commit();
		}
	}

	/**
	 * The grant of the stored access or refresh token.
	 */
	protected static OAuthGrant grantOf(String plainToken) {
		OAuthToken token = find(TlOauthServerFactory.KO_NAME_O_AUTH_TOKEN, OAuthToken.TOKEN_HASH_ATTR, plainToken);
		return token.getGrant();
	}

	private static <T> T find(String table, String hashAttribute, String plain) {
		Iterator<KnowledgeItem> hits = kb().getObjectsByAttribute(table, hashAttribute, SecretTokens.hash(plain));
		assertTrue("No stored object for the given secret.", hits.hasNext());
		return hits.next().getWrapper();
	}

	private static Date past() {
		return new Date(System.currentTimeMillis() - 1000);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Signs in the root account at {@link #RESOURCE_PATH} with the given client.
	 */
	protected Tokens signIn(String clientId) throws Exception {
		return signIn(clientId, RESOURCE_PATH);
	}

	/**
	 * Signs in the root account at the given resource path with the given client.
	 */
	protected Tokens signIn(String clientId, String resourcePath) throws Exception {
		URI resource = resource(resourcePath);
		CodeVerifier verifier = new CodeVerifier();
		String code = authorize(clientId, REDIRECT_URI, verifier, resource);
		TokenResponse response = exchange(code, clientId, REDIRECT_URI, verifier, resource);
		assertTrue(String.valueOf(response), response.indicatesSuccess());
		AccessTokenResponse success = response.toSuccessResponse();
		return success.getTokens();
	}

	/**
	 * Registers a public client with the given redirect URIs.
	 *
	 * @return The client ID.
	 */
	protected String register(URI... redirectUris) throws Exception {
		WebResponse response = postRegistration(redirectUris);
		assertEquals(response.getText(), 201, response.getResponseCode());
		ClientInformationResponse information = ClientInformationResponse.parse(httpResponse(response));
		return information.getClientInformation().getID().getValue();
	}

	protected WebResponse postRegistration(URI... redirectUris) throws Exception {
		ClientMetadata metadata = clientMetadata(redirectUris);
		ClientRegistrationRequest request =
			new ClientRegistrationRequest(URI.create("http://localhost" + OAuthAuthorizationServer.REGISTRATION_PATH),
				metadata, null);
		return post(OAuthAuthorizationServer.REGISTRATION_PATH, request.toHTTPRequest().getBody(), JSON_MEDIA_TYPE);
	}

	protected static ClientMetadata clientMetadata(URI... redirectUris) {
		ClientMetadata metadata = new ClientMetadata();
		metadata.setName("Test client");
		metadata.setRedirectionURIs(Set.of(redirectUris));
		metadata.setTokenEndpointAuthMethod(ClientAuthenticationMethod.NONE);
		return metadata;
	}

	protected static HTTPResponse httpResponse(WebResponse response) throws Exception {
		HTTPResponse result = new HTTPResponse(response.getResponseCode());
		result.setContentType(response.getContentType());
		result.setBody(response.getText());
		return result;
	}

	/**
	 * The suite running the given test class against a knowledge base, with the configuration file
	 * of the class.
	 */
	protected static Test suite(Class<? extends AbstractOAuthServerTest> testClass) {
		Test test = KBSetup.getSingleKBTest(testClass,
			ServiceTestSetup.createStarterFactoryForModules(TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ModelService.Module.INSTANCE, PersonManager.Module.INSTANCE,
				OAuthAuthorizationServer.Module.INSTANCE));
		test = TestUtils.doNotMerge(
			new CustomPropertiesSetup(test, CustomPropertiesDecorator.createFileName(testClass), true));
		return TLTestSetup.createTLTestSetup(test);
	}

}
