/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.factory.llm.LLMRoute;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.util.model.ModelService;

/**
 * Access of the workspaces to services of the control application's environment without a
 * credential in the workspace.
 *
 * <p>
 * A workspace container gets no credential and no token in its environment, only what its
 * {@link Config#getRoutes() routes} need to find the proxy, e.g. the URL of the {@link LLMRoute LLM
 * route} - the {@link Config#getProxyUrl() URL of the proxy} followed by the path of the route.
 * A process that may use the proxy - an agent run - gets a token of its own,
 * {@link #issue(String, Duration) issued} for its workspace and passed only to that process, in
 * the {@link #processEnv(String) environment} of its command, with the git configuration the routes
 * contribute. The proxy accepts a request from an
 * {@link Config#getAllowedSources() accepted address} whose token admits it and passes it to the
 * route of its path, which adds the credentials of its upstream and forwards it, see
 * {@link WorkspaceProxyHandler}.
 * </p>
 *
 * <p>
 * A token is {@value TokenGrants#TOKEN_PREFIX} followed by a random secret of about 256 bits. It is
 * valid for all routes until it is {@link #revoke(String) revoked} - when its run ends -, until its
 * lifetime ends (at most {@link Config#getMaxTokenLifetime()}), or until its workspace is removed.
 * It is bound to the address of its first request and limited to {@link Config#getRateLimit()}
 * requests per minute, see {@link TokenGrants}. Tokens are kept in memory only, as their hashes; a
 * restart of the control application invalidates all of them, together with the runs it started.
 * </p>
 *
 * <p>
 * The proxy is switched on by its {@link Config#getProxyUrl() URL}: without one, workspaces get no
 * URL, no token is issued, and the proxy answers every request with 404. It listens on a port of
 * its own ({@link WorkspaceProxyListener}, {@link Config#getListenAddress()},
 * {@link Config#getListenPort()}); the {@link WorkspaceProxyServlet} in the web application answers
 * only if {@link Config#isServletEnabled()} is set.
 * </p>
 */
