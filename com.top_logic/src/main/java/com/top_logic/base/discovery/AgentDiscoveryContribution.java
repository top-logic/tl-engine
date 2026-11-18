/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.discovery;

import java.util.List;

import com.top_logic.base.discovery.catalog.CatalogEntry;

/**
 * A part of the self-description an application publishes for agents through the
 * {@link AgentDiscoveryService}.
 *
 * <p>
 * A module that publishes a machine endpoint (an MCP server, A2A agent cards, ...) registers an
 * implementation in the {@link AgentDiscoveryService.Config#getContributions() contributions} of
 * the {@link AgentDiscoveryService} by a configuration overlay in its own
 * <code>*.config.xml</code>.
 * </p>
 */
public interface AgentDiscoveryContribution {

	/**
	 * Whether this contribution currently describes something.
	 *
	 * <p>
	 * An inactive contribution (e.g. one whose service is not running) contributes nothing; neither
	 * {@link #llmsTxtSections(DiscoveryContext)} nor {@link #catalogEntries(DiscoveryContext)} is
	 * called.
	 * </p>
	 */
	default boolean isActive() {
		return true;
	}

	/**
	 * The sections this contribution adds to <code>llms.txt</code>.
	 *
	 * @param context
	 *        The context providing the absolute URLs of the application.
	 */
	default List<LlmsTxtSection> llmsTxtSections(DiscoveryContext context) {
		return List.of();
	}

	/**
	 * The entries this contribution adds to the AI Catalog.
	 *
	 * <p>
	 * Each entry has an {@link CatalogEntry#getIdentifier() identifier}, a
	 * {@link CatalogEntry#getType() type} and exactly one of {@link CatalogEntry#getUrl() url} and
	 * {@link CatalogEntry#getData() data}.
	 * </p>
	 *
	 * @param context
	 *        The context providing the absolute URLs of the application.
	 */
	default List<CatalogEntry> catalogEntries(DiscoveryContext context) {
		return List.of();
	}

}
