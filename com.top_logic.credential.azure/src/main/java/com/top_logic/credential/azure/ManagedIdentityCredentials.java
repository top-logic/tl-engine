/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.credential.azure;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.RetryStrategy;
import com.azure.identity.ManagedIdentityCredentialBuilder;

import reactor.core.publisher.Mono;

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
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;

/**
 * {@link CredentialProvider} authenticating requests with an access token of the Azure managed
 * identity of the machine, app or pod the application runs on.
 *
 * <p>
 * Azure issues the token to the identity assigned to the virtual machine, App Service, Function,
 * Container App or Kubernetes workload, so the application owns no secret at all. Access is granted
 * to the identity in Azure (role assignment), e.g. the role
 * <code>Cognitive Services OpenAI User</code> for a language model deployed in Azure AI services.
 * </p>
 *
 * <p>
 * The token is requested with the <code>ManagedIdentityCredential</code> of the Azure Identity
 * library, which finds the identity endpoint of the platform from the environment variables Azure
 * sets: <code>IDENTITY_ENDPOINT</code> and <code>IDENTITY_HEADER</code> in an App Service, a
 * Function or a Container App; <code>AZURE_TENANT_ID</code>, <code>AZURE_CLIENT_ID</code> and
 * <code>AZURE_FEDERATED_TOKEN_FILE</code> in a pod of the Azure Kubernetes Service (AKS) with
 * workload identity, whose service account token is exchanged at Microsoft Entra ID; otherwise,
 * e.g. on a virtual machine or a scale set, the instance metadata service (IMDS) at
 * <code>http://169.254.169.254</code>, or the host given in
 * <code>AZURE_POD_IDENTITY_AUTHORITY_HOST</code>. The library caches the token and renews it
 * shortly before it expires.
 * </p>
 *
 * <p>
 * A language model in Azure AI services with the system-assigned identity, or in AKS with the
 * workload identity of the pod:
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
 * A token request answered with a status that Azure documents as transient (404 and 410 while the
 * metadata service is updating, 429 when throttling, 5xx) is repeated a few times with growing
 * delay. An endpoint that cannot be reached at all is reported at once, since the application then
 * does not run on Azure.
 * </p>
 */