@ServiceDependencies({
	// Used by the LLM route.
	LLMProviderService.Module.class,
	PersistencyLayer.Module.class,
	ModelService.Module.class,
	WorkspaceService.Module.class,
})
public class WorkspaceProxyService extends ConfiguredManagedClass<WorkspaceProxyService.Config<?>>
		implements WorkspaceProxy, ProxyTokens {

	/**
	 * Configuration of {@link WorkspaceProxyService}.
	 */
	public interface Config<I extends WorkspaceProxyService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getProxyUrl()}.
		 */
		String PROXY_URL = "proxy-url";

		/**
		 * Configuration property name of {@link #getRoutes()}.
		 */
		String ROUTES = "routes";

		/**
		 * Configuration property name of {@link #getMaxTokenLifetime()}.
		 */
		String MAX_TOKEN_LIFETIME = "max-token-lifetime";

		/**
		 * Configuration property name of {@link #getAllowedSources()}.
		 */
		String ALLOWED_SOURCES = "allowed-sources";

		/**
		 * Configuration property name of {@link #getRateLimit()}.
		 */
		String RATE_LIMIT = "rate-limit";

		/**
		 * Configuration property name of {@link #getListenAddress()}.
		 */
		String LISTEN_ADDRESS = "listen-address";

		/**
		 * Configuration property name of {@link #getListenPort()}.
		 */
		String LISTEN_PORT = "listen-port";

		/**
		 * Configuration property name of {@link #isServletEnabled()}.
		 */
		String SERVLET_ENABLED = "servlet-enabled";

		/**
		 * Tag of an entry of {@link #getRoutes()}.
		 */
		String ROUTE = "route";

		/**
		 * The URL of the root of the proxy as the workspace containers reach it, without the path
		 * of a route: the {@link #getListenPort() port of its own}, e.g.
		 * {@code http://host.docker.internal:8091}, or the web application with the
		 * {@link WorkspaceProxyServlet}, e.g. {@code http://host.docker.internal:8080/tl-factory-app}.
		 *
		 * <p>
		 * A route is reached at this URL followed by its {@link ProxyRoute#getPath() path}. Empty
		 * switches the proxy off.
		 * </p>
		 *
		 * @see #PROXY_URL
		 */
		@Name(PROXY_URL)
		@Nullable
		String getProxyUrl();

		/**
		 * The services the proxy gives the workspaces access to, each below a path of its own.
		 *
		 * @see #ROUTES
		 */
		@Name(ROUTES)
		@EntryTag(ROUTE)
		@Key(ProxyRoute.Config.PATH)
		List<ProxyRoute.Config<?>> getRoutes();

		/**
		 * The longest lifetime of a token in milliseconds, e.g. {@code 4h}; a longer one asked for
		 * is cut to it.
		 *
		 * @see #MAX_TOKEN_LIFETIME
		 */
		@Name(MAX_TOKEN_LIFETIME)
		@Format(MillisFormat.class)
		@LongDefault(4 * 60 * 60 * 1000)
		long getMaxTokenLifetime();

		/**
		 * The client addresses requests are accepted from: ranges in CIDR notation (IPv4 or IPv6),
		 * single addresses, or {@value SourceFilter#WORKSPACE_NETWORK} for the network of the
		 * workspace containers.
		 *
		 * <p>
		 * Empty accepts every address. The address is the one of the connection, never a header
		 * of the request: behind a reverse proxy or a gateway it is the address of that proxy.
		 * </p>
		 *
		 * @see #ALLOWED_SOURCES
		 * @see SourceFilter
		 */
		@Name(ALLOWED_SOURCES)
		@Format(CommaSeparatedStrings.class)
		List<String> getAllowedSources();

		/**
		 * The requests per minute a token may make, with bursts up to this number; {@code 0} for
		 * no limit.
		 *
		 * <p>
		 * A request beyond is answered with 429 and the seconds to wait in {@code Retry-After},
		 * which Claude Code waits for before it repeats the request. The default leaves an agent
		 * working at full speed - a request per step and a few side requests - room, and limits a
		 * token that got out of the workspace.
		 * </p>
		 *
		 * @see #RATE_LIMIT
		 */
		@Name(RATE_LIMIT)
		@IntDefault(DEFAULT_RATE_LIMIT)
		int getRateLimit();

		/**
		 * The address the proxy listens on with a port of its own, see {@link #getListenPort()}.
		 *
		 * <p>
		 * The default, the loopback address, serves a reverse proxy or a tunnel on the same host
		 * only. With the control application on the host of the Docker daemon, it is the address
		 * {@code host-gateway} stands for in the workspaces - the gateway of Docker's default
		 * bridge, e.g. {@code 172.17.0.1} -, so that workspaces reach it but no other host does.
		 * In a container of its own, the control application listens on {@code 0.0.0.0} and the
		 * port is not published, or published on an internal address only.
		 * </p>
		 *
		 * @see #LISTEN_ADDRESS
		 */
		@Name(LISTEN_ADDRESS)
		@StringDefault(DEFAULT_LISTEN_ADDRESS)
		String getListenAddress();

		/**
		 * The port the proxy listens on, apart from the port of the control application; {@code 0}
		 * for no port of its own.
		 *
		 * <p>
		 * The routes are reached there at their {@link ProxyRoute#getPath() paths}. The port is
		 * opened only if the proxy is switched on by its {@link #getProxyUrl() URL}.
		 * </p>
		 *
		 * @see #LISTEN_PORT
		 */
		@Name(LISTEN_PORT)
		@IntDefault(DEFAULT_LISTEN_PORT)
		int getListenPort();

		/**
		 * Whether the proxy also answers in the web application, through the port of the control
		 * application, at the paths the {@link WorkspaceProxyServlet} is mapped to.
		 *
		 * <p>
		 * Off by default: the port of the control application is the one its users reach, and the
		 * proxy needs not be reachable there. Switched on, a reverse proxy in front of the control
		 * application must keep clients from outside from reaching these paths.
		 * </p>
		 *
		 * @see #SERVLET_ENABLED
		 */
		@Name(SERVLET_ENABLED)
		@BooleanDefault(false)
		boolean isServletEnabled();

	}

	/**
	 * Default of {@link Config#getRateLimit()}.
	 */
	public static final int DEFAULT_RATE_LIMIT = 120;

	/**
	 * Default of {@link Config#getListenAddress()}, the loopback address.
	 */
	public static final String DEFAULT_LISTEN_ADDRESS = "127.0.0.1";

	/**
	 * Default of {@link Config#getListenPort()}.
	 */
	public static final int DEFAULT_LISTEN_PORT = 8091;

	private static final String PATH_SEPARATOR = "/";

	private final TokenGrants _grants;

	private final SourceFilter _sources;

	private final List<ProxyRoute> _routes;

	private WorkspaceProxyListener _listener;

	/**
	 * Creates a {@link WorkspaceProxyService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public WorkspaceProxyService(InstantiationContext context, Config<?> config) {
		super(context, config);
		try {
			_sources = new SourceFilter(config.getAllowedSources(),
				() -> WorkspaceService.getInstance().getProvider().networkSubnets());
		} catch (ParseException ex) {
			throw new ConfigurationError(
				"Invalid entry in '" + Config.ALLOWED_SOURCES + "' of the workspace proxy: " + ex.getMessage(), ex);
		}
		if (config.getRateLimit() < 0 || config.getListenPort() < 0) {
			throw new ConfigurationError("Negative '" + Config.RATE_LIMIT + "' or '" + Config.LISTEN_PORT
				+ "' of the workspace proxy.");
		}
		for (ProxyRoute.Config<?> route : config.getRoutes()) {
			String path = route.getPath();
			if (path == null || !path.startsWith(PATH_SEPARATOR) || path.endsWith(PATH_SEPARATOR)) {
				throw new ConfigurationError("The '" + ProxyRoute.Config.PATH + "' of a route of the workspace proxy"
					+ " must start with a slash and not end with one: " + path);
			}
		}
		_routes = List.copyOf(TypedConfiguration.getInstanceList(context, config.getRoutes()));
		_grants = new TokenGrants(Clock.systemUTC(), WorkspaceProxyService::workspaceExists, config.getRateLimit());
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (isEnabled() && getConfig().getListenPort() > 0) {
			InetSocketAddress address =
				new InetSocketAddress(getConfig().getListenAddress().strip(), getConfig().getListenPort());
			try {
				_listener = new WorkspaceProxyListener(address, () -> this);
				Logger.info("Workspace proxy listening on " + _listener.getAddress() + ".",
					WorkspaceProxyService.class);
			} catch (IOException ex) {
				Logger.error("Workspace proxy: cannot listen on " + address
					+ ", the workspaces have no access to its routes: " + ex.getMessage(), ex,
					WorkspaceProxyService.class);
			}
		}
	}

	@Override
	protected void shutDown() {
		if (_listener != null) {
			_listener.close();
			_listener = null;
		}
		for (ProxyRoute route : _routes) {
			route.close();
		}
		super.shutDown();
	}

	/**
	 * The address the proxy listens on with a port of its own, {@code null} if it does not.
	 */
	public InetSocketAddress getListenerAddress() {
		WorkspaceProxyListener listener = _listener;
		return listener == null ? null : listener.getAddress();
	}

	/**
	 * The {@link WorkspaceProxy} the {@link WorkspaceProxyServlet} of the web application serves:
	 * the {@link WorkspaceProxyService} if it is running and {@link Config#isServletEnabled() serves
	 * the servlet}, {@code null} otherwise.
	 */
	public static WorkspaceProxy servletProxy() {
		WorkspaceProxyService service = getInstance();
		return service != null && service.getConfig().isServletEnabled() ? service : null;
	}

	@Override
	public boolean isEnabled() {
		String url = getConfig().getProxyUrl();
		return url != null && !url.isBlank();
	}

	@Override
	public List<ProxyRoute> routes() {
		return _routes;
	}

	/**
	 * The longest lifetime of a token.
	 *
	 * @see Config#getMaxTokenLifetime()
	 */
	public Duration getMaxTokenLifetime() {
		return Duration.ofMillis(getConfig().getMaxTokenLifetime());
	}

	/**
	 * The first route of the given type.
	 *
	 * @return The route, {@code null} if the proxy has none of this type.
	 */
	public <R extends ProxyRoute> R getRoute(Class<R> type) {
		for (ProxyRoute route : _routes) {
			if (type.isInstance(route)) {
				return type.cast(route);
			}
		}
		return null;
	}

	/**
	 * The URL of the given route as the workspace containers reach it: the
	 * {@link Config#getProxyUrl() URL of the proxy} followed by the path of the route.
	 *
	 * @return The URL, {@code null} if the proxy is not {@link #isEnabled() switched on}.
	 */
	public String getRouteUrl(ProxyRoute route) {
		String root = proxyRoot();
		return root == null ? null : root + route.getPath();
	}

	/**
	 * The {@link Config#getProxyUrl() URL of the proxy} without slash at its end, {@code null} if
	 * the proxy is not {@link #isEnabled() switched on}.
	 */
	private String proxyRoot() {
		if (!isEnabled()) {
			return null;
		}
		String proxyUrl = getConfig().getProxyUrl().strip();
		while (proxyUrl.endsWith(PATH_SEPARATOR)) {
			proxyUrl = proxyUrl.substring(0, proxyUrl.length() - 1);
		}
		return proxyUrl;
	}

	/**
	 * The environment variables a workspace container gets for the routes of the proxy, e.g. their
	 * URLs; no token.
	 *
	 * @return The variables, empty if the proxy is not {@link #isEnabled() switched on}.
	 */
	public Map<String, String> containerEnv() {
		String proxyRoot = proxyRoot();
		if (proxyRoot == null) {
			return Collections.emptyMap();
		}
		Map<String, String> result = new LinkedHashMap<>();
		for (ProxyRoute route : _routes) {
			result.putAll(route.containerEnv(proxyRoot + route.getPath()));
		}
		return result;
	}

	@Override
	public Map<String, String> processEnv(String token) {
		return processEnv(_routes, proxyRoot(), token);
	}

	/**
	 * The environment variables a process with the given token gets for the given routes.
	 *
	 * <p>
	 * The token in {@link ProxyTokens#ENV_PROXY_TOKEN}, the {@link ProxyRoute#processEnv(String)
	 * variables of the routes}, and the {@link ProxyRoute#gitConfig(String) git configuration} of
	 * all routes as one set of {@link GitEnvironment git configuration variables}.
	 * </p>
	 *
	 * @param routes
	 *        The routes of the proxy.
	 * @param proxyRoot
	 *        The URL of the proxy, see {@link Config#getProxyUrl()}, without slash at its end;
	 *        {@code null} for a proxy that is switched off, which gives no git configuration.
	 * @param token
	 *        The token of the process.
	 */
	public static Map<String, String> processEnv(List<? extends ProxyRoute> routes, String proxyRoot, String token) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(ENV_PROXY_TOKEN, token);
		List<Map.Entry<String, String>> gitConfig = new ArrayList<>();
		for (ProxyRoute route : routes) {
			result.putAll(route.processEnv(token));
			if (proxyRoot != null) {
				gitConfig.addAll(route.gitConfig(proxyRoot + route.getPath()));
			}
		}
		result.putAll(GitEnvironment.config(gitConfig));
		return result;
	}

	/**
	 * Issues a token for a process in a workspace, e.g. an agent run.
	 *
	 * <p>
	 * The token is to be passed only to that process, in its {@link #processEnv(String)
	 * environment}, and {@link #revoke(String) revoked} when it ends.
	 * </p>
	 *
	 * @param workspaceId
	 *        The workspace the process runs in.
	 * @param lifetime
	 *        How long the token is valid at most; cut to the {@link #getMaxTokenLifetime() longest
	 *        lifetime}.
	 * @return The token, {@code null} if the proxy is not {@link #isEnabled() switched on}.
	 */
	@Override
	public String issue(String workspaceId, Duration lifetime) {
		if (!isEnabled()) {
			return null;
		}
		Duration max = getMaxTokenLifetime();
		return _grants.issue(workspaceId, lifetime.compareTo(max) > 0 ? max : lifetime);
	}

	/**
	 * When the given token stops being valid.
	 *
	 * @return The end of its lifetime, {@code null} if it is not valid.
	 */
	@Override
	public Instant expiresAt(String token) {
		return _grants.expiresAt(token);
	}

	/**
	 * Revokes a token: the next request presenting it is refused.
	 */
	@Override
	public void revoke(String token) {
		_grants.revoke(token);
	}

	/**
	 * Revokes all tokens issued for the given workspace, e.g. when it is removed.
	 */
	public void revokeAll(String workspaceId) {
		_grants.revokeAll(workspaceId);
	}

	@Override
	public boolean isAllowedSource(String remoteAddress) {
		return _sources.allows(remoteAddress);
	}

	@Override
	public ProxyAdmission admit(String token, String remoteAddress) {
		return _grants.admit(token, remoteAddress);
	}

	private static boolean workspaceExists(String workspaceId) {
		return ThreadContext.<Boolean, RuntimeException, RuntimeException> inSystemContext(
			WorkspaceProxyService.class,
			() -> Boolean.valueOf(FactoryModel.findWorkspace(workspaceId) != null)).booleanValue();
	}

	/**
	 * The {@link WorkspaceProxyService} of the running application, {@code null} if it is not
	 * started.
	 */
	public static WorkspaceProxyService getInstance() {
		return Module.INSTANCE.isActive() ? Module.INSTANCE.getImplementationInstance() : null;
	}

	/**
	 * Module of the {@link WorkspaceProxyService}.
	 */
	public static final class Module extends TypedRuntimeModule<WorkspaceProxyService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<WorkspaceProxyService> getImplementation() {
			return WorkspaceProxyService.class;
		}
	}

}
