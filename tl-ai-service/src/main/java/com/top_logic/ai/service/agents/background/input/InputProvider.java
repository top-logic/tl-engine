/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Pluggable provider for gathering background agent input.
 *
 * <p>
 * Implementations are instantiated from configuration and provide a custom input gathering
 * strategy. Use {@link ProviderInput} to wire an {@link InputProvider} into a background agent
 * configuration.
 * </p>
 */
public interface InputProvider extends ConfiguredInstance<InputProvider.Config> {

	/**
	 * Configuration for {@link InputProvider} implementations.
	 */
	interface Config extends PolymorphicConfiguration<InputProvider> {
		// Marker configuration — concrete implementations add their own properties.
	}

	/**
	 * Produces the input string for the background agent.
	 *
	 * @param context
	 *        The background execution context.
	 * @return The input string, never {@code null}.
	 */
	String provide(BackgroundContext context);
}
