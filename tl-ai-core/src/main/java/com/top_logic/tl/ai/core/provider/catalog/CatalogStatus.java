/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.catalog;

import java.time.Instant;
import java.util.Objects;

import com.top_logic.tl.ai.core.provider.LLMProviderService;

/**
 * The state of the model catalog of {@link LLMProviderService}.
 *
 * @param state
 *        Where loading the remote catalog stands.
 * @param catalogUrl
 *        The URL of the remote catalog, or <code>null</code> when the service has no catalog
 *        configuration.
 * @param installedAt
 *        When the metadata the service currently answers with was installed.
 * @param failure
 *        Why loading the remote catalog failed, in the state {@link State#FAILED};
 *        <code>null</code> otherwise.
 *
 * @see LLMProviderService#catalogStatus()
 */
public record CatalogStatus(State state, String catalogUrl, Instant installedAt, String failure) {

	/**
	 * Creates a {@link CatalogStatus}.
	 */
	public CatalogStatus {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(installedAt, "installedAt");
	}

	/**
	 * The same status in the given state, with the given failure detail.
	 */
	public CatalogStatus withState(State newState, String newFailure) {
		return new CatalogStatus(newState, catalogUrl, installedAt, newFailure);
	}

	/**
	 * Where loading the remote catalog stands.
	 */
	public enum State {

		/**
		 * No remote catalog is loaded in this start of the service: it is
		 * {@link LLMProviderService.ModelCatalogConfig#getFetchRemoteCatalog() switched off}, or no
		 * provider asks the catalog for a model. The bundled catalog is in use.
		 */
		DISABLED,

		/**
		 * The remote catalog is being loaded in the background. The bundled catalog is in use
		 * meanwhile.
		 */
		LOADING,

		/**
		 * The remote catalog has been loaded and is in use, completed by the bundled catalog for the
		 * models it does not list.
		 */
		LOADED,

		/**
		 * Loading the remote catalog failed. The bundled catalog stays in use.
		 */
		FAILED;
	}
}
