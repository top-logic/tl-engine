/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import java.util.List;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.base.discovery.AgentDiscoveryContribution;
import com.top_logic.base.discovery.AgentDiscoveryService;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.base.discovery.catalog.CatalogEntry;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.util.Resources;

/**
 * Describes the MCP endpoint of the {@link MCPServerService} in the self-description of the
 * application ({@link AgentDiscoveryService}), while the service runs.
 *
 * <ul>
 * <li>The AI Catalog lists the {@link McpServerCard server card}, identified as
 * <code>urn:air:&lt;host&gt;:mcp:&lt;name&gt;</code> ({@link #identifier(String, String)}).</li>
 * <li><code>llms.txt</code> gets a section with the endpoint, the server card, the accepted
 * credentials and a command that connects Claude Code, see
 * {@link McpConnectionGuide#llmsTxtSection}.</li>
 * </ul>
 */
public class McpDiscoveryContribution implements AgentDiscoveryContribution {

	/**
	 * The scheme and publisher prefix of a catalog {@link CatalogEntry#getIdentifier() identifier}.
	 */
	public static final String IDENTIFIER_PREFIX = "urn:air:";

	/**
	 * The namespace of the catalog {@link CatalogEntry#getIdentifier() identifier} of the server
	 * card.
	 */
	public static final String IDENTIFIER_NAMESPACE = "mcp";

	/**
	 * Creates a {@link McpDiscoveryContribution}.
	 */
	@CalledByReflection
	public McpDiscoveryContribution() {
		// Nothing configurable: everything comes from the running MCPServerService.
	}

	@Override
	public boolean isActive() {
		return MCPServerService.getInstance() != null;
	}

	@Override
	public List<LlmsTxtSection> llmsTxtSections(DiscoveryContext context) {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null) {
			return List.of();
		}
		return List.of(McpConnectionGuide.llmsTxtSection(service.getConfig(), service.getCredentials(), context,
			Resources.getLogInstance()));
	}

	@Override
	public List<CatalogEntry> catalogEntries(DiscoveryContext context) {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null) {
			return List.of();
		}
		return List.of(catalogEntry(service.getServerCard(context), context));
	}

	/**
	 * The catalog entry of the given server card.
	 */
	static CatalogEntry catalogEntry(ServerCard card, DiscoveryContext context) {
		return CatalogEntry.create()
			.setIdentifier(identifier(McpServerCard.host(context.applicationUrl()), card.getName()))
			.setType(McpServerCard.MEDIA_TYPE)
			.setUrl(context.url(McpServerCard.PATH))
			.setDisplayName(card.getTitle())
			.setDescription(card.getDescription())
			.setVersion(card.getVersion());
	}

	/**
	 * The catalog identifier of the server card with the given name, published by the given host.
	 */
	static String identifier(String host, String cardName) {
		return IDENTIFIER_PREFIX + host + ':' + IDENTIFIER_NAMESPACE + ':' + McpServerCard.localName(cardName);
	}

}
