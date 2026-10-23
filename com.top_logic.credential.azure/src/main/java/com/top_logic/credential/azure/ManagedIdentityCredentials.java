/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minidev.json.JSONObject;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.util.JSONObjectUtils;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.credential.oauth.ClientCredentialsProvider;
import com.top_logic.credential.oauth.IssuedToken;
import com.top_logic.credential.oauth.OAuthServer;
import com.top_logic.credential.oauth.TokenCache;

/**
 * {@link CredentialProvider} authenticating requests with an access token of the Azure managed
 * identity of the machine or app the application runs on.
 *
 * <p>
 * Azure issues the token to the identity assigned to the virtual machine, App Service, Function or
 * Container App, so the application owns no secret at all. Access is granted to the identity in
 * Azure (role assignment), e.g. the role <code>Cognitive Services OpenAI User</code> for a language model
 * deployed in Azure AI services.
 * </p>
 *
 * <p>
 * The token is fetched from the identity endpoint of the platform: In an App Service, a Function or
 * a Container App, the platform announces the endpoint in the environment variables
 * <code>IDENTITY_ENDPOINT</code> and <code>IDENTITY_HEADER</code>. Elsewhere, e.g. on a virtual
 * machine or a scale set, the token comes from the instance metadata service (IMDS) at
 * <code>http://169.254.169.254</code>. The token is cached and renewed before it expires (see
 * {@link TokenCache}).
 * </p>
 *
 * <p>
 * A language model in Azure AI services with the system-assigned identity:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
 *   resource="https://cognitiveservices.azure.com"
 * /&gt;
 * </pre>
 *
 * <p>
 * With a user-assigned identity, its client ID selects it:
 * </p>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.credential.azure.ManagedIdentityCredentials"
 *   resource="https://cognitiveservices.azure.com"
 *   client-id="00000000-0000-0000-0000-000000000000"
 * /&gt;
 * </pre>
 *
 * <p>
 * In Azure Kubernetes Service (AKS), a pod authenticates with workload identity instead: Its
 * projected service account token is exchanged at Microsoft Entra ID for an access token with the
 * {@link ClientCredentialsProvider}, using the variables <code>AZURE_TENANT_ID</code>,
 * <code>AZURE_CLIENT_ID</code> and <code>AZURE_FEDERATED_TOKEN_FILE</code> that the workload
 * identity webhook sets in the pod. The configuration references them through aliases defined in
 * the application configuration:
 * </p>
 *
 * <pre>
 * &lt;alias&gt;
 *   &lt;entry name="%AZURE_TENANT_ID%" value="${env:AZURE_TENANT_ID}"/&gt;
 *   &lt;entry name="%AZURE_CLIENT_ID%" value="${env:AZURE_CLIENT_ID}"/&gt;
 *   &lt;entry name="%AZURE_FEDERATED_TOKEN_FILE%" value="${env:AZURE_FEDERATED_TOKEN_FILE}"/&gt;
 * &lt;/alias&gt;
 * </pre>
 *
 * <pre>
 * &lt;credentials class="com.top_logic.credential.oauth.ClientCredentialsProvider"
 *   token-url="https://login.microsoftonline.com/%AZURE_TENANT_ID%/oauth2/v2.0/token"
 *   client-id="%AZURE_CLIENT_ID%"
 *   scope="https://cognitiveservices.azure.com/.default"
 * &gt;
 *   &lt;client-authentication class="com.top_logic.credential.oauth.ClientAssertionAuthentication"&gt;
 *     &lt;assertion class="com.top_logic.basic.credential.FileSecretSource" path="%AZURE_FEDERATED_TOKEN_FILE%"/&gt;
 *   &lt;/client-authentication&gt;
 * &lt;/credentials&gt;
 * </pre>
 *
 * <p>
 * A request answered with a status that Azure documents as transient (404 and 410 while the
 * metadata service is updating, 429 when throttling, 5xx) is repeated a few times with growing
 * delay. An endpoint that cannot be reached at all is reported at once, since the application then
 * does not run on Azure.
 * </p>
 */
