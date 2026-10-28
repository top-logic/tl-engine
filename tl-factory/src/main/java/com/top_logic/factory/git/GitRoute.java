/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.git;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.RequestAuthentication;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.ProxyError;
import com.top_logic.factory.proxy.ProxyExchange;
import com.top_logic.factory.proxy.ProxyForwarding;
import com.top_logic.factory.proxy.ProxyRoute;
import com.top_logic.factory.proxy.ProxyTokens;
import com.top_logic.factory.proxy.WorkspaceProxyHandler;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.model.TLObject;

/**
 * {@link ProxyRoute} of the {@link WorkspaceProxyService workspace proxy} to the repository of the
 * project of a workspace on the git hosting service of its git connection, without a credential of
 * the hosting service in the workspace.
 *
 * <p>
 * The route passes git's smart HTTP protocol through to the {@link GitHostingProvider}, unchanged
 * and streamed in both directions. Below the path of the route (by default {@link #DEFAULT_PATH}),
 * a repository is addressed as {@code /<owner>/<name>.git} - the {@link RepositoryName#GIT_SUFFIX}
 * may be missing -, and only the requests of the protocol are forwarded:
 * </p>
 * <ul>
 * <li>{@code GET} of {@link #INFO_REFS} with the query {@link #SERVICE_PARAMETER}{@code =}
 * {@link #UPLOAD_PACK} (fetch, clone) or {@link #RECEIVE_PACK} (push),</li>
 * <li>{@code POST} of {@link #UPLOAD_PACK} and {@link #RECEIVE_PACK}.</li>
 * </ul>
 *
 * <p>
 * The repository must be the one of the project of the workspace the token of the request was
 * issued for, compared without regard to case; the request goes to the provider of the git
 * connection of the project (see {@link #workspaceTarget(String)}). Every other request - another
 * path or method, the repository of another project, a workspace without repository or whose
 * project has no available git connection - is answered with 404, not telling whether a repository
 * exists.
 * </p>
 *
 * <p>
 * Of the request, only the headers of git's protocol are forwarded - {@link ProxyForwarding#CONTENT_TYPE},
 * {@link #CONTENT_ENCODING} of a compressed request, {@link #GIT_PROTOCOL} for protocol version 2,
 * {@link #ACCEPT}, {@link #ACCEPT_ENCODING}, {@link #ACCEPT_LANGUAGE}, {@link #USER_AGENT} -, never
 * the token of the client, cookies, or headers the hosting service might trust
 * from the address of the control application (e.g. the user header of an authenticating reverse
 * proxy). The request is authenticated with the {@link GitHostingProvider#gitCredentials()
 * credentials of the git connection}. Status, headers and body of the answer are passed back
 * as they come, except cookies and hop-by-hop headers.
 * </p>
 *
 * <p>
 * If the hosting service rejects the credentials (401), they are invalidated; a {@link #INFO_REFS}
 * request, which has no body, is repeated once with fresh ones. A request with body cannot be
 * repeated - the body is streamed to the hosting service as it arrives and is gone -, it is answered
 * with 502. Since git always starts with {@link #INFO_REFS}, rotated credentials are picked up
 * there.
 * </p>
 *
 * <p>
 * A request without valid token is answered with 401 and {@link #WWW_AUTHENTICATE} for basic
 * authentication: git sends its first request without credentials and asks its credential helper
 * only after such a challenge. The token is accepted as password of basic authentication (see
 * {@link WorkspaceProxyHandler}); a process in a workspace gets the {@link #gitConfig(String)
 * credential helper} answering with it.
 * </p>
 *
 * @implNote The repository of a project is {@link FactoryModel#getRepository(TLObject)}, the
 *           forwarded headers are {@link #FORWARDED_REQUEST_HEADERS}.
 */
public class GitRoute implements ProxyRoute {

	/**
	 * Configuration of {@link GitRoute}.
	 */
	public interface Config<I extends GitRoute> extends ProxyRoute.Config<I> {

		/**
		 * Configuration property name of {@link #getConnectTimeout()}.
		 */
		String CONNECT_TIMEOUT = "connect-timeout";

		/**
		 * Configuration property name of {@link #getResponseTimeout()}.
		 */
		String RESPONSE_TIMEOUT = "response-timeout";

