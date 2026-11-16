/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.discovery;

import java.util.List;

import com.top_logic.ai.mcp.server.MCPServerService;
import com.top_logic.base.discovery.AgentDiscoveryServlet;
import com.top_logic.base.discovery.DiscoveryContext;
import com.top_logic.base.discovery.LlmsTxtSection;
import com.top_logic.basic.StringServices;
import com.top_logic.tl.ai.core.access.EndpointCredential;
import com.top_logic.util.Resources;

/**
 * Tells a client how to connect to the MCP endpoint: in the section of the endpoint in
 * <code>llms.txt</code>, and in the answer to a request without a valid credential.
 *
 * <p>
 * Every text of one answer is resolved by the one {@link Resources} given, so that an answer is in
 * a single language.
 * </p>
 */
public final class McpConnectionGuide {

	/**
	 * The environment variable reference shown as the place of the secret in a project's
	 * <code>.mcp.json</code>.
	 */
	public static final String TOKEN_VARIABLE = "${MCP_TOKEN}";

	/**
	 * The command line client whose command {@link #llmsTxtSection} shows.
	 */
	public static final String CLAUDE_MCP_ADD = "claude mcp add --transport http --scope user";

	/**
	 * The option of {@link #CLAUDE_MCP_ADD} that adds a request header.
	 */
	public static final String HEADER_OPTION = "--header";

	private McpConnectionGuide() {
		// Static utility.
	}

	/**
	 * The section of the MCP endpoint in <code>llms.txt</code>.
	 *
	 * @param config
	 *        The configuration of the server.
	 * @param credentials
	 *        How a client presents the credentials the server accepts.
	 * @param context
	 *        The URLs of the application.
	 * @param resources
	 *        The language of the section.
	 */
	public static LlmsTxtSection llmsTxtSection(MCPServerService.Config<?> config,
			List<EndpointCredential> credentials, DiscoveryContext context, Resources resources) {
		String endpoint = context.url(MCPServerService.MCP_ENDPOINT);
		StringBuilder out = new StringBuilder();
		out.append(resources.getString(I18NConstants.LLMS_INTRO__SERVER.fill(config.getServerName())));
		out.append("\n\n");
		out.append("- ").append(resources.getString(I18NConstants.LLMS_ENDPOINT__URL_SLASH_URL.fill(endpoint, endpoint + "/")));
		out.append('\n');
		out.append("- ").append(resources.getString(I18NConstants.LLMS_CARD__URL.fill(context.url(McpServerCard.PATH))));
		out.append("\n\n");
		appendCredentials(out, credentials, context, resources);

		EndpointCredential header = firstHeaderCredential(credentials);
		if (header != null) {
			out.append('\n');
			out.append(resources.getString(I18NConstants.LLMS_CONNECT_CLAUDE)).append("\n\n");
			out.append("```bash\n");
			out.append(CLAUDE_MCP_ADD).append(' ')
				.append(McpServerCard.localName(McpServerCard.name(config, context))).append(' ')
				.append(endpoint).append(" \\\n    ")
				.append(HEADER_OPTION).append(" \"").append(header.header()).append(": ")
				.append(resources.getString(header.headerValue())).append("\"\n");
			out.append("```\n\n");
			out.append(resources.getString(I18NConstants.LLMS_CREDENTIAL_STORAGE__VARIABLE.fill(TOKEN_VARIABLE)));
			out.append('\n');
		}

		String instructions = StringServices.nonEmpty(config.getInstructions());
		if (instructions != null) {
			out.append('\n');
			out.append(resources.getString(I18NConstants.LLMS_INSTRUCTIONS)).append("\n\n");
			out.append(instructions.strip()).append('\n');
		}
		return new LlmsTxtSection(resources.getString(I18NConstants.LLMS_TITLE), out.toString());
	}

	/**
	 * What the answer to a request without a valid credential adds to the message of the
	 * authentication: where the client reads how to connect, and which credentials are accepted.
	 *
	 * @param credentials
	 *        How a client presents the credentials the server accepts.
	 * @param context
	 *        The URLs of the application.
	 * @param resources
	 *        The language of the message of the authentication.
	 */
	public static String unauthorizedGuidance(List<EndpointCredential> credentials, DiscoveryContext context,
			Resources resources) {
		StringBuilder out = new StringBuilder();
		out.append(resources.getString(I18NConstants.UNAUTHORIZED_HOW_TO_CONNECT__LLMS_CARD.fill(
			context.url(AgentDiscoveryServlet.LLMS_TXT_PATH), context.url(McpServerCard.PATH))));
		out.append('\n');
		appendCredentials(out, credentials, context, resources);
		return out.toString();
	}

	private static void appendCredentials(StringBuilder out, List<EndpointCredential> credentials,
			DiscoveryContext context, Resources resources) {
		if (credentials.isEmpty()) {
			out.append(resources.getString(I18NConstants.NO_CREDENTIALS)).append('\n');
			return;
		}
		out.append(resources.getString(I18NConstants.CREDENTIALS_INTRO)).append('\n');
		for (EndpointCredential credential : credentials) {
			out.append("- ").append(credential.describeWithOrigin(context, resources)).append('\n');
		}
	}

	private static EndpointCredential firstHeaderCredential(List<EndpointCredential> credentials) {
		for (EndpointCredential credential : credentials) {
			if (credential.isHeader()) {
				return credential;
			}
		}
		return null;
	}

}
