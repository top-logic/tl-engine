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

import com.fasterxml.jackson.databind.JsonNode;

import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.git.GitCapabilities;
import com.top_logic.factory.git.GitConnectionSettings;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitRepository;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.git.RepositoryName;
import com.top_logic.factory.gitea.client.ApiClient;
import com.top_logic.factory.gitea.client.ApiException;
import com.top_logic.factory.gitea.client.api.OrganizationApi;
import com.top_logic.factory.gitea.client.api.RepositoryApi;
import com.top_logic.factory.gitea.client.api.UserApi;
import com.top_logic.factory.gitea.client.model.CreateBranchProtectionOption;
import com.top_logic.factory.gitea.client.model.CreateOrgOption;
import com.top_logic.factory.gitea.client.model.CreateRepoOption;
import com.top_logic.factory.gitea.client.model.EditBranchProtectionOption;
import com.top_logic.factory.gitea.client.model.OrganizationPermissions;
import com.top_logic.factory.gitea.client.model.Permission;
import com.top_logic.factory.gitea.client.model.Repository;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GitHostingProvider} of one git connection to a <a href="https://about.gitea.com/">Gitea</a>
 * server, created by the {@link GiteaProviderType}.
 *
 * <p>
 * The repositories are created in the {@link GitConnectionSettings#owner() owner} of the
 * connection: an organization, or the user of the access token. An organization the provider
 * {@link GitConnectionSettings#createOwner() creates itself} is created on the first creation of a
 * repository, private and owned by the user of the token. The requests are authenticated by the
 * {@link GitConnectionSettings#credentials() credentials} of the connection, usually an access
 * token sent with the scheme {@link #TOKEN_SCHEME}.
 * </p>
 *
 * <p>
 * Git's smart HTTP protocol reaches a repository at its clone URL below the URL of the server,
 * authenticated with the same credentials.
 * </p>
 *
 * <h2>The access token</h2>
 *
 * <p>
 * The token needs the scopes {@code write:organization} (creating a repository or an organization),
 * {@code write:repository} (reading repositories, protecting branches) and {@code read:user}
 * (telling the {@link #capabilities() rights} of the token); for repositories in the namespace of
 * its user also {@code write:user}. Its user must be allowed to create repositories in the
 * organization and to administer them, e.g. as member of its team <i>Owners</i>; a user without the
 * right to administer a repository gets it {@link GitRepository#canAdminister() reported}, and its
 * branches stay unprotected.
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
 * <li>Every other pull request needs {@link GiteaProviderType.Config#getRequiredApprovals()}
 * approvals and no rejecting review ({@code block_on_rejected_reviews}); an approval is dismissed by
 * a new commit ({@code dismiss_stale_approvals}).</li>
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
public class GiteaHostingProvider implements GitHostingProvider {

	/**
	 * Authentication scheme of a Gitea access token in the {@link CredentialProvider#AUTHORIZATION}
	 * header.
	 */
	public static final String TOKEN_SCHEME = "token";

	/**
	 * Path of the REST API below the URL of the server.
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
	 * Name of the request creating an organization in error messages, the operation of the Gitea
	 * API.
	 */
	private static final String OP_CREATE_ORGANIZATION = "orgCreate";

	/**
	 * Name of the request creating a repository in the namespace of the user of the token in error
	 * messages, the operation of the Gitea API.
	 */
	private static final String OP_CREATE_USER_REPOSITORY = "createCurrentUserRepo";

	/**
	 * Name of the request reading the user of the token in error messages, the operation of the
	 * Gitea API.
	 */
	private static final String OP_GET_USER = "userGetCurrent";

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

	private final String _owner;

	private final boolean _createOwner;

	private final int _requiredApprovals;

	private final CredentialProvider _credentials;

	private final ApiClient _client;

	private final RepositoryApi _repositories;

	private final OrganizationApi _organizations;

	private final UserApi _users;

	/**
	 * Creates a {@link GiteaHostingProvider}.
	 *
	 * @param settings
	 *        The server, owner and credentials of the connection; the URL of the server is the URL
	 *        under which its web interface is served, its {@code ROOT_URL}, with the REST API at
	 *        {@value #API_PATH} below it.
	 * @param timeout
	 *        How long a connection to Gitea and each request may take.
	 * @param requiredApprovals
	 *        The number of approvals a pull request to a protected branch needs.
	 * @throws TopLogicException
	 *         With {@link I18NConstants#ERROR_INVALID_BASE_URL__URL} if the URL is no URL of a
	 *         server.
	 */
	public GiteaHostingProvider(GitConnectionSettings settings, Duration timeout, int requiredApprovals) {
		_baseUrl = withSlash(settings.url().strip());
		URI apiUri;
		try {
			apiUri = URI.create(_baseUrl).resolve(API_PATH);
		} catch (IllegalArgumentException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_BASE_URL__URL.fill(settings.url()), ex);
		}
		if (apiUri.getScheme() == null || apiUri.getHost() == null) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_BASE_URL__URL.fill(settings.url()));
		}
		_owner = settings.owner();
		_createOwner = settings.createOwner();
		_requiredApprovals = requiredApprovals;
		_credentials = settings.credentials();

		_client = new ApiClient(HttpClient.newBuilder().connectTimeout(timeout),
			ApiClient.createDefaultObjectMapper(), apiUri.toString());
		_client.setReadTimeout(timeout);
		_client.setRequestInterceptor(request -> _credentials.authenticate(RequestAuthentication.of(request)));
		_repositories = new RepositoryApi(_client);
		_organizations = new OrganizationApi(_client);
		_users = new UserApi(_client);
	}

	@Override
	public String getOwner() {
		return _owner;
	}

	/**
	 * The URL of the server followed by owner, name and {@link RepositoryName#GIT_SUFFIX}, the
	 * clone URL of Gitea.
	 */
	@Override
	public String gitUrl(RepositoryName repository) {
		return _baseUrl + repository.getFullName() + RepositoryName.GIT_SUFFIX;
	}

	/**
	 * The credentials of the API requests: Gitea (checked with 28.2.0) accepts an access token for
	 * git over HTTP as {@code Authorization: token <token>} as well, besides as bearer token and as
	 * password of basic authentication.
	 */
	@Override
	public CredentialProvider gitCredentials() {
		return _credentials;
	}

	/**
	 * Asks Gitea for the user of the token and its permissions in the owner: in its own namespace,
	 * it may create and administer repositories; in an organization, it may create repositories as
	 * the permissions of the organization tell ({@code can_create_repository}), and administer them
	 * as owner or administrator of the organization. An organization that does not exist is fully
	 * accessible if the provider creates it (Gitea does not tell it from a private organization the
	 * user cannot see, whose creation then fails with
	 * {@link I18NConstants#ERROR_PERMISSION_DENIED__REPOSITORY}), otherwise the problem is
	 * {@link I18NConstants#ERROR_OWNER_NOT_FOUND__OWNER}. A token without the scope
	 * {@code read:user} gets {@link I18NConstants#ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM}.
	 *
	 * <p>
	 * The permissions of the user do not tell the scopes of the token, and Gitea does not show a
	 * token its own scopes. Where creating is allowed by the permissions, the creation request is
	 * therefore {@link #checkScopes(Request, String) sent without a name}: Gitea checks the scopes
	 * of the token before the content, so it answers 403 naming the missing scope (e.g.
	 * {@code write:user} for the namespace of the user), or 422 for the missing name, which creates
	 * nothing. A missing scope is reported as
	 * {@link I18NConstants#ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM} with Gitea's message.
	 * </p>
	 */
	@Override
	public GitCapabilities capabilities() {
		RepositoryName owner = new RepositoryName(_owner, "");
		String login;
		try {
			login = call(() -> _users.userGetCurrent()).getLogin();
		} catch (ApiException ex) {
			if (ex.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
				return GitCapabilities.unknown(I18NConstants.ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM
					.fill(_baseUrl, String.valueOf(errorMessage(ex))));
			}
			return GitCapabilities.unknown(failure(ex, OP_GET_USER, owner).getErrorKey());
		}
		if (_owner.equalsIgnoreCase(login)) {
			return withScopes(GitCapabilities.ALL,
				checkScopes(() -> _users.createCurrentUserRepo(new CreateRepoOption()), OP_CREATE_USER_REPOSITORY));
		}
		try {
			OrganizationPermissions permissions = call(() -> _organizations.orgGetUserPermissions(login, _owner));
			GitCapabilities granted = new GitCapabilities(isTrue(permissions.getCanCreateRepository()),
				isTrue(permissions.getIsOwner()) || isTrue(permissions.getIsAdmin()), null);
			if (!granted.createRepositories()) {
				return granted;
			}
			return withScopes(granted,
				checkScopes(() -> _organizations.createOrgRepo(_owner, new CreateRepoOption()), OP_CREATE_REPOSITORY));
		} catch (ApiException ex) {
			if (ex.getCode() == HttpURLConnection.HTTP_NOT_FOUND) {
				return _createOwner
					? withScopes(GitCapabilities.ALL,
						checkScopes(() -> _organizations.orgCreate(new CreateOrgOption()), OP_CREATE_ORGANIZATION))
					: GitCapabilities.unknown(I18NConstants.ERROR_OWNER_NOT_FOUND__OWNER.fill(_owner));
			}
			if (ex.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
				return GitCapabilities.unknown(I18NConstants.ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM
					.fill(_baseUrl, String.valueOf(errorMessage(ex))));
			}
			return GitCapabilities.unknown(failure(ex, OP_GET_USER, owner).getErrorKey());
		}
	}

	/**
	 * The given capabilities, or unknown ones with the given problem of the scopes of the token.
	 */
	private static GitCapabilities withScopes(GitCapabilities capabilities, ResKey scopeProblem) {
		return scopeProblem == null ? capabilities : GitCapabilities.unknown(scopeProblem);
	}

	/**
	 * Sends a creation request without content to learn whether the scopes of the token allow it,
	 * without creating anything: Gitea checks the scopes before the content of the request.
	 *
	 * @param request
	 *        The creation request, whose content lacks the mandatory name.
	 * @param operation
	 *        The name of the request in an error message.
	 * @return The problem, {@code null} if the scopes of the token allow the request (Gitea
	 *         refuses the content only).
	 */
	private ResKey checkScopes(Request<?> request, String operation) {
		try {
			call(request);
			// Not to be expected without a name; Gitea accepted the scopes all the same.
			return null;
		} catch (ApiException ex) {
			if (ex.getCode() == HTTP_UNPROCESSABLE) {
				return null;
			}
			if (ex.getCode() == HttpURLConnection.HTTP_FORBIDDEN) {
				return I18NConstants.ERROR_CAPABILITIES_UNKNOWN__SERVICE_PROBLEM
					.fill(_baseUrl, String.valueOf(errorMessage(ex)));
			}
			return failure(ex, operation, new RepositoryName(_owner, "")).getErrorKey();
		}
	}

	private static boolean isTrue(Boolean value) {
		return Boolean.TRUE.equals(value);
	}

	/**
	 * Creates the repository in the organization of the owner, or - if there is no such
	 * organization and the owner is the user of the token - in the namespace of the user. An
	 * organization the provider creates itself is created first if it does not exist.
	 */
	@Override
	public GitRepository createRepository(String name, String description) {
		RepositoryName fullName = new RepositoryName(getOwner(), name);
		CreateRepoOption option = new CreateRepoOption()
			.name(name)
			.description(description)
			._private(Boolean.TRUE)
			.autoInit(Boolean.FALSE);
		if (_createOwner) {
			ensureOrganization(fullName);
		}
		try {
			return toRepository(call(() -> _organizations.createOrgRepo(getOwner(), option)));
		} catch (ApiException ex) {
			if (ex.getCode() == HttpURLConnection.HTTP_NOT_FOUND && isOwnerTheUser()) {
				return createUserRepository(fullName, option);
			}
			throw createFailure(ex, fullName, name);
		}
	}

	private GitRepository createUserRepository(RepositoryName fullName, CreateRepoOption option) {
		try {
			return toRepository(call(() -> _users.createCurrentUserRepo(option)));
		} catch (ApiException ex) {
			throw createFailure(ex, fullName, fullName.getName());
		}
	}

	/**
	 * Whether the owner is the user of the token; {@code false} if Gitea does not tell.
	 */
	private boolean isOwnerTheUser() {
		try {
			return _owner.equalsIgnoreCase(call(() -> _users.userGetCurrent()).getLogin());
		} catch (ApiException ex) {
			return false;
		}
	}

	/**
	 * Creates the organization of the owner, private, unless it exists.
	 */
	private void ensureOrganization(RepositoryName fullName) {
		try {
			call(() -> _organizations.orgGet(_owner));
			return;
		} catch (ApiException ex) {
			if (ex.getCode() != HttpURLConnection.HTTP_NOT_FOUND) {
				throw failure(ex, OP_CREATE_ORGANIZATION, fullName);
			}
		}
		try {
			call(() -> _organizations.orgCreate(new CreateOrgOption()
				.username(_owner)
				.visibility(CreateOrgOption.VisibilityEnum.PRIVATE)));
		} catch (ApiException ex) {
			if (ex.getCode() != HTTP_UNPROCESSABLE) {
				throw failure(ex, OP_CREATE_ORGANIZATION, fullName);
			}
			// The name is taken: created concurrently, or an organization the user cannot see.
			try {
				call(() -> _organizations.orgGet(_owner));
			} catch (ApiException notVisible) {
				throw new TopLogicException(I18NConstants.ERROR_PERMISSION_DENIED__REPOSITORY.fill(fullName.getFullName()));
			}
		}
	}

	/**
	 * The exception reporting a failed creation of a repository.
	 */
	private TopLogicException createFailure(ApiException ex, RepositoryName fullName, String name) {
		switch (ex.getCode()) {
			case HTTP_CONFLICT:
				return new TopLogicException(
					I18NConstants.ERROR_REPOSITORY_EXISTS__REPOSITORY.fill(fullName.getFullName()));
			case HTTP_UNPROCESSABLE:
				return new TopLogicException(I18NConstants.ERROR_INVALID_REPOSITORY_NAME__NAME.fill(name));
			case HttpURLConnection.HTTP_NOT_FOUND:
				return new TopLogicException(I18NConstants.ERROR_OWNER_NOT_FOUND__OWNER.fill(getOwner()));
			default:
				return failure(ex, OP_CREATE_REPOSITORY, fullName);
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
			.requiredApprovals(Long.valueOf(_requiredApprovals))
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
		Permission permissions = repository.getPermissions();
		boolean canAdminister = permissions == null || isTrue(permissions.getAdmin());
		return new GitRepository(name, repository.getCloneUrl(), repository.getHtmlUrl(),
			repository.getDefaultBranch(), canAdminister);
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