		/**
		 * Configuration property name of {@link #getUserName()}.
		 */
		String USER_NAME = "user-name";

		/**
		 * Configuration property name of {@link #getUserEmail()}.
		 */
		String USER_EMAIL = "user-email";

		@Override
		@ClassDefault(GitRoute.class)
		Class<? extends I> getImplementationClass();

		@Override
		@StringDefault(DEFAULT_PATH)
		String getPath();

		/**
		 * How long connecting to the hosting service may take.
		 *
		 * @see #CONNECT_TIMEOUT
		 */
		@Name(CONNECT_TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(10 * 1000)
		long getConnectTimeout();

		/**
		 * How long the hosting service may take until the status of its answer, from the start of
		 * the request.
		 *
		 * <p>
		 * The hosting service answers a push only after it has received all of it, so this time
		 * includes the transfer of the largest push.
		 * </p>
		 *
		 * @see #RESPONSE_TIMEOUT
		 */
		@Name(RESPONSE_TIMEOUT)
		@Format(MillisFormat.class)
		@LongDefault(30 * 60 * 1000)
		long getResponseTimeout();

		/**
		 * The author and committer name of the commits made in a workspace, by the agent and by the
		 * control application; empty for none.
		 *
		 * <p>
		 * Every git process in a workspace that gets the {@link GitRoute#gitConfig(String) git
		 * configuration} of the route has it as {@link GitRoute#GIT_USER_NAME}, which takes
		 * precedence over the configuration files of the workspace.
		 * </p>
		 *
		 * @see #USER_NAME
		 */
		@Name(USER_NAME)
		@StringDefault(DEFAULT_USER_NAME)
		String getUserName();

		/**
		 * The author and committer e-mail address of the commits made in a workspace; empty for
		 * none.
		 *
		 * @see #USER_EMAIL
		 * @see #getUserName()
		 */
		@Name(USER_EMAIL)
		@StringDefault(DEFAULT_USER_EMAIL)
		String getUserEmail();

	}

	/**
	 * Default of {@link Config#getUserName()}.
	 */
	public static final String DEFAULT_USER_NAME = "TopLogic Factory";

	/**
	 * Default of {@link Config#getUserEmail()}.
	 */
	public static final String DEFAULT_USER_EMAIL = "tl-factory@localhost";

	/**
	 * Key of git's configuration of the name of the author and committer of a commit.
	 */
	public static final String GIT_USER_NAME = "user.name";

	/**
	 * Key of git's configuration of the e-mail address of the author and committer of a commit.
	 */
	public static final String GIT_USER_EMAIL = "user.email";

	/**
	 * Default of {@link Config#getPath()}.
	 */
	public static final String DEFAULT_PATH = "/git";

	/**
	 * Environment variable of a workspace container naming the URL of the route; the remote of the
	 * repository of the project is this URL followed by {@code /<owner>/<name>.git}, see
	 * {@link #remoteUrl(String, RepositoryName)}.
	 */
	public static final String ENV_GIT_URL = "TL_FACTORY_GIT_URL";

	/**
	 * Path below a repository of the ref advertisement, the first request of every fetch and push.
	 */
	public static final String INFO_REFS = "info/refs";

	/**
	 * The service of a fetch: value of {@link #SERVICE_PARAMETER} and path below a repository.
	 */
	public static final String UPLOAD_PACK = "git-upload-pack";

	/**
	 * The service of a push: value of {@link #SERVICE_PARAMETER} and path below a repository.
	 */
	public static final String RECEIVE_PACK = "git-receive-pack";

	/**
	 * Query parameter of {@link #INFO_REFS} naming the service.
	 */
	public static final String SERVICE_PARAMETER = "service";

	/**
	 * Header of git naming the protocol version and its options, e.g. {@code version=2}.
	 */
	public static final String GIT_PROTOCOL = "Git-Protocol";

	/**
	 * Header naming the compression of a request body, e.g. {@code gzip} of an
	 * {@link #UPLOAD_PACK} request.
	 */
	public static final String CONTENT_ENCODING = "Content-Encoding";

	/**
	 * Header naming the content types the client accepts.
	 */
	public static final String ACCEPT = "Accept";

	/**
	 * Header naming the compressions of the answer the client accepts.
	 */
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	/**
	 * Header naming the languages the client prefers for messages.
	 */
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	/**
	 * Header naming the client, e.g. {@code git/2.43.0}.
	 */
	public static final String USER_AGENT = "User-Agent";

