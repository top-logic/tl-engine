/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
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
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.util.model.ModelService;

/**
 * Access of the workspaces to the Anthropic Messages API without an API key in the workspace.
 *
 * <p>
 * A workspace container gets no API key and no token in its environment, only the URL of the
 * {@link LLMProxyHandler proxy} of the control application in {@link #ENV_BASE_URL}. A process that may
 * use the API - an agent run - gets a token of its own, {@link #issue(String, Duration) issued} for
 * its workspace and passed only to that process in {@link #ENV_AUTH_TOKEN}; Claude Code sends it
 * as bearer token. The proxy accepts a request from an {@link Config#getAllowedSources() accepted
 * address} whose token admits it, adds the API key of the {@link Config#getProvider() configured
 * provider} of the {@link LLMProviderService} and forwards the request.
 * </p>
 *
 * <p>
 * A token is {@value #TOKEN_PREFIX} followed by a random secret of about 256 bits. It is valid until
 * it is {@link #revoke(String) revoked} - when its run ends -, until its lifetime ends (at most
 * {@link Config#getMaxTokenLifetime()}), or until its workspace is removed. It is bound to the
 * address of its first request and limited to {@link Config#getRateLimit()} requests per minute,
 * see {@link TokenGrants}. Tokens are kept in memory only, as their hashes; a restart of the
 * control application invalidates all of them, together with the runs it started.
 * </p>
 *
 * <p>
 * The proxy is switched on by its {@link Config#getProxyUrl() URL}: without one, workspaces get no
 * URL, no token is issued, and the proxy answers every request with 404. It listens on a port of
 * its own ({@link LLMProxyListener}, {@link Config#getListenAddress()},
 * {@link Config#getListenPort()}); the {@link LLMProxyServlet} in the web application answers only
 * if {@link Config#isServletEnabled()} is set.
 * </p>
 */
@ServiceDependencies({
	LLMProviderService.Module.class,
	PersistencyLayer.Module.class,
	ModelService.Module.class,
	WorkspaceService.Module.class,
})
public class LLMProxyService extends ConfiguredManagedClass<LLMProxyService.Config<?>> implements LLMProxy, LLMTokens {

	/**
	 * Configuration of {@link LLMProxyService}.
	 */
	public interface Config<I extends LLMProxyService> extends ConfiguredManagedClass.Config<I> {

		/**
		 * Configuration property name of {@link #getProxyUrl()}.
		 */
		String PROXY_URL = "proxy-url";

