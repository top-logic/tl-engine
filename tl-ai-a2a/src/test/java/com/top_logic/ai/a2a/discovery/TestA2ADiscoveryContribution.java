/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.discovery;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.a2a.A2AService;
import com.top_logic.ai.a2a.A2AService.ExposedAgentConfig;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.service.openapi.server.authentication.conf.EndpointCredential;
import com.top_logic.service.openapi.server.authentication.conf.ServerAuthentication;
import com.top_logic.tl.ai.core.access.AccessTokenAuthentication;
import com.top_logic.tl.ai.core.access.EndpointAuthentication;
import com.top_logic.util.Resources;

/**
 * Tests how the exposed A2A agents are described in <code>llms.txt</code> and the AI Catalog
 * ({@link A2ADiscoveryContribution}).
 */
public class TestA2ADiscoveryContribution extends TestCase {

	private static final String APP = "https://apps.haumacher.de/ehome";

	private static final DiscoveryContext CONTEXT = new DiscoveryContext(APP);

	/**
	 * The texts come from the resource bundles.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestA2ADiscoveryContribution.class));
	}

	/** Every exposed agent gets a catalog entry pointing to its card. */
	public void testCatalogEntries() {
		List<CatalogEntry> entries = A2ADiscoveryContribution.catalogEntries(
			List.of(agent("orders", "Order desk", "Answers questions about orders."), agent("plain", null, null)),
			CONTEXT);

		assertEquals(2, entries.size());
		CatalogEntry orders = entries.get(0);
		assertEquals("urn:air:apps.haumacher.de:a2a:orders", orders.getIdentifier());
		assertEquals(A2ADiscoveryContribution.MEDIA_TYPE, orders.getType());
		assertEquals("application/a2a-agent-card+json", orders.getType());
		assertEquals(APP + "/a2a/orders/agent-card.json", orders.getUrl());
		assertFalse(orders.hasData());
		assertEquals("Order desk", orders.getDisplayName());
		assertEquals("Answers questions about orders.", orders.getDescription());
		assertEquals(A2AService.AGENT_VERSION, orders.getVersion());

		CatalogEntry plain = entries.get(1);
		assertEquals("urn:air:apps.haumacher.de:a2a:plain", plain.getIdentifier());
		assertEquals(APP + "/a2a/plain/agent-card.json", plain.getUrl());
		assertEquals("The path is the display name.", "plain", plain.getDisplayName());
		assertNull(plain.getDescription());
	}

	/** An application URL without a host is published as {@link DiscoveryContext#NO_HOST}. */
	public void testIdentifierWithoutHost() {
		CatalogEntry entry = A2ADiscoveryContribution
			.catalogEntries(List.of(agent("orders", null, null)), new DiscoveryContext("not a url")).get(0);

		assertEquals("urn:air:local:a2a:orders", entry.getIdentifier());
	}

	/**
	 * The <code>llms.txt</code> section lists the agents with card and endpoint, the credentials and
	 * where to create a token.
	 */
	public void testLlmsTxtSection() {
		Resources resources = Resources.getLogInstance();
		List<LlmsTxtSection> sections = A2ADiscoveryContribution.llmsTxtSections(
			List.of(agent("orders", "Order desk", "Answers questions about orders."), agent("plain", null, null)),
			personalAccessTokens(), CONTEXT, resources);

		assertEquals(1, sections.size());
		LlmsTxtSection section = sections.get(0);
		assertEquals(resources.getString(I18NConstants.LLMS_TITLE), section.title());
		String text = section.markdown();
		assertTrue(text, text.contains(resources.getString(I18NConstants.LLMS_INTRO)));
		assertTrue(text, text.contains("[Order desk](" + APP + "/a2a/orders/agent-card.json)"));
		assertTrue(text, text.contains("Answers questions about orders."));
		assertTrue(text, text.contains(APP + "/a2a/orders)"));
		assertTrue(text, text.contains("[plain](" + APP + "/a2a/plain/agent-card.json)"));
		assertTrue(text, text.contains(APP + "/a2a/plain\n"));
		assertTrue(text, text.contains(resources.getString(I18NConstants.CREDENTIALS_INTRO)));
		assertTrue(text, text.contains("\"Authorization: " + resources.getString(
			com.top_logic.tl.ai.core.access.I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_VALUE) + "\""));
		assertTrue(text, text.contains(resources.getString(
			com.top_logic.tl.ai.core.access.I18NConstants.CREDENTIAL_PERSONAL_ACCESS_TOKEN_OBTAIN__URL
				.fill(APP + ViewServlet.ROOT_PATH))));
	}

	/** Without an accepted credential, the section says that no request is served. */
	public void testLlmsTxtSectionWithoutCredentials() {
		Resources resources = Resources.getLogInstance();
		LlmsTxtSection section = A2ADiscoveryContribution
			.llmsTxtSections(List.of(agent("orders", null, null)), List.of(), CONTEXT, resources).get(0);

		assertTrue(section.markdown(), section.markdown().contains(resources.getString(I18NConstants.NO_CREDENTIALS)));
		assertFalse(section.markdown(),
			section.markdown().contains(resources.getString(I18NConstants.CREDENTIALS_INTRO)));
	}

	/** Without exposed agents, nothing is described. */
	public void testNoAgents() {
		assertEquals(List.of(), A2ADiscoveryContribution.catalogEntries(List.of(), CONTEXT));
		assertEquals(List.of(), A2ADiscoveryContribution.llmsTxtSections(List.of(), personalAccessTokens(), CONTEXT,
			Resources.getLogInstance()));
	}

	/** Without a running service, the contribution describes nothing. */
	public void testContributionInactiveWithoutService() {
		assertNull("Precondition: no started A2AService.", A2AService.getInstance());
		A2ADiscoveryContribution contribution = new A2ADiscoveryContribution();

		assertFalse(contribution.isActive());
		assertEquals(List.of(), contribution.llmsTxtSections(CONTEXT));
		assertEquals(List.of(), contribution.catalogEntries(CONTEXT));
	}

	private static ExposedAgentConfig agent(String path, String displayName, String description) {
		ExposedAgentConfig config = TypedConfiguration.newConfigItem(ExposedAgentConfig.class);
		set(config, ExposedAgentConfig.PATH, path);
		set(config, ExposedAgentConfig.AGENT, path + "-agent");
		set(config, ExposedAgentConfig.DISPLAY_NAME, displayName);
		set(config, ExposedAgentConfig.DESCRIPTION, description);
		return config;
	}

	private static List<EndpointCredential> personalAccessTokens() {
		AccessTokenAuthentication.Config<?> config =
			TypedConfiguration.newConfigItem(AccessTokenAuthentication.Config.class);
		config.setDomain("tokens");
		return EndpointAuthentication.credentials(Map.<String, ServerAuthentication.Config<?>> of("tokens", config));
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}
}
