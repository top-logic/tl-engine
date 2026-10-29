/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.util.Map;
import java.util.Objects;

import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * An immutable snapshot of the model catalog of {@link LLMProviderService}: the metadata of every
 * model it knows, and the state of the catalog these were taken from.
 *
 * <p>
 * The service replaces its snapshot as a whole, so the models and the status of one snapshot always
 * belong together.
 * </p>
 *
 * @param models
 *        The metadata by model name: the catalog entries of the configured models, with the model
 *        overrides applied. A configured model the catalog does not list and no override describes
 *        has no entry.
 * @param status
 *        The state of the catalog.
 *
 * @see LLMProviderService#catalog()
 */
public record ModelCatalog(Map<String, ResolvedModel> models, CatalogStatus status) {

	/**
	 * Creates a {@link ModelCatalog}.
	 */
	public ModelCatalog {
		models = Map.copyOf(models);
		Objects.requireNonNull(status, "status");
	}

	/**
	 * The metadata of the given model, or <code>null</code> when the catalog has none.
	 */
	public ResolvedModel get(String modelName) {
		return modelName == null ? null : models.get(modelName);
	}

	/**
	 * This catalog in the given state, with the same models.
	 */
	public ModelCatalog withState(CatalogStatus.State state, String failure) {
		return new ModelCatalog(models, status.withState(state, failure));
	}
}
