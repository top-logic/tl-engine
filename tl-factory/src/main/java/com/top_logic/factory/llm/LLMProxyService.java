/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.llm;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.factory.project.FactoryModel;
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
 * A workspace container gets no API key. It gets the URL of the {@link LLMProxyServlet} of the
 * control application in {@link #ENV_BASE_URL} and a token of its own in {@link #ENV_AUTH_TOKEN};
 * Claude Code sends the token as bearer token to that URL. The proxy accepts a request whose token
 * belongs to an existing workspace, adds the API key of the {@link Config#getProvider()
 * configured provider} of the {@link LLMProviderService} and forwards the request.
 * </p>
 *
 * <p>
 * A token is {@value #TOKEN_PREFIX} followed by a random secret (see {@link SecretTokens}). The
 * workspace stores only its hash ({@link FactoryModel#WORKSPACE_LLM_TOKEN_HASH}); removing the
 * workspace invalidates the token.
 * </p>
 *
 * <p>
 * The proxy is switched on by its {@link Config#getProxyUrl() URL}: without one, new workspaces get
 * neither URL nor token, and the servlet answers every request with 404.
 * </p>
 */
@ServiceDependencies({
	LLMProviderService.Module.class,
	PersistencyLayer.Module.class,
	ModelService.Module.class,
})
public class LLMProxyService extends ConfiguredManagedClass<LLMProxyService.Config<?>> implements LLMProxy {

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
		 * The URL of the {@link LLMProxyServlet} as the workspace containers reach it, e.g.
		 * {@code http://host.docker.internal:8080/tl-factory-app/llm-proxy}.
		 *
		 * <p>
		 * It is passed to new workspaces in {@link LLMProxyService#ENV_BASE_URL}. Empty switches the
		 * proxy off.
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

	}

	/**
	 * Environment variable of a workspace container naming the URL of the proxy, read by Claude
	 * Code as the base URL of the Anthropic API.
	 */
	public static final String ENV_BASE_URL = "ANTHROPIC_BASE_URL";

	/**
	 * Environment variable of a workspace container holding its token, which Claude Code sends as
	 * bearer token.
	 */
	public static final String ENV_AUTH_TOKEN = "ANTHROPIC_AUTH_TOKEN";

	/**
	 * What every workspace token starts with.
	 */
	public static final String TOKEN_PREFIX = "tlwst_";

	/**
	 * A token that was just issued.
	 *
	 * @param plain
	 *        The token, to be passed to the workspace container and stored nowhere else.
	 * @param hash
	 *        What the workspace stores, see {@link FactoryModel#WORKSPACE_LLM_TOKEN_HASH}.
	 */
	public record WorkspaceToken(String plain, String hash) {
		@Override
		public String toString() {
			// Keeps the plain token out of anything that prints this record.
			return "WorkspaceToken[" + hash + "]";
		}
	}

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
	}

	@Override
	public boolean isEnabled() {
		String url = getConfig().getProxyUrl();
		return url != null && !url.isBlank();
	}

	/**
	 * Issues a token for a new workspace.
	 *
	 * @return The token, {@code null} if the proxy is not {@link #isEnabled() switched on}.
	 */
	public WorkspaceToken issueToken() {
		if (!isEnabled()) {
			return null;
		}
		String plain = SecretTokens.newToken(TOKEN_PREFIX);
		return new WorkspaceToken(plain, SecretTokens.hash(plain));
	}

	/**
	 * The environment variables a workspace container gets for the proxy.
	 *
	 * @param token
	 *        The {@link WorkspaceToken#plain() token} of the workspace.
	 */
	public Map<String, String> containerEnv(WorkspaceToken token) {
		Map<String, String> result = new LinkedHashMap<>();
		result.put(ENV_BASE_URL, getConfig().getProxyUrl().strip());
		result.put(ENV_AUTH_TOKEN, token.plain());
		return result;
	}

	@Override
	public boolean authenticate(String token) {
		if (!SecretTokens.hasFormat(TOKEN_PREFIX, token)) {
			return false;
		}
		String hash = SecretTokens.hash(token);
		return ThreadContext.<Boolean, RuntimeException, RuntimeException> inSystemContext(LLMProxyService.class,
			() -> Boolean.valueOf(FactoryModel.findWorkspaceByLLMTokenHash(hash) != null)).booleanValue();
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
