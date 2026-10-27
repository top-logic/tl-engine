/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.server;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.git.GitHostingService;

/**
 * Configuration of a provider that talks to a server at a {@link #getBaseUrl() base URL}, e.g. a
 * git hosting service or a CI server.
 *
 * <p>
 * A provider configured without base URL is switched off: a service holding such a provider, e.g.
 * {@link GitHostingService} or {@link CIService}, does not create it and is disabled, as if no
 * provider were configured (see {@link #isSwitchedOn(PolymorphicConfiguration)}). This lets an
 * application configure the provider with its settings taken from the environment and switch it on
 * by setting the URL of the server alone.
 * </p>
 */
public interface ServerProviderConfig<I> extends PolymorphicConfiguration<I> {

	/**
	 * Configuration property name of {@link #getBaseUrl()}.
	 */
	String BASE_URL = "base-url";

	/**
	 * The URL of the server; empty switches the provider off.
	 *
	 * @see #BASE_URL
	 */
	@Name(BASE_URL)
	@Nullable
	String getBaseUrl();

	/**
	 * Whether the given provider configuration is set and switched on: it is not a
	 * {@link ServerProviderConfig}, or one with a non-blank {@link #getBaseUrl() base URL}.
	 *
	 * @param config
	 *        The provider configuration, {@code null} for none.
	 */
	static boolean isSwitchedOn(PolymorphicConfiguration<?> config) {
		if (config == null) {
			return false;
		}
		if (config instanceof ServerProviderConfig<?> server) {
			String baseUrl = server.getBaseUrl();
			return baseUrl != null && !baseUrl.isBlank();
		}
		return true;
	}

}
