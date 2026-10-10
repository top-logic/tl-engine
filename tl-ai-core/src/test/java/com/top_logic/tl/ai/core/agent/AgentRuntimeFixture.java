/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.basic.config.InstantiationContext;

/**
 * Starts and stops an {@link AgentRuntimeService} outside the module system, for a test that builds
 * its configuration by hand - the way the provider tests start {@code LLMProviderService}.
 */
public final class AgentRuntimeFixture {

	private AgentRuntimeFixture() {
		// Static utility.
	}

	/**
	 * Starts a service on the given configuration; the {@code LLMProviderService} it reads is the one started
	 * last, if any.
	 */
	public static AgentRuntimeService start(InstantiationContext context, AgentRuntimeService.Config<?> config) {
		AgentRuntimeService service = new AgentRuntimeService(context, config);
		service.startUp();
		return service;
	}

	/**
	 * Stops a service {@link #start(InstantiationContext, AgentRuntimeService.Config) started} here.
	 */
	public static void stop(AgentRuntimeService service) {
		service.shutDown();
	}
}
