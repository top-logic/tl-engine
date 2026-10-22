/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.credential.CredentialProvider;
import com.top_logic.basic.credential.HeaderCredentials;
import com.top_logic.basic.credential.NoCredentials;
import com.top_logic.basic.credential.SecretSource;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;

/**
 * Factory contract for LLM provider instances.
 *
 * <p>
 * Each concrete implementation (Anthropic, <code>OpenAI</code>, Mistral, <code>xAI</code>, etc.) defines its own nested
 * {@link Config} with provider-specific properties and knows how to create its
 * {@link LLMProvider} and discover available models.
 * </p>
 *
 * @see LLMProviderService
 */
public interface LLMProviderConfig {

	/**
	 * Base configuration shared by all provider types.
	 *
	 * <p>
	 * Provides a {@link #getName() name} for registry keying and logging, plus
	 * an optional {@link #getModels() models} allowlist.
	 * </p>
	 */
	interface Config extends PolymorphicConfiguration<LLMProviderConfig>, NamedConfigMandatory {

		/** Configuration property name for {@link #getModels()}. */
		String MODELS = "models";

		/** Configuration property name for {@link #getCatalogProviderId()}. */
		String CATALOG_PROVIDER_ID = "catalog-provider-id";

		/** Configuration property name for {@link #getConnectTimeout()}. */
		String CONNECT_TIMEOUT = "connect-timeout";

		/** Configuration property name for {@link #getRequestTimeout()}. */
		String REQUEST_TIMEOUT = "request-timeout";

		/** Configuration property name for {@link #getCustomHeaders()}. */
		String CUSTOM_HEADERS = "custom-headers";

		/** Configuration property name for {@link #getBaseUrl()}. */
		String BASE_URL = "base-url";

		/** Configuration property name for {@link #getCredentials()}. */
		String CREDENTIALS = "credentials";

		/**
		 * Model names enabled for this provider.
		 *
		 * <p>
		 * Only models listed here are available for use. If empty, no models are exposed from
		 * this provider.
		 * </p>
		 */
		@Name(MODELS)
		@Format(CommaSeparatedStrings.class)
		@PropertyEditor(ModelsEditor.class)
		List<String> getModels();

		/**
		 * The provider ID used to look up model metadata from the remote catalog (models.dev).
		 *
		 * <p>
		 * Each provider supplies a sensible default matching its models.dev entry. Leave empty
		 * for providers that have no remote catalog (e.g. local servers) — their models must be
		 * defined in the model catalog manually.
		 * </p>
		 */
		@Name(CATALOG_PROVIDER_ID)
		@Nullable
		String getCatalogProviderId();

		/**
		 * TCP connection timeout in seconds.
		 *
		 * <p>
		 * Controls how long the HTTP client waits to establish a TCP connection to the provider
		 * endpoint before giving up.
		 * </p>
		 */
		@Name(CONNECT_TIMEOUT)
		@IntDefault(30)
		int getConnectTimeout();

		/**
		 * Total streaming request timeout in seconds.
		 *
		 * <p>
		 * Maximum wall-clock time allowed for a complete streaming request, including the time
		 * spent receiving streamed tokens. A generous default accommodates long-running agent
		 * responses.
		 * </p>
		 */
		@Name(REQUEST_TIMEOUT)
		@IntDefault(600)
		int getRequestTimeout();

		/**
		 * Extra HTTP headers sent on every request to this provider.
		 *
		 * <p>
		 * Useful for custom authentication schemes, routing headers, or provider-specific
		 * metadata that must accompany each API call.
		 * </p>
		 */
		@Name(CUSTOM_HEADERS)
		@MapBinding(tag = "entry", key = "name", attribute = "value")
		Map<String, String> getCustomHeaders();

		/**
		 * The base URL of the provider API endpoint.
		 *
		 * @see #BASE_URL
		 */
		@Name(BASE_URL)
		String getBaseUrl();

		/**
		 * How the requests to the provider are authenticated.
		 *
		 * <p>
		 * E.g. an access token from an OAuth2 authorization server, the managed identity of the
		 * cloud environment, or no credentials at all, when a gateway in between adds them. If not
		 * set, the requests are authenticated with the API key of the provider, if one is
		 * configured. Configuring both is an error.
		 * </p>
		 */
		@Name(CREDENTIALS)
		@Nullable
		PolymorphicConfiguration<? extends CredentialProvider> getCredentials();

		/** @see #getCredentials() */
		void setCredentials(PolymorphicConfiguration<? extends CredentialProvider> value);
	}

	/**
	 * Creates the {@link CredentialProvider} authenticating the requests of a provider without
	 * API key.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param config
	 *        The provider configuration.
	 * @return The configured {@link Config#getCredentials() credentials}, or
	 *         {@link NoCredentials}.
	 */
	static CredentialProvider createCredentials(InstantiationContext context, Config config) {
		return createCredentials(context, config, null, null, null);
	}

	/**
	 * Creates the {@link CredentialProvider} authenticating the requests of a provider.
	 *
	 * @param context
	 *        The context for instantiating the credentials.
	 * @param config
	 *        The provider configuration.
	 * @param apiKey
	 *        The API key of the provider, <code>null</code> if none is configured.
	 * @param header
	 *        The request header in which the provider expects the API key.
	 * @param scheme
	 *        The authentication scheme written before the API key, <code>null</code> for sending
	 *        the bare key.
	 * @return The configured {@link Config#getCredentials() credentials}; if there are none, the
	 *         API key in the given header; without API key, {@link NoCredentials}.
	 * @throws ConfigurationError
	 *         If both credentials and an API key are configured.
	 */
	static CredentialProvider createCredentials(InstantiationContext context, Config config,
			PolymorphicConfiguration<? extends SecretSource> apiKey, String header, String scheme) {
		PolymorphicConfiguration<? extends CredentialProvider> credentials = config.getCredentials();
		if (credentials != null) {
			if (apiKey != null) {
				throw new ConfigurationError(
					I18NConstants.ERROR_CREDENTIALS_AND_API_KEY__PROVIDER.fill(config.getName()));
			}
			return context.getInstance(credentials);
		}
		if (apiKey != null) {
			return context.getInstance(HeaderCredentials.config(header, scheme, apiKey));
		}
		return context.getInstance(TypedConfiguration.newConfigItem(NoCredentials.Config.class));
	}

	/**
	 * Creates a ready-to-use {@link LLMProvider} from this configuration.
	 */
	LLMProvider createProvider();

	/**
	 * Discovers available models from the provider's API.
	 *
	 * <p>
	 * Returns an empty list if model discovery is not supported (e.g. Anthropic).
	 * </p>
	 */
	List<String> discoverModels();

	/**
	 * Returns the explicitly configured model allowlist from the configuration.
	 */
	List<String> getConfiguredModels();

	/**
	 * Returns the catalog provider ID from configuration.
	 */
	String getCatalogProviderId();
}
