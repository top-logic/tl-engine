/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Factory contract for LLM provider instances.
 *
 * <p>
 * Each concrete implementation (Anthropic, OpenAI, Mistral, xAI, etc.) defines its own nested
 * {@link Config} with provider-specific properties and knows how to create its
 * {@link LLMProvider} and discover available models.
 * </p>
 *
 * @see LLMProviderRegistry
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

		/**
		 * Allowed model names for this provider.
		 *
		 * <p>
		 * If non-empty, only these models are exposed. If empty, all models available from the
		 * provider are exposed.
		 * </p>
		 */
		@Name(MODELS)
		@Format(CommaSeparatedStrings.class)
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