public class ManagedIdentityCredentials extends AbstractConfiguredInstance<ManagedIdentityCredentials.Config>
		implements CredentialProvider {

	/**
	 * Suffix of the scope requesting a token with the permissions granted to the identity for a
	 * resource.
	 */
	public static final String DEFAULT_SCOPE_SUFFIX = "/.default";

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
		 * there is no system-assigned one. In AKS, the client ID of the workload identity is taken
		 * from the environment variable <code>AZURE_CLIENT_ID</code>.
		 * </p>
		 */
		@Name(CLIENT_ID)
		String getClientId();

		/**
		 * @see #getClientId()
		 */
		void setClientId(String value);

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

	private final HttpClient _httpClient;

	private final TokenRequestContext _request;

	private final HttpPipelinePolicy _recorder = new OutcomeRecorder();

	/**
	 * The outcome of the last request of the Azure Identity library to an identity endpoint.
	 */
	private volatile Outcome _lastOutcome;

	/**
	 * The credential of the Azure Identity library, replaced on {@link #invalidate()}.
	 */
	private volatile TokenCredential _credential;

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
		_httpClient = AzureHttpClients.create(config);
		_request = new TokenRequestContext().addScopes(scope(config.getResource()));
		_credential = createCredential();
	}

	private static String scope(String resource) {
		return resource.endsWith(DEFAULT_SCOPE_SUFFIX) ? resource : resource + DEFAULT_SCOPE_SUFFIX;
	}

	private TokenCredential createCredential() {
		Config config = getConfig();
		return new ManagedIdentityCredentialBuilder()
			.clientId(StringServices.nonEmpty(config.getClientId()))
			.httpClient(_httpClient)
			.addPolicy(_recorder)
			.retryPolicy(new RetryPolicy(new TransientErrorRetry(config.getMaxAttempts(), config.getRetryDelay())))
			.build();
	}

	@Override
	public void authenticate(RequestAuthentication request) {
		request.setHeader(AUTHORIZATION, BEARER + ' ' + fetchToken().getToken());
	}

	/**
	 * Drops the per-credential state of the Azure Identity library, such as the token of the
	 * workload identity in AKS, so that the next request fetches a fresh one.
	 *
	 * <p>
	 * A token from the instance metadata service or the identity endpoint of an App Service is
	 * kept by the library in a cache shared by the whole application and by the endpoint itself;
	 * such a token is renewed when it is about to expire. Since these endpoints do not revoke a
	 * token before its end of life, a rejection of such a token means a missing role assignment
	 * rather than a stale token.
	 * </p>
	 */
	@Override
	public void invalidate() {
		_credential = createCredential();
	}

	private AccessToken fetchToken() {
		_lastOutcome = null;
		try {
			return _credential.getTokenSync(_request);
		} catch (RuntimeException ex) {
			throw error(ex);
		}
	}

	/**
	 * The error reported for a failed token request.
	 *
	 * <p>
	 * The exceptions of the Azure Identity library may quote the response of the identity endpoint,
	 * which may carry a token. When the endpoint answered, the error is therefore described by the
	 * {@link Outcome} of the request alone, without the exception of the library as cause.
	 * </p>
	 */
	private I18NRuntimeException error(RuntimeException ex) {
		String resource = getConfig().getResource();
		Outcome outcome = _lastOutcome;
		if (outcome == null) {
			// No request has been sent, the library itself refused the request.
			return new I18NRuntimeException(
				I18NConstants.ERROR_TOKEN_UNAVAILABLE__RESOURCE_DETAIL.fill(resource, ex.getMessage()), ex);
		}
		if (outcome.getFailure() != null) {
			return new I18NRuntimeException(
				I18NConstants.ERROR_IDENTITY_ENDPOINT_UNREACHABLE__URL.fill(outcome.getEndpoint()),
				outcome.getFailure());
		}
		if (outcome.getStatus() / 100 == 2) {
			return new I18NRuntimeException(I18NConstants.ERROR_MALFORMED_RESPONSE__URL.fill(outcome.getEndpoint()));
		}
		return new I18NRuntimeException(I18NConstants.ERROR_TOKEN_REQUEST_FAILED__RESOURCE_URL_STATUS_DETAIL
			.fill(resource, outcome.getEndpoint(), outcome.getStatus(), outcome.getDetail()));
	}

	/**
	 * {@link RetryStrategy} repeating a token request answered with a transient error.
	 *
	 * <p>
	 * The instance metadata service answers 404 and 410 while it is updating, 429 when throttling.
	 * A request that fails without response is not repeated.
	 * </p>
	 */
	private static final class TransientErrorRetry implements RetryStrategy {

		private final int _maxRetries;

		private final long _delay;

		TransientErrorRetry(int maxAttempts, long delay) {
			_maxRetries = Math.max(0, maxAttempts - 1);
			_delay = delay;
		}

		@Override
		public int getMaxRetries() {
			return _maxRetries;
		}

		@Override
		public Duration calculateRetryDelay(int retryAttempts) {
			return Duration.ofMillis(_delay << Math.min(Math.max(0, retryAttempts), 20));
		}

		@Override
		public boolean shouldRetry(HttpResponse response) {
			int status = response.getStatusCode();
			return status == 404 || status == 410 || status == 429 || status >= 500;
		}

		@Override
		public boolean shouldRetryException(Throwable throwable) {
			return false;
		}

	}

	/**
	 * Outcome of a request to an identity endpoint, as far as it may be reported.
	 */
	private static final class Outcome {

		private final String _endpoint;

		private final int _status;

		private final String _detail;

		private final IOException _failure;

		Outcome(String endpoint, int status, String detail, IOException failure) {
			_endpoint = endpoint;
			_status = status;
			_detail = detail;
			_failure = failure;
		}

		/**
		 * URL of the endpoint without query.
		 */
		String getEndpoint() {
			return _endpoint;
		}

		/**
		 * HTTP status of the response, 0 if there is none.
		 */
		int getStatus() {
			return _status;
		}

		/**
		 * Error code and description of an error response, see
		 * {@link AzureErrorResponse#describe(String)}.
		 */
		String getDetail() {
			return _detail;
		}

		/**
		 * The error that prevented a response, <code>null</code> if the endpoint answered.
		 */
		IOException getFailure() {
			return _failure;
		}

	}

	/**
	 * {@link HttpPipelinePolicy} recording the {@link Outcome} of the requests to the identity
	 * endpoint in {@link ManagedIdentityCredentials#_lastOutcome}.
	 */
	private final class OutcomeRecorder implements HttpPipelinePolicy {

		@Override
		public HttpPipelinePosition getPipelinePosition() {
			// Sees the final response after all retries.
			return HttpPipelinePosition.PER_CALL;
		}

		@Override
		public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
			String endpoint = endpoint(context);
			return next.process()
				.flatMap(response -> {
					if (isSuccess(response)) {
						record(endpoint, response.getStatusCode(), "");
						return Mono.just(response);
					}
					HttpResponse buffered = response.buffer();
					return buffered.getBodyAsString().defaultIfEmpty("").map(body -> {
						record(endpoint, buffered.getStatusCode(), AzureErrorResponse.describe(body));
						return buffered;
					});
				})
				.doOnError(ex -> recordFailure(endpoint, ex));
		}

		@Override
		public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
			String endpoint = endpoint(context);
			HttpResponse response;
			try {
				response = next.processSync();
			} catch (RuntimeException ex) {
				recordFailure(endpoint, ex);
				throw ex;
			}
			if (isSuccess(response)) {
				record(endpoint, response.getStatusCode(), "");
				return response;
			}
			HttpResponse buffered = response.buffer();
			record(endpoint, buffered.getStatusCode(), AzureErrorResponse.describe(buffered.getBodyAsBinaryData().toString()));
			return buffered;
		}

		private boolean isSuccess(HttpResponse response) {
			return response.getStatusCode() / 100 == 2;
		}

		private String endpoint(HttpPipelineCallContext context) {
			URL url = context.getHttpRequest().getUrl();
			return url.getProtocol() + "://" + url.getAuthority() + url.getPath();
		}

		private void record(String endpoint, int status, String detail) {
			_lastOutcome = new Outcome(endpoint, status, detail, null);
		}

		private void recordFailure(String endpoint, Throwable ex) {
			IOException failure = ioCause(ex);
			if (failure != null) {
				_lastOutcome = new Outcome(endpoint, 0, "", failure);
			}
		}

	}

	/**
	 * The {@link IOException} in the cause chain of the given exception, <code>null</code> if there
	 * is none.
	 */
	static IOException ioCause(Throwable ex) {
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof IOException) {
				return (IOException) cause;
			}
			if (cause.getCause() == cause) {
				break;
			}
		}
		return null;
	}

}