	/**
	 * The request headers forwarded to the hosting service; all others are dropped.
	 */
	public static final List<String> FORWARDED_REQUEST_HEADERS = List.of(ProxyForwarding.CONTENT_TYPE,
		CONTENT_ENCODING, GIT_PROTOCOL, ACCEPT, ACCEPT_ENCODING, ACCEPT_LANGUAGE, USER_AGENT);

	/**
	 * Header of a 401 answer asking for credentials.
	 */
	public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

	/**
	 * Realm of the basic authentication {@link #WWW_AUTHENTICATE} asks for.
	 */
	public static final String REALM = "TopLogic workspace proxy";

	/**
	 * User name the {@link #gitConfig(String) credential helper} answers with; the proxy ignores
	 * it.
	 */
	public static final String HELPER_USERNAME = "tl-factory";

	/**
	 * Prefix of the key of git's configuration of the credentials for a URL, followed by the URL
	 * and {@link #HELPER_KEY_SUFFIX}.
	 */
	public static final String CREDENTIAL_KEY_PREFIX = "credential.";

	/**
	 * Suffix of the key of the credential helper of a URL, after {@link #CREDENTIAL_KEY_PREFIX} and
	 * the URL.
	 */
	public static final String HELPER_KEY_SUFFIX = ".helper";

	/**
	 * The credential helper of a process in a workspace: a shell function answering git's
	 * {@code get} with {@link #HELPER_USERNAME} and the token in
	 * {@link ProxyTokens#ENV_PROXY_TOKEN}, ignoring {@code store} and {@code erase}.
	 */
	public static final String CREDENTIAL_HELPER = "!f() { if [ \"$1\" = get ]; then echo username="
		+ HELPER_USERNAME + "; echo \"password=$" + ProxyTokens.ENV_PROXY_TOKEN + "\"; fi; }; f";

	private static final String GET = "GET";

	private static final String POST = "POST";

	private static final char PATH_SEPARATOR = '/';

	private static final char QUERY_START = '?';

	private static final String QUERY_SEPARATOR = "&";

	private static final char VALUE_SEPARATOR = '=';

	/**
	 * Owner or name of a repository in a path: no separator, no dot segment.
	 */
	private static final Pattern NAME_SEGMENT = Pattern.compile("[A-Za-z0-9_.\\-]+");

	private static final int SC_UNAUTHORIZED = 401;

	private static final int SC_NOT_FOUND = 404;

	private static final int SC_BAD_GATEWAY = 502;

	private static final int SC_SERVICE_UNAVAILABLE = 503;

	private static final int SC_GATEWAY_TIMEOUT = 504;

	private final String _path;

	private final Function<String, Target> _targetOfWorkspace;

	private final Duration _connectTimeout;

	private final Duration _responseTimeout;

	private final String _userName;

	private final String _userEmail;

	private HttpClient _client;

	/**
	 * The repository a workspace has access to and the provider of the git connection it lives on.
	 *
	 * @param provider
	 *        The provider whose {@link GitHostingProvider#gitUrl(RepositoryName) git URL} and
	 *        {@link GitHostingProvider#gitCredentials() credentials} the requests go to.
	 * @param repository
	 *        The repository of the project of the workspace.
	 */
	public record Target(GitHostingProvider provider, RepositoryName repository) {

		/**
		 * The target of the given repository on the given provider.
		 *
		 * @return The target, {@code null} if one of both is missing.
		 */
		public static Target of(GitHostingProvider provider, RepositoryName repository) {
			return provider == null || repository == null ? null : new Target(provider, repository);
		}
	}

	/**
	 * Creates a {@link GitRoute} from configuration, admitting each workspace to the repository of
	 * its stored project on the git connection of the project, see
	 * {@link #workspaceTarget(String)}.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public GitRoute(InstantiationContext context, Config<?> config) {
		this(config.getPath(), GitRoute::workspaceTarget, Duration.ofMillis(config.getConnectTimeout()),
			Duration.ofMillis(config.getResponseTimeout()), config.getUserName(), config.getUserEmail());
	}

	/**
	 * Creates a {@link GitRoute} that sets no commit identity.
	 *
	 * @see #GitRoute(String, Function, Duration, Duration, String, String)
	 */
	public GitRoute(String path, Function<String, Target> targetOfWorkspace, Duration connectTimeout,
			Duration responseTimeout) {
		this(path, targetOfWorkspace, connectTimeout, responseTimeout, null, null);
	}

