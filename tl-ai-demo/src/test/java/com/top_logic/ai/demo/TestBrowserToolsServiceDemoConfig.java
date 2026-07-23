/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.tl.ai.core.tools.web.BrowserToolsService;

/**
 * Boots the assembled tl-ai-demo application configuration (the same typed-config load path the
 * running app uses) and prints/asserts the <em>effective</em> {@link BrowserToolsService}
 * configuration.
 *
 * <p>
 * The purpose is to answer a single question: when {@code evade-bot-detection} is set in
 * {@code tl-ai-core/WEB-INF/conf/tl-ai-core.conf.config.xml}, does the demo's config assembly
 * actually yield {@code isEvadeBotDetection() == true}? It also reports the effective
 * allowlist / allow-any / navigation-settle so the browser tool's real policy in the demo is
 * visible.
 * </p>
 *
 * <p>
 * The configuration is loaded via {@link ModuleTestSetup} (which loads the application's assembled
 * typed configuration), and the {@link BrowserToolsService} configuration is read straight from
 * {@link ApplicationConfig} - the service itself is <em>not</em> started, so no browser is
 * launched.
 * </p>
 */
public class TestBrowserToolsServiceDemoConfig extends TestCase {

	/** Prints and asserts the effective browser-tool configuration of the assembled demo app. */
	public void testEffectiveBrowserConfig() throws Exception {
		BrowserToolsService.Config<?> config =
			(BrowserToolsService.Config<?>) ApplicationConfig.getInstance()
				.getServiceConfiguration(BrowserToolsService.class);

		System.out.println("=== EFFECTIVE tl-ai-demo BrowserToolsService config ===");
		System.out.println("evade-bot-detection    = " + config.isEvadeBotDetection());
		System.out.println("allow-any-domains      = " + config.isAllowAnyDomain());
		System.out.println("allowed-domains        = " + config.getAllowedDomains());
		System.out.println("blocked-domains        = " + config.getBlockedDomains());
		System.out.println("navigation-settle-ms   = " + config.getNavigationSettleMillis());
		System.out.println("navigation-timeout-s   = " + config.getNavigationTimeoutSeconds());
		System.out.println("=======================================================");

		// The allowlist is proof the core config file reaches the demo: its default is the empty
		// list, so a non-empty value can only come from tl-ai-core.conf.config.xml.
		assertFalse(
			"allowed-domains is empty: tl-ai-core.conf.config.xml is NOT on the demo's config load path.",
			config.getAllowedDomains().isEmpty());
	}

	/** Loads the assembled application configuration around the test. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestBrowserToolsServiceDemoConfig.class);
	}
}
