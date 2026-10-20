/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.react.resource.ClientResources;
import com.top_logic.layout.react.resource.ResourceConfig;

/**
 * The start pages and client resources of the assembled demo application, which depends on
 * tl-chat-classic and still starts on its React UI.
 */
public class TestDemoHostConfig extends TestCase {

	/**
	 * The application's own {@code ApplicationPages} entry wins over the classic pages
	 * tl-chat-classic sets for a classic host.
	 */
	public void testApplicationPagesOfHostWin() {
		ApplicationPages.Config pages = ApplicationPages.getInstance();

		assertEquals("/view/", pages.getStartPage());
		assertEquals("/view/", pages.getLoginPage());
		assertEquals("/view/", pages.getLoginRetrySSOPage());
		assertEquals("/view/", pages.getLogoutPage());
		assertEquals("/view/unknown-account.view.xml", pages.getUnknownAccountPage());
	}

	/**
	 * The chat bundle comes from tl-chat, and the demo's own stylesheet joins it in the same list.
	 */
	public void testClientResources() throws Exception {
		ClientResources.Config config = (ClientResources.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(ClientResources.class);
		Set<String> names = config.getResources().stream()
			.map(ResourceConfig::getName)
			.collect(Collectors.toSet());

		assertTrue(names.toString(), names.contains("tl-react-agent-chat"));
		assertTrue(names.toString(), names.contains("tl-react-agent-chat-css"));
		assertTrue(names.toString(), names.contains("tl-ai-demo-shell-css"));
	}

	/** Loads the assembled application configuration around the test. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestDemoHostConfig.class);
	}
}
