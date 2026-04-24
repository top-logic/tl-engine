/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import io.modelcontextprotocol.spec.McpClientTransport;

/**
 * Factory for creating MCP client transports.
 *
 * @see StdioMcpTransport
 * @see HttpMcpTransport
 */
public interface McpTransportConfig {

	/**
	 * Creates the MCP client transport for connecting to a server.
	 */
	McpClientTransport createTransport();

}
