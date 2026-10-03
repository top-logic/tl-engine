/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.classic;

import java.util.Map;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.base.accesscontrol.ApplicationPages;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.react.resource.ClientResources;
import com.top_logic.layout.react.resource.ModuleScriptConfig;
import com.top_logic.layout.react.resource.ResourceConfig;
import com.top_logic.layout.react.resource.StyleSheetConfig;

/**
 * What a classic application gets from this module with no configuration of its own: the
 * configuration of this module and of everything below it, as a host that only adds the
 * dependency loads it.
 */
public class TestClassicHostConfig extends TestCase {

	private static final String LAYOUT_SERVLET = "/servlet/LayoutServlet";

	/**
	 * The start and login pages are the classic ones of tl-core, not the {@code /view/} pages
	 * tl-layout-view sets (workaround E5).
	 */
	public void testClassicApplicationPages() {
		ApplicationPages.Config pages = ApplicationPages.getInstance();

		assertEquals(LAYOUT_SERVLET, pages.getStartPage());
		assertEquals(LAYOUT_SERVLET, pages.getLoginPage());
		assertEquals(LAYOUT_SERVLET, pages.getLoginRetrySSOPage());
		assertEquals(LAYOUT_SERVLET, pages.getLogoutPage());
		assertEquals("/jsp/main/LoginErrorPage_unknownAccount.jsp", pages.getUnknownAccountPage());
	}

	/**
	 * The client bundle of the chat is registered by tl-chat, so the Albert tab can render its
	 * controls.
	 */
	public void testChatBundleRegistered() throws Exception {
		ClientResources.Config config = (ClientResources.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(ClientResources.class);
		Map<String, ResourceConfig> byName = config.getResources().stream()
			.collect(Collectors.toMap(ResourceConfig::getName, r -> r));

		ResourceConfig script = byName.get("tl-react-agent-chat");
		assertTrue("chat bundle not registered: " + byName.keySet(), script instanceof ModuleScriptConfig);
		assertEquals("/script/tl-chat-ui.js", ((ModuleScriptConfig) script).getResource());

		ResourceConfig css = byName.get("tl-react-agent-chat-css");
		assertTrue("chat stylesheet not registered: " + byName.keySet(), css instanceof StyleSheetConfig);
		assertEquals("/script/tl-chat-ui.css", ((StyleSheetConfig) css).getResource());
	}

	/** Loads the module's assembled configuration around the test. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestClassicHostConfig.class);
	}
}
