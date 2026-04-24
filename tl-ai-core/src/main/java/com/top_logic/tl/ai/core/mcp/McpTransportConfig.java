/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Base configuration interface for MCP server transports.
 *
 * @see StdioTransportConfig
 * @see HttpTransportConfig
 */
public interface McpTransportConfig extends PolymorphicConfiguration<McpTransportConfig> {
	// Marker interface. Concrete transport configs define transport-specific properties.
}