		/**
		 * Configuration property name of {@link #getProvider()}.
		 */
		String PROVIDER = "provider";

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
		 * The URL of the proxy as the workspace containers reach it: the
		 * {@link #getListenPort() port of its own}, e.g.
		 * {@code http://host.docker.internal:8091/llm-proxy}, or the {@link LLMProxyServlet}, e.g.
		 * {@code http://host.docker.internal:8080/tl-factory-app/llm-proxy}.
		 *
		 * <p>
		 * It is passed to the workspace containers in {@link LLMProxyService#ENV_BASE_URL}. Empty
		 * switches the proxy off.
		 * </p>
		 *
		 * @see #PROXY_URL
		 */
		@Name(PROXY_URL)
		@Nullable
		String getProxyUrl();

		/**
		 * The name of the Anthropic provider of the {@link LLMProviderService} whose API key, base
		 * URL, version and timeouts the proxy uses.
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@StringDefault("anthropic")
		String getProvider();

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
		 * The proxy is reached there at {@link LLMProxyHandler#PATH}. The port is opened only if
		 * the proxy is switched on by its {@link #getProxyUrl() URL}.
		 * </p>
		 *
		 * @see #LISTEN_PORT
		 */
		@Name(LISTEN_PORT)
		@IntDefault(DEFAULT_LISTEN_PORT)
		int getListenPort();

		/**
		 * Whether the proxy also answers at {@link LLMProxyHandler#PATH} of the web application,
		 * through the port of the control application.
		 *
		 * <p>
		 * Off by default: the port of the control application is the one its users reach, and the
		 * proxy needs not be reachable there. Switched on, a reverse proxy in front of the control
		 * application must keep clients from outside from reaching that path.
		 * </p>
		 *
		 * @see #SERVLET_ENABLED
		 */
		@Name(SERVLET_ENABLED)
		@BooleanDefault(false)
		boolean isServletEnabled();

	}

	/**
	 * Environment variable of a workspace container naming the URL of the proxy, read by Claude
	 * Code as the base URL of the Anthropic API.
	 */
	public static final String ENV_BASE_URL = "ANTHROPIC_BASE_URL";

	/**
	 * Environment variable of a process in a workspace - never of the container - holding its
	 * token, which Claude Code sends as bearer token.
	 */
	public static final String ENV_AUTH_TOKEN = "ANTHROPIC_AUTH_TOKEN";

	/**
	 * What every workspace token starts with.
	 */
	public static final String TOKEN_PREFIX = "tlwst_";

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

	private final TokenGrants _grants;

	private final SourceFilter _sources;

	private LLMProxyListener _listener;

	/**
	 * Creates a {@link LLMProxyService} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public LLMProxyService(InstantiationContext context, Config<?> config) {
		super(context, config);
		try {
			_sources = new SourceFilter(config.getAllowedSources(),
				() -> WorkspaceService.getInstance().getProvider().networkSubnets());
		} catch (ParseException ex) {
			throw new ConfigurationError(
				"Invalid entry in '" + Config.ALLOWED_SOURCES + "' of the LLM proxy: " + ex.getMessage(), ex);
		}
		if (config.getRateLimit() < 0 || config.getListenPort() < 0) {
			throw new ConfigurationError("Negative '" + Config.RATE_LIMIT + "' or '" + Config.LISTEN_PORT
				+ "' of the LLM proxy.");
		}
		_grants = new TokenGrants(Clock.systemUTC(), LLMProxyService::workspaceExists, config.getRateLimit());
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (isEnabled() && getConfig().getListenPort() > 0) {
			InetSocketAddress address =
				new InetSocketAddress(getConfig().getListenAddress().strip(), getConfig().getListenPort());
			try {
				_listener = new LLMProxyListener(address, () -> this);
				Logger.info("LLM proxy listening on " + _listener.getAddress() + ".", LLMProxyService.class);
			} catch (IOException ex) {
				Logger.error("LLM proxy: cannot listen on " + address + ", the workspaces have no LLM access: "
					+ ex.getMessage(), ex, LLMProxyService.class);
			}
		}
	}

	@Override
	protected void shutDown() {
		if (_listener != null) {
			_listener.close();
			_listener = null;
		}
		super.shutDown();
	}

	/**
	 * The address the proxy listens on with a port of its own, {@code null} if it does not.
	 */
	public InetSocketAddress getListenerAddress() {
		LLMProxyListener listener = _listener;
		return listener == null ? null : listener.getAddress();
	}

	/**
	 * The {@link LLMProxy} the {@link LLMProxyServlet} of the web application serves: the
	 * {@link LLMProxyService} if it is running and {@link Config#isServletEnabled() serves the
	 * servlet}, {@code null} otherwise.
	 */
	public static LLMProxy servletProxy() {
		LLMProxyService service = getInstance();
		return service != null && service.getConfig().isServletEnabled() ? service : null;
	}

	@Override
	public boolean isEnabled() {
		String url = getConfig().getProxyUrl();
		return url != null && !url.isBlank();
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
	 * The environment variables a workspace container gets for the proxy: the URL of the proxy, no
	 * token.
	 *
	 * @return The variables, empty if the proxy is not {@link #isEnabled() switched on}.
	 */
	public Map<String, String> containerEnv() {
		if (!isEnabled()) {
			return Collections.emptyMap();
		}
		return Collections.singletonMap(ENV_BASE_URL, getConfig().getProxyUrl().strip());
	}

	/**
	 * Issues a token for a process in a workspace, e.g. an agent run.
	 *
	 * <p>
	 * The token is to be passed only to that process, in {@link #ENV_AUTH_TOKEN}, and
	 * {@link #revoke(String) revoked} when it ends.
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
	public LLMAdmission admit(String token, String remoteAddress) {
		return _grants.admit(token, remoteAddress);
	}

	private static boolean workspaceExists(String workspaceId) {
		return ThreadContext.<Boolean, RuntimeException, RuntimeException> inSystemContext(
			LLMProxyService.class,
			() -> Boolean.valueOf(FactoryModel.findWorkspace(workspaceId) != null)).booleanValue();
	}

	@Override
	public LLMUpstream upstream() {
		LLMProviderService providers = LLMProviderService.getInstance();
		if (providers == null) {
			Logger.warn("LLM proxy: the LLM providers are not running.", LLMProxyService.class);
			return null;
		}
		String name = getConfig().getProvider();
		LLMProviderConfig.Config provider = providers.getConfig().getProviders().get(name);
		if (!(provider instanceof AnthropicProviderConfig.Config)) {
			Logger.warn("LLM proxy: no Anthropic provider '" + name + "' is configured.", LLMProxyService.class);
			return null;
		}
		LLMUpstream result = LLMUpstream.fromProvider((AnthropicProviderConfig.Config) provider);
		if (result == null) {
			Logger.warn("LLM proxy: the provider '" + name + "' has no API key.", LLMProxyService.class);
		}
		return result;
	}

	/**
	 * The {@link LLMProxyService} of the running application, {@code null} if it is not started.
	 */
	public static LLMProxyService getInstance() {
		return Module.INSTANCE.isActive() ? Module.INSTANCE.getImplementationInstance() : null;
	}

	/**
	 * Module of the {@link LLMProxyService}.
	 */
	public static final class Module extends TypedRuntimeModule<LLMProxyService> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<LLMProxyService> getImplementation() {
			return LLMProxyService.class;
		}
	}

}
