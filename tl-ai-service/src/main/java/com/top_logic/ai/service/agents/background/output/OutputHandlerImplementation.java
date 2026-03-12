/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.output;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Base class for custom Java output handler implementations.
 *
 * <p>
 * Subclasses receive the raw LLM response and {@link BackgroundContext} and can perform arbitrary
 * processing (logging, notifications, persistence, etc.).
 * </p>
 */
public abstract class OutputHandlerImplementation
		extends AbstractConfiguredInstance<OutputHandlerImplementation.Config<?>> {

	/**
	 * Configuration for {@link OutputHandlerImplementation}.
	 */
	public interface Config<I extends OutputHandlerImplementation>
			extends OutputHandler, PolymorphicConfiguration<I> {
		// No additional properties — subclasses extend this with their own config.
	}

	/**
	 * Creates a new {@link OutputHandlerImplementation}.
	 */
	protected OutputHandlerImplementation(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * Handles the output of a background agent.
	 *
	 * @param response
	 *        The raw LLM response string; may be {@code null} if the agent failed.
	 * @param context
	 *        The background execution context.
	 */
	public abstract void handleOutput(String response, BackgroundContext context);
}
