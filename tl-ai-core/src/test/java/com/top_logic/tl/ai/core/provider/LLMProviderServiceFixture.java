/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import com.top_logic.basic.config.InstantiationContext;

/**
 * Starts and stops an {@link LLMProviderService} outside the module system, for a test in another package
 * that builds its configuration by hand.
 */
public final class LLMProviderServiceFixture {

	private LLMProviderServiceFixture() {
		// Static utility.
	}

	/**
	 * Starts a service on the given configuration.
	 */
	public static LLMProviderService start(InstantiationContext context, LLMProviderService.Config<?> config) {
		LLMProviderService service = new LLMProviderService(context, config);
		service.startUp();
		return service;
	}

	/**
	 * Stops a service {@link #start(InstantiationContext, LLMProviderService.Config) started} here.
	 */
	public static void stop(LLMProviderService service) {
		service.shutDown();
	}
}
