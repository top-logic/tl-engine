/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.dynamic;

import io.modelcontextprotocol.server.McpServerFeatures;

/**
 * Interface for dynamic MCP resources that can be registered with the MCP server.
 *
 * <p>
 * This interface provides an abstraction for different types of resource implementations,
 * allowing flexibility in how resources are created and managed. Implementations can vary
 * in how they generate resource content (e.g., via TL-Script expressions, static content,
 * database queries, etc.).
 * </p>
 *
 * <p>
 * Example implementations:
 * </p>
 * <ul>
 * <li>{@link ConfigurableResourceTemplate} - Uses TL-Script expressions to compute content</li>
 * <li>Future: StaticResourceTemplate - Serves static content</li>
 * <li>Future: DatabaseResourceTemplate - Queries database for content</li>
 * </ul>
 *
 * @author Bernhard Haumacher
 */
public interface DynamicResource {

	/**
	 * Creates the MCP resource template specification for this dynamic resource.
	 *
	 * <p>
	 * This method is called during MCP server initialization to register the resource
	 * with the server.
	 * </p>
	 *
	 * @return The resource template specification that can be registered with the MCP server.
	 */
	McpServerFeatures.SyncResourceTemplateSpecification createSpecification();
}
