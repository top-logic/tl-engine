/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.jenkins;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.ci.CIService;
import com.top_logic.factory.jenkins.JenkinsCIProvider;
import com.top_logic.factory.server.ServerProviderConfig;

/**
 * Test of switching CI on and off by the {@link ServerProviderConfig#getBaseUrl() base URL}
 * of a configured {@link JenkinsCIProvider}.
 */
public class TestJenkinsSwitch extends TestCase {

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
		CIService service = service("http://localhost:1/");
		assertTrue(service.isEnabled());
		assertTrue(service.getProvider() instanceof JenkinsCIProvider);
	}

	private static CIService service(String baseUrl) {
		JenkinsCIProvider.Config<?> provider = TypedConfiguration.newConfigItem(JenkinsCIProvider.Config.class);
		if (baseUrl != null) {
			TypedConfigUtil.setProperty(provider, JenkinsCIProvider.Config.BASE_URL, baseUrl);
		}
		TypedConfigUtil.setProperty(provider, JenkinsCIProvider.Config.USER, JenkinsTestServer.ADMIN);
		CIService.Config<?> config = TypedConfiguration.newConfigItem(CIService.Config.class);
		TypedConfigUtil.setProperty(config, CIService.Config.PROVIDER, provider);
		return (CIService) TypedConfigUtil.createInstance(config);
	}

	/**
	 * The suite of this test.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(new TestSuite(TestJenkinsSwitch.class), TypeIndex.Module.INSTANCE,
			SecretStoreService.Module.INSTANCE);
	}

}