	/**
	 * Creates a {@link GitRoute}.
	 *
	 * @param path
	 *        See {@link #getPath()}.
	 * @param targetOfWorkspace
	 *        The repository a workspace has access to and the provider it is reached through, by
	 *        the ID of the workspace; {@code null} for a workspace without repository or without
	 *        available git connection, whose requests are answered with 404.
	 * @param connectTimeout
	 *        See {@link Config#getConnectTimeout()}.
	 * @param responseTimeout
	 *        See {@link Config#getResponseTimeout()}.
	 * @param userName
	 *        See {@link Config#getUserName()}; {@code null} for none.
	 * @param userEmail
	 *        See {@link Config#getUserEmail()}; {@code null} for none.
	 */
	public GitRoute(String path, Function<String, Target> targetOfWorkspace, Duration connectTimeout,
			Duration responseTimeout, String userName, String userEmail) {
		_path = path;
		_targetOfWorkspace = targetOfWorkspace;
		_connectTimeout = connectTimeout;
		_responseTimeout = responseTimeout;
		_userName = blankToNull(userName);
		_userEmail = blankToNull(userEmail);
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

	@Override
	public String getPath() {
		return _path;
	}

	@Override
	public Map<String, String> containerEnv(String routeUrl) {
		return Map.of(ENV_GIT_URL, routeUrl);
	}

	/**
	 * The {@link #CREDENTIAL_HELPER credential helper} for the URL of the route: an empty helper
	 * first, which drops the helpers configured before for the URL, so that only this one answers;
	 * then the commit identity, {@link #GIT_USER_NAME} and {@link #GIT_USER_EMAIL}, as far as
	 * configured.
	 *
	 * <p>
	 * Git matches the URL of the route against the URL of a request as prefix of its path, so the
	 * helper answers for the repositories of the route only and the token never goes to another
	 * host.
	 * </p>
	 *
	 * @see Config#getUserName()
	 * @see Config#getUserEmail()
	 */
	@Override
	public List<Map.Entry<String, String>> gitConfig(String routeUrl) {
		String key = helperKey(routeUrl);
		List<Map.Entry<String, String>> result = new ArrayList<>();
		result.add(Map.entry(key, ""));
		result.add(Map.entry(key, CREDENTIAL_HELPER));
		if (_userName != null) {
			result.add(Map.entry(GIT_USER_NAME, _userName));
		}
		if (_userEmail != null) {
			result.add(Map.entry(GIT_USER_EMAIL, _userEmail));
		}
		return result;
	}

	/**
	 * The key of git's configuration of the credential helper of the given URL.
	 */
	public static String helperKey(String url) {
		return CREDENTIAL_KEY_PREFIX + url + HELPER_KEY_SUFFIX;
	}

	/**
	 * The URL a git command in a workspace reaches the given repository at through the route.
	 *
	 * @param routeUrl
	 *        The URL of the route as the workspace containers reach it, e.g. the value of
	 *        {@link #ENV_GIT_URL}.
	 * @param repository
	 *        The repository.
	 */
	public static String remoteUrl(String routeUrl, RepositoryName repository) {
		return routeUrl + PATH_SEPARATOR + repository.getFullName() + RepositoryName.GIT_SUFFIX;
	}

	/**
	 * The URL a git command in the workspace of the given project reaches its repository at
	 * through the git route of the running {@link WorkspaceProxyService}.
	 *
	 * @return The URL, {@code null} if the project has no repository, or the proxy is not running,
	 *         switched off, or has no git route.
	 */
	public static String projectRemoteUrl(TLObject project) {
		RepositoryName repository = FactoryModel.getRepository(project);
		WorkspaceProxyService proxy = WorkspaceProxyService.getInstance();
		if (repository == null || proxy == null) {
			return null;
		}
		GitRoute route = proxy.getRoute(GitRoute.class);
		String routeUrl = route == null ? null : proxy.getRouteUrl(route);
		return routeUrl == null ? null : remoteUrl(routeUrl, repository);
	}

	@Override
	public void handle(ProxyExchange exchange, String path, ProxyAdmission admission) throws IOException {
		GitRequest request = GitRequest.parse(exchange.method(), path, exchange.query());
		if (request == null) {
			notFound(exchange);
			return;
		}
		Target target = _targetOfWorkspace.apply(admission.workspaceId());
		RepositoryName repository = target == null ? null : target.repository();
		if (!request.repository().matches(repository)) {
			Logger.info("Git proxy: refused access of workspace '" + admission.workspaceId() + "' to '"
				+ request.repository() + "', its repository is '" + repository + "'.", GitRoute.class);
			notFound(exchange);
			return;
		}
		forward(target.provider(), repository, request, exchange);
	}

	@Override
	public void sendError(ProxyExchange exchange, ProxyError error) throws IOException {
		if (error == ProxyError.INVALID_TOKEN) {
			exchange.addHeader(WWW_AUTHENTICATE, WorkspaceProxyHandler.BASIC_PREFIX + "realm=\"" + REALM + "\"");
		}
		ProxyForwarding.sendText(exchange, error.status(), error.message());
	}

	private void forward(GitHostingProvider provider, RepositoryName repository, GitRequest request,
			ProxyExchange exchange) throws IOException {
		URI target = URI.create(provider.gitUrl(repository) + request.upstreamSuffix());
		CredentialProvider credentials = provider.gitCredentials();

		HttpResponse<InputStream> response = send(exchange, request, target, credentials);
		if (response == null) {
			return;
		}
		if (response.statusCode() == SC_UNAUTHORIZED) {
			response.body().close();
			credentials.invalidate();
			if (request.hasBody()) {
				// The body is gone, the request cannot be repeated.
				rejectedCredentials(exchange, target);
				return;
			}
			response = send(exchange, request, target, credentials);
			if (response == null) {
				return;
			}
			if (response.statusCode() == SC_UNAUTHORIZED) {
				response.body().close();
				credentials.invalidate();
				rejectedCredentials(exchange, target);
				return;
			}
		}
		ProxyForwarding.sendResponse(response, exchange);
	}

	/**
	 * Sends the request to the hosting service.
	 *
	 * @return The answer, {@code null} if there is none and the client got an error.
	 */
	private HttpResponse<InputStream> send(ProxyExchange exchange, GitRequest request, URI target,
			CredentialProvider credentials) throws IOException {
		HttpRequest.Builder builder = HttpRequest.newBuilder(target).timeout(_responseTimeout);
		if (request.hasBody()) {
			builder.POST(ProxyForwarding.requestBody(exchange));
		} else {
			builder.GET();
		}
		for (String name : FORWARDED_REQUEST_HEADERS) {
			for (String value : exchange.headers(name)) {
				builder.header(name, value);
			}
		}
		try {
			credentials.authenticate(RequestAuthentication.of(builder));
		} catch (I18NRuntimeException ex) {
			Logger.warn("Git proxy: no credentials for " + target + ": " + ex.getMessage(), GitRoute.class);
			error(exchange, SC_SERVICE_UNAVAILABLE, "The credentials of the git hosting service are not available.");
			return null;
		}

		try {
			return client().send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (HttpTimeoutException ex) {
			Logger.warn("Git proxy: " + target + " did not answer in time.", GitRoute.class);
			error(exchange, SC_GATEWAY_TIMEOUT, "The git hosting service did not answer in time.");
		} catch (IOException ex) {
			Logger.warn("Git proxy: " + target + " not reachable: " + ex.getClass().getName() + ": "
				+ ex.getMessage(), GitRoute.class);
			error(exchange, SC_BAD_GATEWAY, "The git hosting service is not reachable.");
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			error(exchange, SC_SERVICE_UNAVAILABLE, "Interrupted.");
		}
		return null;
	}

	private static void rejectedCredentials(ProxyExchange exchange, URI target) throws IOException {
		Logger.warn("Git proxy: " + target + " rejected the credentials of the control application.", GitRoute.class);
		error(exchange, SC_BAD_GATEWAY, "The git hosting service rejected the credentials of the control application.");
	}

	private static void notFound(ProxyExchange exchange) throws IOException {
		error(exchange, SC_NOT_FOUND, ProxyError.NOT_FOUND.message());
	}

	/**
	 * Answers with an error as plain text, which git shows. The message is a fixed text of this
	 * class, never something of the request or the hosting service.
	 */
	private static void error(ProxyExchange exchange, int status, String message) throws IOException {
		ProxyForwarding.sendText(exchange, status, message);
	}

	@Override
	public synchronized void close() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
	}

	private synchronized HttpClient client() {
		if (_client == null) {
			_client = HttpClient.newBuilder()
				.connectTimeout(_connectTimeout)
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
		}
		return _client;
	}

	/**
	 * The repository of the project of the stored workspace with the given ID, and the provider of
	 * the git connection of the project.
	 *
	 * @return The target, {@code null} if there is no such workspace, its project has no
	 *         repository, or the git connection of the project is not available.
	 * @see GitConnections#projectProvider(TLObject)
	 */
	public static Target workspaceTarget(String workspaceId) {
		return ThreadContext.<Target, RuntimeException, RuntimeException> inSystemContext(GitRoute.class, () -> {
			TLObject workspace = FactoryModel.findWorkspace(workspaceId);
			if (workspace == null) {
				return null;
			}
			TLObject project = FactoryModel.getProject(workspace);
			return Target.of(GitConnections.projectProvider(project), FactoryModel.getRepository(project));
		});
	}

	/**
	 * A request of git's smart HTTP protocol the route forwards.
	 *
	 * @param repository
	 *        The repository as the client names it.
	 * @param service
	 *        {@link GitRoute#UPLOAD_PACK} or {@link GitRoute#RECEIVE_PACK}.
	 * @param hasBody
	 *        Whether it is the {@code POST} of the service, otherwise the {@code GET} of
	 *        {@link GitRoute#INFO_REFS}.
	 */
	private record GitRequest(RepositoryName repository, String service, boolean hasBody) {

		/**
		 * The request of the given method, path below the route and query.
		 *
		 * @return The request, {@code null} if it is none the route forwards.
		 */
		static GitRequest parse(String method, String path, String query) {
			if (path.isEmpty() || path.charAt(0) != PATH_SEPARATOR) {
				return null;
			}
			String[] segments = path.substring(1).split(String.valueOf(PATH_SEPARATOR), -1);
			if (segments.length < 3) {
				return null;
			}
			RepositoryName repository = repository(segments[0], segments[1]);
			if (repository == null) {
				return null;
			}
			String rest = path.substring(1 + segments[0].length() + 1 + segments[1].length() + 1);
			if (INFO_REFS.equals(rest)) {
				String service = serviceOf(query);
				return GET.equals(method) && service != null ? new GitRequest(repository, service, false) : null;
			}
			if (isService(rest)) {
				return POST.equals(method) ? new GitRequest(repository, rest, true) : null;
			}
			return null;
		}

		private static RepositoryName repository(String owner, String name) {
			if (name.endsWith(RepositoryName.GIT_SUFFIX)) {
				name = name.substring(0, name.length() - RepositoryName.GIT_SUFFIX.length());
			}
			if (!isName(owner) || !isName(name)) {
				return null;
			}
			return new RepositoryName(owner, name);
		}

		private static boolean isName(String segment) {
			return NAME_SEGMENT.matcher(segment).matches() && !segment.chars().allMatch(c -> c == '.');
		}

		/**
		 * The value of {@link GitRoute#SERVICE_PARAMETER} in the given query, {@code null} if it has
		 * none or one the route does not forward.
		 */
		private static String serviceOf(String query) {
			if (query == null) {
				return null;
			}
			String prefix = SERVICE_PARAMETER + VALUE_SEPARATOR;
			for (String parameter : query.split(QUERY_SEPARATOR)) {
				if (parameter.startsWith(prefix)) {
					String service = parameter.substring(prefix.length());
					return isService(service) ? service : null;
				}
			}
			return null;
		}

		private static boolean isService(String value) {
			return UPLOAD_PACK.equals(value) || RECEIVE_PACK.equals(value);
		}

		/**
		 * What follows the {@link GitHostingProvider#gitUrl(RepositoryName) git URL} of the
		 * repository in the forwarded request.
		 */
		String upstreamSuffix() {
			if (hasBody) {
				return PATH_SEPARATOR + service;
			}
			return PATH_SEPARATOR + INFO_REFS + QUERY_START + SERVICE_PARAMETER + VALUE_SEPARATOR + service;
		}

	}

}
