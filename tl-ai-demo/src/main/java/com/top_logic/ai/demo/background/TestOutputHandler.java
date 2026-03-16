/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo.background;

import com.top_logic.ai.service.agents.background.context.BackgroundContext;
import com.top_logic.ai.service.agents.background.output.OutputHandlerImplementation;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;

/**
 * Sample output handler that logs the raw LLM response to the application log.
 *
 * <p>
 * Demonstrates how to implement a custom Java output handler using
 * {@link OutputHandlerImplementation}.
 * </p>
 */
public class TestOutputHandler extends OutputHandlerImplementation {

	/**
	 * Creates a new {@link TestOutputHandler}.
	 */
	@CalledByReflection
	public TestOutputHandler(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void handleOutput(String response, BackgroundContext context) {
		String agentName = context.getAgentName();
		if (response == null || response.isEmpty()) {
			Logger.info("TestOutputHandler [" + agentName + "]: empty response", TestOutputHandler.class);
		} else {
			String summary = response.length() > 500 ? response.substring(0, 497) + "..." : response;
			Logger.info("TestOutputHandler [" + agentName + "]: " + summary, TestOutputHandler.class);
		}
	}
}
