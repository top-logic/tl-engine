/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.gitea;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.server.ServerProviderConfig;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.gitea.client.ApiClient;
import com.top_logic.factory.gitea.client.ApiException;
import com.top_logic.factory.gitea.client.api.OrganizationApi;
import com.top_logic.factory.gitea.client.api.RepositoryApi;
import com.top_logic.factory.gitea.client.model.CreateBranchProtectionOption;
import com.top_logic.factory.gitea.client.model.CreateRepoOption;
import com.top_logic.factory.gitea.client.model.EditBranchProtectionOption;
import com.top_logic.factory.gitea.client.model.Repository;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GitHostingProvider} of a <a href="https://about.gitea.com/">Gitea</a> server.
 *
 * <p>
 * The repositories are created in the organization {@link Config#getOwner()} of the Gitea at
 * {@link Config#getBaseUrl()}. The requests are authenticated by the configured
 * {@link Config#getCredentials() credentials}; without them, with the access token in the secret
 * {@link #TOKEN_SECRET}, sent with the scheme {@link #TOKEN_SCHEME}.
 * </p>
 *
 * <p>
 * Git's smart HTTP protocol reaches a repository at its clone URL below the base URL,
 * authenticated with the same credentials.
 * </p>
 *
 * <h2>The access token</h2>
 *
 * <p>
 * The token needs the scopes {@code write:organization} (creating a repository in the
 * organization) and {@code write:repository} (reading repositories, protecting branches). Its user
 * must be allowed to create repositories in the organization and to administer them, e.g. as
 * member of its team <i>Owners</i>.
 * </p>
 *
 * <h2>Branch protection</h2>
 *
 * <p>
 * Protecting a branch sets up a branch protection rule named like the branch: pushing is allowed for every user with write access ({@code enable_push}, no push
 * allowlist), force pushing for no one ({@code enable_force_push} off), the protected file
 * patterns are the given patterns joined by semicolons ({@code protected_file_patterns}), there are
 * no unprotected file patterns. Gitea then enforces:
 * </p>
 * <ul>
 * <li>A protected branch cannot be deleted, and a push rewriting its history is rejected, also for
 * administrators.</li>
 * <li>A push changing a protected file is rejected, also for administrators. Gitea matches the
 * patterns against the lower-cased path; {@code *} matches neither {@code /} nor {@code .},
 * {@code **} matches any sequence.</li>
 * <li>A pull request changing a protected file can be merged only by a user who may bypass the
 * branch protection: an administrator of the repository ({@code block_admin_merge_override} off).
 * Such a merge ignores the required approvals; Gitea's own check of the pull request reports the
 * changed protected files to every other user.</li>
 * <li>Every other pull request needs {@link Config#getRequiredApprovals()} approvals and no
 * rejecting review ({@code block_on_rejected_reviews}); an approval is dismissed by a new commit
 * ({@code dismiss_stale_approvals}).</li>
 * </ul>
 *
 * <p>
 * A rule the branch already has is replaced by these settings. A branch is protected after the
 * push creating it: Gitea rejects the push that creates a branch whose rule has protected file
 * patterns ("cannot find the start commit"), so the first push into an empty repository has to
 * happen before.
 * </p>
 *
 * @implNote The clone URL of git's smart HTTP protocol is {@link #gitUrl(RepositoryName)}.
 */
public class GiteaHostingProvider extends AbstractConfiguredInstance<GiteaHostingProvider.Config<?>>
		implements GitHostingProvider {

	/**
	 * Configuration of {@link GiteaHostingProvider}.
	 */
	public interface Config<I extends GiteaHostingProvider> extends ServerProviderConfig<I> {

		/**
		 * Configuration property name of {@link #getOwner()}.
		 */
		String OWNER = "owner";

		/**
		 * Configuration property name of {@link #getCredentials()}.
		 */
		String CREDENTIALS = "credentials";

		/**
		 * Configuration property name of {@link #getTimeout()}.
		 */
		String TIMEOUT = "timeout";

		/**
		 * Configuration property name of {@link #getRequiredApprovals()}.
		 */
		String REQUIRED_APPROVALS = "required-approvals";

		/**
		 * The URL of the Gitea server, e.g. {@code https://git.example.com/}.
		 *
		 * <p>
		 * The URL under which the web interface is served, i.e. the {@code ROOT_URL} of the
		 * server. The REST API is expected below it, at {@value GiteaHostingProvider#API_PATH}.
		 * </p>
		 *
		 * <p>
		 * Empty switches the provider off, see {@link ServerProviderConfig}.
		 * </p>
		 *
		 * @see #BASE_URL
		 */
		@Override
		@Name(BASE_URL)
		@Nullable
		String getBaseUrl();

		/**
		 * The organization the repositories are created in.
		 *
		 * @see #OWNER
		 */
		@Name(OWNER)
		@Mandatory
		String getOwner();

		/**
		 * How the requests to Gitea are authenticated.
		 *
		 * <p>
		 * If not set, with the access token in the secret {@link GiteaHostingProvider#TOKEN_SECRET},
		 * sent with the scheme {@link GiteaHostingProvider#TOKEN_SCHEME}.
		 * </p>
		 *
		 * @see #CREDENTIALS
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		/**
		 * How long a connection to Gitea and each request may take.
		 *
		 * @see #TIMEOUT
		 */
		@Name(TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(30 * 1000)
		long getTimeout();

		/**
		 * The number of approvals a pull request to a protected branch needs.
		 *
		 * @see #REQUIRED_APPROVALS
		 * @see GiteaHostingProvider#protectBranch(GitRepository, String, List)
		 */
		@Name(REQUIRED_APPROVALS)
		@IntDefault(1)
		int getRequiredApprovals();

	}

	/**
	 * Name of the secret holding the Gitea access token, if no {@link Config#getCredentials()
	 * credentials} are configured.
	 *
	 * <p>
	 * The secret is fetched from the secret storage of the installation (see
	 * {@link NamedSecretSource}), by default from the system property or environment variable of
	 * this name.
	 * </p>
	 */
	public static final String TOKEN_SECRET = "tl_factory_gitea_token";

	/**
	 * Authentication scheme of a Gitea access token in the {@link CredentialProvider#AUTHORIZATION}
	 * header.
	 */
	public static final String TOKEN_SCHEME = "token";

	/**
	 * Path of the REST API below the {@link Config#getBaseUrl() base URL}.
	 */
	public static final String API_PATH = "api/v1";

	/**
	 * Separator of the patterns in the {@code protected_file_patterns} of a Gitea branch
	 * protection.
	 */
	private static final String PATTERN_SEPARATOR = ";";

	/**
	 * Field of an error answer of Gitea holding its description.
	 */
	private static final String ERROR_MESSAGE = "message";

	/**
	 * Status of an answer to a request the server cannot process for a conflict with the current
	 * state, e.g. a repository that already exists.
	 */
	private static final int HTTP_CONFLICT = 409;

	/**
	 * Status of an answer to a request with invalid content, e.g. a name Gitea does not accept.
	 */
	private static final int HTTP_UNPROCESSABLE = 422;

	/**
	 * Name of the request creating a repository in error messages, the operation of the Gitea API.
	 */
	private static final String OP_CREATE_REPOSITORY = "createOrgRepo";

	/**
	 * Name of the request reading a repository in error messages, the operation of the Gitea API.
	 */
	private static final String OP_GET_REPOSITORY = "repoGet";

	/**
	 * Name of the requests protecting a branch in error messages, the operation of the Gitea API
	 * creating the rule.
	 */
	private static final String OP_PROTECT_BRANCH = "repoCreateBranchProtection";

	/**
	 * Status of an {@link ApiException} without answer of the server.
	 */
	private static final int NO_RESPONSE = 0;

	private final String _baseUrl;

	private final CredentialProvider _credentials;

	private final ApiClient _client;

	private final RepositoryApi _repositories;

	private final OrganizationApi _organizations;

	/**
	 * Creates a {@link GiteaHostingProvider} from configuration.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public GiteaHostingProvider(InstantiationContext context, Config<?> config) {
		super(context, config);
		_baseUrl = withSlash(Objects.toString(config.getBaseUrl(), "").strip());
		URI apiUri;
		try {
			apiUri = URI.create(_baseUrl).resolve(API_PATH);
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationError(I18NConstants.ERROR_INVALID_BASE_URL__URL.fill(config.getBaseUrl()), ex);
		}
		if (apiUri.getScheme() == null || apiUri.getHost() == null) {
			throw new ConfigurationError(I18NConstants.ERROR_INVALID_BASE_URL__URL.fill(config.getBaseUrl()));
		}

		PolymorphicConfiguration<? extends CredentialProvider> credentials = config.getCredentials();
		if (credentials == null) {
			credentials = defaultCredentials();
		}
		_credentials = context.getInstance(credentials);

		Duration timeout = Duration.ofMillis(config.getTimeout());
		_client = new ApiClient(HttpClient.newBuilder().connectTimeout(timeout),
			ApiClient.createDefaultObjectMapper(), apiUri.toString());
		_client.setReadTimeout(timeout);
		_client.setRequestInterceptor(request -> _credentials.authenticate(RequestAuthentication.of(request)));
		_repositories = new RepositoryApi(_client);
		_organizations = new OrganizationApi(_client);
	}

	/**
	 * The credentials sending the access token in the secret {@link #TOKEN_SECRET}.
	 */
	private static PolymorphicConfiguration<? extends CredentialProvider> defaultCredentials() {
		NamedSecretSource.Config token = TypedConfiguration.newConfigItem(NamedSecretSource.Config.class);
		token.setName(TOKEN_SECRET);
		return HeaderCredentials.config(CredentialProvider.AUTHORIZATION, TOKEN_SCHEME, token);
	}

	@Override
	public String getOwner() {
		return getConfig().getOwner();
	}

	/**
	 * The {@link Config#getBaseUrl() base URL} followed by owner, name and
	 * {@link RepositoryName#GIT_SUFFIX}, the clone URL of Gitea.
	 */
	@Override
	public String gitUrl(RepositoryName repository) {
		return _baseUrl + repository.getFullName() + RepositoryName.GIT_SUFFIX;
	}

	/**
	 * The {@link Config#getCredentials() credentials} of the API requests: Gitea (checked with
	 * 28.2.0) accepts an access token for git over HTTP as {@code Authorization: token <token>} as
	 * well, besides as bearer token and as password of basic authentication.
	 */
	@Override
	public CredentialProvider gitCredentials() {
		return _credentials;
	}

	@Override
	public GitRepository createRepository(String name, String description) {
		RepositoryName fullName = new RepositoryName(getOwner(), name);
		CreateRepoOption option = new CreateRepoOption()
			.name(name)
			.description(description)
			._private(Boolean.TRUE)
			.autoInit(Boolean.FALSE);
		try {
			return toRepository(call(() -> _organizations.createOrgRepo(getOwner(), option)));
		} catch (ApiException ex) {
			switch (ex.getCode()) {
				case HTTP_CONFLICT:
					throw new TopLogicException(
						I18NConstants.ERROR_REPOSITORY_EXISTS__REPOSITORY
							.fill(fullName.getFullName()));
				case HTTP_UNPROCESSABLE:
					throw new TopLogicException(
						I18NConstants.ERROR_INVALID_REPOSITORY_NAME__NAME.fill(name));
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new TopLogicException(
						I18NConstants.ERROR_OWNER_NOT_FOUND__OWNER.fill(getOwner()));
				default:
					throw failure(ex, OP_CREATE_REPOSITORY, fullName);
			}
		}
	}

	@Override
	public GitRepository findRepository(String reference) {
		RepositoryName name = RepositoryName.parse(reference, _baseUrl);
		if (name == null) {
			return null;
		}
		try {
			return toRepository(call(() -> _repositories.repoGet(name.getOwner(), name.getName())));
		} catch (ApiException ex) {
			switch (ex.getCode()) {
				case HttpURLConnection.HTTP_NOT_FOUND:
				case HttpURLConnection.HTTP_FORBIDDEN:
					return null;
				default:
					throw failure(ex, OP_GET_REPOSITORY, name);
			}
		}
	}

	@Override
	public void protectBranch(GitRepository repository, String branch, List<String> protectedFiles) {
		RepositoryName name = repository.getName();
		CreateBranchProtectionOption protection = new CreateBranchProtectionOption()
			.ruleName(branch)
			.enablePush(Boolean.TRUE)
			.enablePushWhitelist(Boolean.FALSE)
			.enableForcePush(Boolean.FALSE)
			.enableForcePushAllowlist(Boolean.FALSE)
			.enableMergeWhitelist(Boolean.FALSE)
			.enableBypassAllowlist(Boolean.FALSE)
			.blockAdminMergeOverride(Boolean.FALSE)
			.protectedFilePatterns(String.join(PATTERN_SEPARATOR, protectedFiles))
			.unprotectedFilePatterns("")
			.requiredApprovals(Long.valueOf(getConfig().getRequiredApprovals()))
			.blockOnRejectedReviews(Boolean.TRUE)
			.dismissStaleApprovals(Boolean.TRUE);
		try {
			if (hasProtection(name, branch)) {
				EditBranchProtectionOption edit =
					_client.getObjectMapper().convertValue(protection, EditBranchProtectionOption.class);
				call(() -> _repositories.repoEditBranchProtection(name.getOwner(), name.getName(), branch, edit));
			} else {
				call(() -> _repositories.repoCreateBranchProtection(name.getOwner(), name.getName(), protection));
			}
		} catch (ApiException ex) {
			switch (ex.getCode()) {
				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new TopLogicException(
						I18NConstants.ERROR_REPOSITORY_NOT_FOUND__REPOSITORY
							.fill(name.getFullName()));
				default:
					throw failure(ex, OP_PROTECT_BRANCH, name);
			}
		}
	}

	/**
	 * Whether the given branch has a protection rule of its name.
	 */
	private boolean hasProtection(RepositoryName name, String branch) throws ApiException {
		try {
			call(() -> _repositories.repoGetBranchProtection(name.getOwner(), name.getName(), branch));
			return true;
		} catch (ApiException ex) {
			if (ex.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
				// Either the rule or the repository does not exist; creating the rule tells which.
				return false;
			}
			throw ex;
		}
	}

	/**
	 * Performs a request; if Gitea rejects the credentials, once more with fresh ones.
	 */
	private <T> T call(Request<T> request) throws ApiException {
		try {
			return request.send();
		} catch (ApiException ex) {
			if (ex.getCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
				throw ex;
			}
			// Cached credentials (an access token) may have expired or been revoked.
			_credentials.invalidate();
			try {
				return request.send();
			} catch (ApiException again) {
				if (again.getCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					_credentials.invalidate();
				}
				throw again;
			}
		}
	}

	/**
	 * The exception reporting a failed request that no operation handles itself.
	 */
	private TopLogicException failure(ApiException ex, String operation, RepositoryName name) {
		switch (ex.getCode()) {
			case NO_RESPONSE:
				Throwable cause = ex.getCause() == null ? ex : ex.getCause();
				return new TopLogicException(
					I18NConstants.ERROR_SERVICE_UNREACHABLE__SERVICE_PROBLEM.fill(_baseUrl,
						String.valueOf(cause.getMessage())),
					ex);
			case HttpURLConnection.HTTP_UNAUTHORIZED:
				return new TopLogicException(
					I18NConstants.ERROR_AUTHENTICATION_FAILED__SERVICE.fill(_baseUrl));
			case HttpURLConnection.HTTP_FORBIDDEN: {
				TopLogicException denied = new TopLogicException(
					I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(name.getFullName()));
				String message = errorMessage(ex);
				if (message != null) {
					denied.initDetails(ResKey.text(message));
				}
				return denied;
			}
			default:
				return new TopLogicException(I18NConstants.ERROR_REQUEST_FAILED__SERVICE_OPERATION_STATUS_MESSAGE
					.fill(_baseUrl, operation, ex.getCode(), String.valueOf(errorMessage(ex))));
		}
	}

	/**
	 * The description of the error in the answer of Gitea, {@code null} if it has none.
	 */
	private String errorMessage(ApiException ex) {
		String body = ex.getResponseBody();
		if (body == null || body.isBlank()) {
			return null;
		}
		try {
			JsonNode message = _client.getObjectMapper().readTree(body).get(ERROR_MESSAGE);
			return message == null ? body : message.asText();
		} catch (Exception notJson) {
			return body;
		}
	}

	private GitRepository toRepository(Repository repository) {
		RepositoryName name = RepositoryName.parse(repository.getFullName(), _baseUrl);
		if (name == null) {
			name = new RepositoryName(repository.getOwner().getLogin(), repository.getName());
		}
		return new GitRepository(name, repository.getCloneUrl(), repository.getHtmlUrl(),
			repository.getDefaultBranch());
	}

	private static String withSlash(String url) {
		return url.endsWith("/") ? url : url + "/";
	}

	/**
	 * A request of the generated Gitea client.
	 */
	private interface Request<T> {

		/**
		 * Sends the request and returns the answer.
		 */
		T send() throws ApiException;

	}

}
