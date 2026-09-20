/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.mcp;

import java.util.List;
import java.util.Map;

import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Wraps a single MCP tool as a native {@link Tool}.
 *
 * <p>
 * From the agent's perspective, an {@link McpTool} is indistinguishable from a locally defined
 * tool. The {@link #execute} method delegates to the {@link McpServerRegistry} which routes the
 * call to the correct MCP server via JSON-RPC.
 * </p>
 *
 * <p>
 * Every tool reached this way is {@link ToolMode#EXTERNAL}, read and write alike, so that the one
 * gate that fits an outward call - a human being asked first - is the one it goes through.
 * </p>
 *
 * <p>
 * What comes back is <b>not</b> trusted. An MCP server is a separate process or a remote endpoint
 * that this application does not control, and its result text reaches the model verbatim, so it is
 * returned inside the {@link UntrustedEnvelope#MCP_TOOL_RESULT untrusted-content envelope}. tl-ai's
 * own tools are not wrapped and need not be: what separates the two is the class rather than a flag,
 * because no tool of this application is ever dispatched through here.
 * </p>
 *
 * @see McpServerRegistry
 */
public class McpTool implements Tool {

	private final String _serverName;

	private final String _mcpToolName;

	private final String _description;

	private final String _inputSchemaJson;

	private final McpServerRegistry _registry;

	/**
	 * Creates a new {@link McpTool}.
	 *
	 * @param serverName
	 *        The MCP server name as configured in {@link McpServerRegistry}.
	 * @param mcpToolName
	 *        The original tool name from the MCP server.
	 * @param description
	 *        Human-readable description from the MCP tool definition.
	 * @param inputSchemaJson
	 *        JSON Schema string for the tool's input parameters.
	 * @param registry
	 *        The registry to delegate {@link #execute} calls to, or {@code null} for testing.
	 */
	public McpTool(String serverName, String mcpToolName, String description,
			String inputSchemaJson, McpServerRegistry registry) {
		_serverName = serverName;
		_mcpToolName = mcpToolName;
		_description = description;
		_inputSchemaJson = inputSchemaJson;
		_registry = registry;
	}

	@Override
	public String name() {
		return "mcp__" + sanitize(_serverName) + "__" + sanitize(_mcpToolName);
	}

	/**
	 * Sanitizes a name to match LLM provider tool name constraints ({@code [a-zA-Z0-9_-]}).
	 */
	private static String sanitize(String name) {
		return name.replaceAll("[^a-zA-Z0-9_-]", "_");
	}

	@Override
	public String description() {
		return _description;
	}

	@Override
	public ToolSchema schema() {
		return new ToolSchema(name(), _description, _inputSchemaJson);
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		McpSchema.CallToolResult result = _registry.callTool(_serverName, _mcpToolName, args);
		return answer(name(), result);
	}

	/**
	 * The remote result as the agent loop is to record it: the formatted text, and whether the server
	 * said the call failed.
	 *
	 * <p>
	 * The server's {@code isError} is the only thing that says so, and it has to survive the trip:
	 * without {@link ToolResult#failure(String)} a failed remote call arrives at the provider as a
	 * <em>successful</em> tool call whose text starts with {@code "Error: "} (Gitea #387). The word
	 * remains in the text as well, because the text alone is what a transcript, a compaction summary
	 * and a log line carry.
	 * </p>
	 *
	 * @param source
	 *        Which tool of which server produced the result, for the envelope's {@code source}
	 *        attribute.
	 * @see #formatResult(String, McpSchema.CallToolResult)
	 */
	static ToolResult answer(String source, McpSchema.CallToolResult result) {
		String text = formatResult(source, result);
		return Boolean.TRUE.equals(result.isError()) ? ToolResult.failure(text) : ToolResult.of(text);
	}

	/**
	 * Always {@link ToolMode#EXTERNAL}: the call leaves this application.
	 *
	 * <p>
	 * Not derived from what the tool does on the far side, and deliberately not from what the server
	 * declares about it. The MCP {@code readOnlyHint} and {@code destructiveHint} annotations are a
	 * server's claims about itself, and the specification tells clients not to base trust decisions
	 * on them; a server that wanted to be auto-approved would only have to send
	 * {@code readOnlyHint: true}. Reading them here would make exactly that work, so nothing reads
	 * them, and a server's self-description influences no gate.
	 * </p>
	 *
	 * <p>
	 * Nor is the distinction worth having. A remote read is not the harmless thing a local
	 * {@code READ} is: it sends the agent's arguments out of the application and takes an answer
	 * back that the model then acts on. What the mode has to capture is that the call crosses the
	 * boundary, and every MCP call does.
	 * </p>
	 */
	@Override
	public ToolMode mode() {
		return ToolMode.EXTERNAL;
	}

	/**
	 * Never: follows from {@link #mode()}.
	 *
	 * <p>
	 * Every {@link ToolMode#EXTERNAL} call is put in front of a human, and a concurrent batch is
	 * precisely the shape in which nobody is asked.
	 * </p>
	 */
	@Override
	public boolean isConcurrencySafe() {
		return false;
	}

	/**
	 * Formats a {@link io.modelcontextprotocol.spec.McpSchema.CallToolResult} into a string for the
	 * agent.
	 *
	 * <p>
	 * Concatenates text content items, notes non-text items as placeholders, and puts the whole thing
	 * into the untrusted-content envelope. Error results are prefixed with {@code "Error: "} - the
	 * prefix outside the envelope, because that word is this application's, while the sentence
	 * explaining the failure is the remote server's and is as attacker-controlled as a successful
	 * result.
	 * </p>
	 *
	 * @param source
	 *        Which tool of which server produced the result, for the envelope's {@code source}
	 *        attribute.
	 * @see UntrustedEnvelope#MCP_TOOL_RESULT
	 */
	static String formatResult(String source, McpSchema.CallToolResult result) {
		List<McpSchema.Content> content = result.content();
		if (content == null || content.isEmpty()) {
			// Nothing came back from the server, so there is nothing of the server's to mark.
			return Boolean.TRUE.equals(result.isError()) ? "Error: unknown" : "";
		}

		StringBuilder sb = new StringBuilder();
		for (McpSchema.Content item : content) {
			if (sb.length() > 0) {
				sb.append("\n");
			}
			if (item instanceof McpSchema.TextContent text) {
				sb.append(text.text());
			} else if (item instanceof McpSchema.ImageContent image) {
				sb.append("[image: ").append(image.mimeType()).append("]");
			} else if (item instanceof McpSchema.EmbeddedResource resource) {
				McpSchema.ResourceContents rc = resource.resource();
				if (rc instanceof McpSchema.TextResourceContents trc) {
					sb.append(trc.text());
				} else {
					sb.append("[resource: ").append(rc.uri()).append("]");
				}
			} else {
				sb.append("[unsupported content type]");
			}
		}

		String body = UntrustedEnvelope.MCP_TOOL_RESULT.wrap(source, sb.toString());
		if (Boolean.TRUE.equals(result.isError())) {
			return "Error: " + body;
		}
		return body;
	}
}
