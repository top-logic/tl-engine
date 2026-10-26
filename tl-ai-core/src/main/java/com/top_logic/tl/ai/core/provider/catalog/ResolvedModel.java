/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.util.Objects;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * The metadata {@link LLMProviderService} uses for one model, together with where it comes from.
 *
 * @param info
 *        The metadata of the model.
 * @param origin
 *        The source of {@link #info}.
 *
 * @see LLMProviderService#getCatalogEntry(String)
 */
public record ResolvedModel(ModelInfo info, Origin origin) {

	/**
	 * Creates a {@link ResolvedModel}.
	 */
	public ResolvedModel {
		Objects.requireNonNull(info, "info");
		Objects.requireNonNull(origin, "origin");
	}

	/**
	 * The source of the metadata of a {@link ResolvedModel}.
	 */
	public enum Origin {

		/**
		 * The copy of the <a href="https://models.dev">models.dev</a> catalog bundled with the
		 * application.
		 *
		 * @see ModelsDevFetcher#loadSnapshot(java.util.Map)
		 */
		@Label("Bundled catalog")
		BUNDLED,

		/**
		 * The remote catalog loaded from the configured
		 * {@link LLMProviderService.ModelCatalogConfig#getCatalogUrl() catalog URL}.
		 */
		@Label("Remote catalog")
		REMOTE,

		/**
		 * An entry of the {@link LLMProviderService.ModelCatalogConfig#getModelOverrides() model
		 * overrides}, which takes precedence over a catalog entry of the same model.
		 */
		@Label("Model override")
		OVERRIDE;
	}
}
