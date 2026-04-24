/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Configuration for an MCP server accessed via stdio transport (subprocess).
 *
 * <p>
 * The MCP server is started as a child process. Communication happens via stdin/stdout using
 * JSON-RPC. Environment variables can be used to pass API keys or other secrets.
 * </p>
 *
 * @see McpTransportConfig
 */
@TagName("stdio")
public interface StdioTransportConfig extends McpTransportConfig {

	/** Configuration property name for {@link #getCommand()}. */
	String COMMAND = "command";

	/** Configuration property name for {@link #getArgs()}. */
	String ARGS = "args";

	/** Configuration property name for {@link #getEnv()}. */
	String ENV = "env";

	/**
	 * The command to execute (e.g., {@code "npx"} or {@code "python"}).
	 */
	@Name(COMMAND)
	String getCommand();

	/**
	 * Command-line arguments passed to the command.
	 *
	 * <p>
	 * In XML config, provide as comma-separated values:
	 * {@code args="-y,@modelcontextprotocol/server-github"}
	 * </p>
	 */
	@Name(ARGS)
	List<String> getArgs();

	/**
	 * Environment variables for the subprocess.
	 *
	 * <p>
	 * Use this to pass API keys without putting them in config:
	 * {@code <entry key="GITHUB_TOKEN">${env:GITHUB_TOKEN}</entry>}
	 * </p>
	 */
	@Name(ENV)
	Map<String, String> getEnv();

}
