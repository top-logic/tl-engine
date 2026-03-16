/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background.input;

import java.time.Instant;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Sample {@link InputProvider} that returns a simple test message with a timestamp.
 *
 * <p>
 * Demonstrates how to implement a custom Java input provider for background agents.
 * </p>
 */
public class TestInputProvider implements InputProvider {

	private final Config _config;

	/**
	 * Creates a new {@link TestInputProvider}.
	 */
	@CalledByReflection
	public TestInputProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public String provide(BackgroundContext context) {
		return "Test input from Java provider for agent '" + context.getAgentName()
			+ "' at " + Instant.now();
	}
}
