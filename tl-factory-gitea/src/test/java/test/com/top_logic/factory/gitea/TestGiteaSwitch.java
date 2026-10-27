/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.gitea;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.git.GitHostingService;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.server.ServerProviderConfig;

/**
 * Test of switching git hosting on and off by the {@link ServerProviderConfig#getBaseUrl() base URL}
 * of a configured {@link GiteaHostingProvider}.
 */
public class TestGiteaSwitch extends TestCase {

	/**
	 * A provider without base URL leaves the service disabled.
	 */
	public void testEmptyBaseUrl() {
		assertFalse(service("").isEnabled());
		assertFalse(service("  ").isEnabled());
		assertFalse(service(null).isEnabled());
	}

	/**
	 * A provider with base URL enables the service, without contacting the server.
	 */
	public void testBaseUrl() {
		GitHostingService service = service("http://localhost:1/");
		assertTrue(service.isEnabled());
		assertTrue(service.getProvider() instanceof GiteaHostingProvider);
	}

	private static GitHostingService service(String baseUrl) {
		GiteaHostingProvider.Config<?> provider = TypedConfiguration.newConfigItem(GiteaHostingProvider.Config.class);
		if (baseUrl != null) {
			TypedConfigUtil.setProperty(provider, GiteaHostingProvider.Config.BASE_URL, baseUrl);
		}
		TypedConfigUtil.setProperty(provider, GiteaHostingProvider.Config.OWNER, GiteaTestServer.ORG);
		GitHostingService.Config<?> config = TypedConfiguration.newConfigItem(GitHostingService.Config.class);
		TypedConfigUtil.setProperty(config, GitHostingService.Config.PROVIDER, provider);
		return (GitHostingService) TypedConfigUtil.createInstance(config);
	}

	/**
	 * The suite of this test.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(new TestSuite(TestGiteaSwitch.class), TypeIndex.Module.INSTANCE,
			SecretStoreService.Module.INSTANCE);
	}

}