public class ManagedIdentityCredentials extends AbstractConfiguredInstance<ManagedIdentityCredentials.Config>
		implements CredentialProvider {

	/**
	 * Environment variable with the URL of the identity endpoint in an App Service, a Function or a
	 * Container App.
	 */
	public static final String ENV_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";

	/**
	 * Environment variable with the value of the {@link #HEADER_IDENTITY} header for requests to the
	 * {@link #ENV_IDENTITY_ENDPOINT}.
	 */
	public static final String ENV_IDENTITY_HEADER = "IDENTITY_HEADER";

	/**
	 * Request header proving to the {@link #ENV_IDENTITY_ENDPOINT} that the request comes from
	 * within the app.
	 */
	public static final String HEADER_IDENTITY = "X-IDENTITY-HEADER";

	/**
	 * Request header required by the instance metadata service, with the value
	 * {@link #METADATA_TRUE}.
	 */
	public static final String HEADER_METADATA = "Metadata";

	/**
	 * Value of the {@link #HEADER_METADATA} header.
	 */
	public static final String METADATA_TRUE = "true";

	/**
	 * Token endpoint of the instance metadata service.
	 */
	public static final String IMDS_ENDPOINT = "http://169.254.169.254/metadata/identity/oauth2/token";

	/**
	 * Version of the token API of the instance metadata service.
	 */
	public static final String IMDS_API_VERSION = "2018-02-01";

	/**
	 * Version of the token API of the {@link #ENV_IDENTITY_ENDPOINT}.
	 */
	public static final String APP_SERVICE_API_VERSION = "2019-08-01";

	/**
	 * Query parameter with the resource the token is requested for.
	 */
	public static final String PARAM_RESOURCE = "resource";

	/**
	 * Query parameter selecting a user-assigned identity by its client ID.
	 */
	public static final String PARAM_CLIENT_ID = "client_id";

	/**
	 * Field of the token response with the end of the token's lifetime in seconds since the epoch.
	 *
	 * <p>
	 * The identity endpoint of an App Service sends no <code>expires_in</code>, only this field.
	 * </p>
	 */
	public static final String FIELD_EXPIRES_ON = "expires_on";

	/**
	 * Lifetime in milliseconds of a token whose lifetime the endpoint does not specify.
	 */
	private static final long DEFAULT_TOKEN_LIFETIME = 5 * 60 * 1000;

	/**
	 * Configuration options for {@link ManagedIdentityCredentials}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ManagedIdentityCredentials>, AzureRestConfig {

		/**
		 * Tag name of this configuration in a list of {@link CredentialProvider}s.
		 */
		String TAG_NAME = "azure-managed-identity";

		/**
		 * @see #getResource()
		 */
		String RESOURCE = "resource";

		/**
		 * @see #getClientId()
		 */
		String CLIENT_ID = "client-id";

		/**
		 * @see #getEndpoint()
		 */
		String ENDPOINT = "endpoint";

		/**
		 * @see #getRenewalMargin()
		 */
		String RENEWAL_MARGIN = "renewal-margin";

		/**
		 * @see #getMaxAttempts()
		 */
		String MAX_ATTEMPTS = "max-attempts";

		/**
		 * @see #getRetryDelay()
		 */
		String RETRY_DELAY = "retry-delay";

		/**
		 * Identifier of the Azure service the token is requested for, e.g.
		 * <code>https://cognitiveservices.azure.com</code> for Azure AI services or
		 * <code>https://vault.azure.net</code> for Azure Key Vault.
		 */
		@Name(RESOURCE)
		@Mandatory
		String getResource();

		/**
		 * @see #getResource()
		 */
		void setResource(String value);

		/**
		 * Client ID of the user-assigned managed identity to use.
		 *
		 * <p>
		 * If empty, the system-assigned identity is used, or the only user-assigned identity if
		 * there is no system-assigned one.
		 * </p>
		 */
		@Name(CLIENT_ID)
		String getClientId();

		/**
		 * @see #getClientId()
		 */
		void setClientId(String value);

		/**
		 * URL of a token endpoint answering like the instance metadata service.
		 *
		 * <p>
		 * If empty, the endpoint is found automatically: the endpoint announced in the
		 * environment of an App Service, a Function or a Container App, otherwise the instance
		 * metadata service.
		 * </p>
		 */
		@Name(ENDPOINT)
		String getEndpoint();

		/**
		 * @see #getEndpoint()
		 */
		void setEndpoint(String value);

		/**
		 * Time before the end of the lifetime of an access token when it is renewed.
		 *
		 * <p>
		 * At most half of the token's lifetime is used as margin.
		 * </p>
		 */
		@Name(RENEWAL_MARGIN)
		@Format(MillisFormat.class)
		@FormattedDefault("5min")
		long getRenewalMargin();

		/**
		 * @see #getRenewalMargin()
		 */
		void setRenewalMargin(long value);

		/**
		 * Maximum number of attempts of a token request answered with a transient error.
		 */
		@Name(MAX_ATTEMPTS)
		@IntDefault(3)
		int getMaxAttempts();

		/**
		 * @see #getMaxAttempts()
		 */
		void setMaxAttempts(int value);

		/**
		 * Time to wait before the second attempt of a token request. The time doubles with each
		 * further attempt.
		 */
		@Name(RETRY_DELAY)
		@Format(MillisFormat.class)
		@FormattedDefault("1s")
		long getRetryDelay();

		/**
		 * @see #getRetryDelay()
		 */
		void setRetryDelay(long value);

	}

	private final AzureRestClient _client;

	private final TokenCache _tokens;

	/**
	 * Creates a {@link ManagedIdentityCredentials} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public ManagedIdentityCredentials(InstantiationContext context, Config config) {
		super(context, config);
		_client = new AzureRestClient(config);
		_tokens = new TokenCache(config.getRenewalMargin(), DEFAULT_TOKEN_LIFETIME);
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		IssuedToken token = _tokens.getToken(last -> fetchToken());
		request.setHeader(AUTHORIZATION, token.getAuthorization());
	}

	@Override
	public void invalidate() {
		_tokens.invalidate();
	}

	private IssuedToken fetchToken() {
		Config config = getConfig();
		Map<String, String> headers = new LinkedHashMap<>();
		Map<String, String> query = new LinkedHashMap<>();
		String endpoint = StringServices.nonEmpty(config.getEndpoint());
		if (endpoint == null) {
			String identityEndpoint = Environment.getSystemPropertyOrEnvironmentVariable(ENV_IDENTITY_ENDPOINT, null);
			String identityHeader = Environment.getSystemPropertyOrEnvironmentVariable(ENV_IDENTITY_HEADER, null);
			if (!StringServices.isEmpty(identityEndpoint) && !StringServices.isEmpty(identityHeader)) {
				endpoint = identityEndpoint;
				headers.put(HEADER_IDENTITY, identityHeader);
				query.put(AzureRestClient.PARAM_API_VERSION, APP_SERVICE_API_VERSION);
			}
		}
		if (headers.isEmpty()) {
			if (endpoint == null) {
				endpoint = IMDS_ENDPOINT;
			}
			headers.put(HEADER_METADATA, METADATA_TRUE);
			query.put(AzureRestClient.PARAM_API_VERSION, IMDS_API_VERSION);
		}
		query.put(PARAM_RESOURCE, config.getResource());
		String clientId = StringServices.nonEmpty(config.getClientId());
		if (clientId != null) {
			query.put(PARAM_CLIENT_ID, clientId);
		}
		return requestToken(endpoint, AzureRestClient.withQuery(endpoint, query), headers);
	}

	private IssuedToken requestToken(String endpoint, String url, Map<String, String> headers) {
		Config config = getConfig();
		int maxAttempts = Math.max(1, config.getMaxAttempts());
		long delay = config.getRetryDelay();
		for (int attempt = 1;; attempt++) {
			long issuedAt = System.currentTimeMillis();
			HttpResponse<String> response = send(endpoint, url, headers);
			if (AzureRestClient.isSuccess(response)) {
				return parseToken(endpoint, response.body(), issuedAt);
			}
			int status = response.statusCode();
			if (attempt >= maxAttempts || !isTransient(status) || !sleep(delay)) {
				throw new I18NRuntimeException(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL
					.fill(config.getResource(), endpoint, status, AzureRestClient.errorText(response.body())));
			}
			delay *= 2;
		}
	}

	private HttpResponse<String> send(String endpoint, String url, Map<String, String> headers) {
		try {
			return _client.get(url, headers);
		} catch (IOException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_IDENTITY_ENDPOINT_UNREACHABLE__URL.fill(endpoint), ex);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new I18NRuntimeException(I18NConstants.ERROR_IDENTITY_ENDPOINT_UNREACHABLE__URL.fill(endpoint), ex);
		}
	}

	/**
	 * Whether a request answered with the given status should be repeated.
	 *
	 * <p>
	 * The instance metadata service answers 404 and 410 while it is updating, 429 when throttling.
	 * </p>
	 */
	private static boolean isTransient(int status) {
		return status == 404 || status == 410 || status == 429 || status >= 500;
	}

	private static boolean sleep(long delay) {
		try {
			Thread.sleep(delay);
			return true;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static IssuedToken parseToken(String endpoint, String body, long issuedAt) {
		JSONObject response;
		try {
			response = JSONObjectUtils.parse(body);
		} catch (ParseException ex) {
			// The cause is not reported, its message could quote the token.
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(endpoint));
		}
		IssuedToken token = OAuthServer.parseToken(endpoint, response, issuedAt);
		if (token.getLifetime() > 0) {
			return token;
		}
		Object expiresOnValue = response.get(FIELD_EXPIRES_ON);
		if (expiresOnValue == null) {
			return token;
		}
		long expiresOn;
		try {
			expiresOn = Long.parseLong(expiresOnValue.toString());
		} catch (NumberFormatException ex) {
			throw new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(endpoint));
		}
		long expiresIn = Math.max(0, expiresOn - issuedAt / 1000);
		AccessToken accessToken = token.getAccessToken();
		return new IssuedToken(new BearerAccessToken(accessToken.getValue(), expiresIn, accessToken.getScope()), null,
			issuedAt);
	}

}
