/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.basic.StringServices;
import com.top_logic.element.accesstoken.AccessToken;
import com.top_logic.element.accesstoken.AccessTokens;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.service.openapi.server.authentication.conf.EndpointCredential;
import com.top_logic.tl.ai.core.access.AccessTokenAuthentication;
import com.top_logic.tl.ai.core.access.AccessTokenAuthenticator;

/**
 * TL-Script functions telling a person how to connect an MCP client to this application, e.g. in
 * the dialog that shows a new access token once.
 *
 * <p>
 * The URLs are those of the application as the current request reaches it, see
 * {@link DiscoveryContext#current()}.
 * </p>
 */
@ScriptPrefix("mcp")
public class McpConnectionFunctions extends TLScriptFunctions {

	/**
	 * Whether an MCP client connects to this application with the given access token: the MCP server
	 * runs, its authentications accept access tokens, and the token has the permission
	 * {@value AccessTokenAuthenticator#AGENT_ENDPOINTS_PERMISSION}.
	 *
	 * <p>
	 * Example: <code>mcpAcceptsAccessToken($token)</code>
	 * </p>
	 *
	 * @param token
	 *        An access token, as just created by <code>accessTokenCreate()</code>.
	 * @return Whether a client connects to the MCP endpoint with this token.
	 */
	@SideEffectFree
	public static boolean acceptsAccessToken(String token) {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null || !service.acceptsAccessTokens() || !AccessTokens.hasFormat(token)) {
			return false;
		}
		AccessToken stored = AccessTokens.find(token);
		return stored != null && AccessTokens.hasPermission(stored, AccessTokenAuthenticator.AGENT_ENDPOINTS_PERMISSION);
	}

	/**
	 * The absolute URL of the MCP endpoint of this application, e.g.
	 * <code>https://host/app/mcp</code>.
	 *
	 * <p>
	 * Example: <code>mcpEndpointUrl()</code>
	 * </p>
	 *
	 * @return The URL, or nothing while the MCP server does not run.
	 */
	@SideEffectFree
	public static String endpointUrl() {
		if (MCPServerService.getInstance() == null) {
			return null;
		}
		return DiscoveryContext.current().url(MCPServerService.MCP_ENDPOINT);
	}

	/**
	 * The command that adds the MCP endpoint of this application to Claude Code, authenticated by
	 * the given personal access token.
	 *
	 * <p>
	 * Example: <code>mcpClaudeCommand($token)</code> gives
	 * <code>claude mcp add --transport http --scope user my-app https://host/app/mcp --header "Authorization: Bearer tlpat_..."</code>
	 * </p>
	 *
	 * @param token
	 *        The personal access token the client presents.
	 * @return The command on a single line, or nothing while the MCP server does not run or
	 *         without a token.
	 */
	@SideEffectFree
	public static String claudeCommand(String token) {
		MCPServerService service = MCPServerService.getInstance();
		if (service == null || StringServices.isEmpty(token)) {
			return null;
		}
		return McpConnectionGuide.claudeMcpAdd(service.getConfig(), DiscoveryContext.current(),
			EndpointCredential.AUTHORIZATION_HEADER, AccessTokenAuthentication.headerValue(token), " ");
	}

}
