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
 * <b>What the assertion proves.</b> The <em>product</em> default allowlist is deliberately empty
 * (fail-closed), so its content can prove nothing; asserting it was non-empty would also break the
 * build for any operator who empties the allowlist to get exactly that documented behaviour. The
 * demo instead allowlists its validation target through an <em>override without a class
 * attribute</em> in {@code tl-ai-demo.conf.config.xml}. Such a fragment can only resolve against the
 * {@link BrowserToolsService} instance declared in {@code tl-ai-core.conf.config.xml}, so the value
 * arriving here proves both files are on the demo's configuration load path.
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

		// Reachability, not policy: the demo's class-less override can only apply on top of the
		// instance tl-ai-core.conf.config.xml declares, so seeing its value here proves both files
		// are assembled. The PRODUCT default stays an empty, fail-closed allowlist, which is why
		// nothing here asserts that the allowlist is non-empty in general.
		assertTrue("the demo's allowed-domains override did not reach the assembled service "
			+ "configuration - tl-ai-core.conf.config.xml and/or tl-ai-demo.conf.config.xml is NOT on "
			+ "the demo's config load path. Effective allowlist: " + config.getAllowedDomains(),
			config.getAllowedDomains().contains("quotes.toscrape.com"));
	}

	/** Loads the assembled application configuration around the test. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestBrowserToolsServiceDemoConfig.class);
	}
}
