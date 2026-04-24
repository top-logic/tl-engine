/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.Map;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Configuration for an MCP server accessed via HTTP (Streamable HTTP transport).
 *
 * <p>
 * The MCP server runs as an external HTTP service. Authentication is handled via static headers
 * or a bearer token resolved from an environment variable at connect time.
 * </p>
 *
 * @see McpTransportConfig
 */
@TagName("http")
public interface HttpTransportConfig extends McpTransportConfig {

	/** Configuration property name for {@link #getUrl()}. */
	String URL = "url";

	/** Configuration property name for {@link #getHeaders()}. */
	String HEADERS = "headers";

	/** Configuration property name for {@link #getBearerTokenEnvVar()}. */
	String BEARER_TOKEN_ENV_VAR = "bearer-token-env-var";

	/**
	 * The URL of the MCP server endpoint (e.g., {@code "https://mcp.example.com/mcp"}).
	 */
	@Name(URL)
	String getUrl();

	/**
	 * Static HTTP headers sent with every request.
	 */
	@Name(HEADERS)
	Map<String, String> getHeaders();

	/**
	 * Name of an environment variable holding a bearer token.
	 *
	 * <p>
	 * Resolved via {@code System.getenv()} at connect time. The token is sent as
	 * {@code Authorization: Bearer <token>} header. This avoids putting secrets in config files.
	 * </p>
	 */
	@Name(BEARER_TOKEN_ENV_VAR)
	String getBearerTokenEnvVar();

}
