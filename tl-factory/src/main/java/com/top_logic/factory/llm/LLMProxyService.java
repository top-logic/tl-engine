/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
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
import com.top_logic.tl.ai.core.access.SecretTokens;
import com.top_logic.tl.ai.core.provider.LLMProviderConfig;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.anthropic.AnthropicProviderConfig;
import com.top_logic.util.model.ModelService;

/**
 * Access of the workspaces to the Anthropic Messages API without an API key in the workspace.
 *
 * <p>
 * A workspace container gets no API key and no token in its environment, only the URL of the
 * {@link LLMProxyServlet} of the control application in {@link #ENV_BASE_URL}. A process that may
 * use the API - an agent run - gets a token of its own, {@link #issue(String, Duration) issued} for
 * its workspace and passed only to that process in {@link #ENV_AUTH_TOKEN}; Claude Code sends it
 * as bearer token. The proxy accepts a request from an {@link Config#getAllowedSources() accepted
 * address} whose token is valid, adds the API key of the {@link Config#getProvider() configured
 * provider} of the {@link LLMProviderService} and forwards the request.
 * </p>
 *
 * <p>
 * A token is {@value #TOKEN_PREFIX} followed by a random secret of about 256 bits (see
 * {@link SecretTokens}). It is valid until it is {@link #revoke(String) revoked} - when its run
 * ends -, until its lifetime ends (at most {@link Config#getMaxTokenLifetime()}), or until its
 * workspace is removed. Tokens are kept in memory only, as their hashes; a restart of the control
 * application invalidates all of them, together with the runs it started.
 * </p>
 *
 * <p>
 * The proxy is switched on by its {@link Config#getProxyUrl() URL}: without one, workspaces get no
 * URL, no token is issued, and the servlet answers every request with 404.
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
		 * The URL of the {@link LLMProxyServlet} as the workspace containers reach it, e.g.
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
	 * A valid token, by its hash.
	 *
	 * @param workspaceId
	 *        The workspace the token is issued for.
	 * @param expires
	 *        When the token stops being valid.
	 */
	private record Grant(String workspaceId, Instant expires) {
		// Plain data.
	}

	private final Map<String, Grant> _grants = new ConcurrentHashMap<>();

	private final SourceFilter _sources;

	private final Clock _clock = Clock.systemUTC();

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
		Instant now = _clock.instant();
		dropExpired(now);
		Duration max = getMaxTokenLifetime();
		Duration effective = lifetime.compareTo(max) > 0 ? max : lifetime;
		String plain = SecretTokens.newToken(TOKEN_PREFIX);
		_grants.put(SecretTokens.hash(plain), new Grant(workspaceId, now.plus(effective)));
		return plain;
	}

	/**
	 * When the given token stops being valid.
	 *
	 * @return The end of its lifetime, {@code null} if it is not valid.
	 */
	@Override
	public Instant expiresAt(String token) {
		if (!SecretTokens.hasFormat(TOKEN_PREFIX, token)) {
			return null;
		}
		Grant grant = _grants.get(SecretTokens.hash(token));
		return grant == null ? null : grant.expires();
	}

	/**
	 * Revokes a token: the next request presenting it is refused.
	 */
	@Override
	public void revoke(String token) {
		if (SecretTokens.hasFormat(TOKEN_PREFIX, token)) {
			_grants.remove(SecretTokens.hash(token));
		}
	}

	/**
	 * Revokes all tokens issued for the given workspace, e.g. when it is removed.
	 */
	public void revokeAll(String workspaceId) {
		_grants.values().removeIf(grant -> grant.workspaceId().equals(workspaceId));
	}

	private void dropExpired(Instant now) {
		_grants.values().removeIf(grant -> !now.isBefore(grant.expires()));
	}

	@Override
	public boolean isAllowedSource(String remoteAddress) {
		return _sources.allows(remoteAddress);
	}

	@Override
	public boolean authenticate(String token) {
		if (!SecretTokens.hasFormat(TOKEN_PREFIX, token)) {
			return false;
		}
		String hash = SecretTokens.hash(token);
		Grant grant = _grants.get(hash);
		if (grant == null) {
			return false;
		}
		if (!_clock.instant().isBefore(grant.expires())) {
			_grants.remove(hash, grant);
			return false;
		}
		boolean exists = ThreadContext.<Boolean, RuntimeException, RuntimeException> inSystemContext(
			LLMProxyService.class,
			() -> Boolean.valueOf(FactoryModel.findWorkspace(grant.workspaceId()) != null)).booleanValue();
		if (!exists) {
			_grants.remove(hash, grant);
		}
		return exists;
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
