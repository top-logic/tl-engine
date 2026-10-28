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
import com.top_logic.factory.git.GitConnectionKey;
import com.top_logic.factory.git.GitHostingProvider;
import com.top_logic.factory.git.GitHostingService;
import com.top_logic.factory.git.GitHostingService.BuiltInConnection;
import com.top_logic.factory.git.I18NConstants;
import com.top_logic.factory.gitea.GiteaHostingProvider;
import com.top_logic.factory.gitea.GiteaProviderType;
import com.top_logic.util.error.TopLogicException;

/**
 * Test of the Gitea type in the {@link GitHostingService}: a built-in connection to Gitea is
 * switched on by its URL, and a connection gets a {@link GiteaHostingProvider} without contacting
 * the server.
 */
public class TestGiteaSwitch extends TestCase {

	private static final String CONNECTION = "gitea";

	/**
	 * A built-in connection without URL is switched off.
	 */
	public void testEmptyUrl() {
		for (String url : new String[] { "", "  ", null }) {
			GitHostingService service = service(url);
			assertTrue(service.getBuiltInConnections().isEmpty());
			assertNull(service.getBuiltInConnection(CONNECTION));
		}
	}

	/**
	 * A built-in connection with URL is switched on; its provider is a {@link GiteaHostingProvider}
	 * of the given owner, created once per key.
	 */
	public void testUrl() {
		GitHostingService service = service("http://localhost:1/");
		assertEquals(1, service.getBuiltInConnections().size());
		assertEquals(java.util.List.of(GiteaProviderType.NAME), service.getProviderTypeNames());
		GitConnectionKey key = new GitConnectionKey("1", GiteaProviderType.NAME, "http://localhost:1/",
			GiteaTestServer.ORG + "-t1", CONNECTION, null, true);
		GitHostingProvider provider = service.provider(key);
		assertTrue(provider instanceof GiteaHostingProvider);
		assertEquals(GiteaTestServer.ORG + "-t1", provider.getOwner());
		assertSame(provider, service.provider(key));
		service.forget("1");
		assertNotSame(provider, service.provider(key));
		assertNull("Unknown type.", service.provider(new GitConnectionKey("2", "other", "http://localhost:1/",
			GiteaTestServer.ORG, null, "secret", false)));
	}

	/**
	 * A URL that is no URL of a server is refused when the provider is created.
	 */
	public void testInvalidUrl() {
		GitHostingService service = service("http://localhost:1/");
		try {
			service.provider(new GitConnectionKey("3", GiteaProviderType.NAME, "not a url", GiteaTestServer.ORG,
				null, "secret", false));
			fail("Invalid URL accepted.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_INVALID_BASE_URL__URL.fill("not a url"), ex.getErrorKey());
		}
	}

	private static GitHostingService service(String url) {
		GiteaProviderType.Config<?> type = TypedConfiguration.newConfigItem(GiteaProviderType.Config.class);
		type.setName(GiteaProviderType.NAME);
		BuiltInConnection connection = TypedConfiguration.newConfigItem(BuiltInConnection.class);
		connection.setName(CONNECTION);
		TypedConfigUtil.setProperty(connection, BuiltInConnection.PROVIDER_TYPE, GiteaProviderType.NAME);
		TypedConfigUtil.setProperty(connection, BuiltInConnection.OWNER, GiteaTestServer.ORG);
		TypedConfigUtil.setProperty(connection, BuiltInConnection.TOKEN_SECRET, GiteaProviderType.TOKEN_SECRET);
		if (url != null) {
			TypedConfigUtil.setProperty(connection, BuiltInConnection.URL, url);
		}
		GitHostingService.Config<?> config = TypedConfiguration.newConfigItem(GitHostingService.Config.class);
		config.getProviderTypes().put(GiteaProviderType.NAME, type);
		config.getConnections().put(CONNECTION, connection);
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
