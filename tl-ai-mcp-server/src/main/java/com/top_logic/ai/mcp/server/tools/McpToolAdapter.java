/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncListFilter;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * Adapter that converts the {@link McpExposure published} tools of the application to MCP tool
 * specifications.
 *
 * <p>
 * This adapter bridges the tl-ai-core tool system (used by in-app AI agents) with the MCP
 * protocol, allowing external AI clients to use the same tools via HTTP.
 * </p>
 *
 * <p>
 * A call executes as the account the request was authenticated as, and is authorized exactly like a
 * call from the chat UI: the account's roles decide which tools it reaches, and its model access
 * rights decide what those tools may see and change. What differs is that an MCP caller has
 * <b>nobody to answer an approval prompt</b>, so the run is a background run and the policy's
 * {@code <background-allowlist>} is its additional ceiling - see
 * {@link ToolContext#forBackgroundRun(Person)}.
 * </p>
 *
 * <p>
 * {@code tools/list} shows a caller only the published tools it may call: the
 * {@link #visibilityFilter(List) visibility filter} asks the same gate a call passes, for the
 * account the listing request authenticated as.
 * </p>
 */
public class McpToolAdapter {

	/**
	 * Adapts a published tool to an MCP tool specification.
	 *
	 * <p>
	 * The definition carries {@link McpToolAnnotations annotations} derived from the tool's
	 * {@link Tool#mode() mode} and {@link Tool#group() group}, so that a client that does not know
	 * this application can still tell a read from a write before it calls one.
	 * </p>
	 *
	 * @param published
	 *        The tool and how it is published.
	 * @return An MCP tool specification that wraps the tool.
	 */
	public static McpServerFeatures.SyncToolSpecification adapt(McpExposure.Published published) {
		Tool tool = published.tool();
		McpJsonMapper jsonMapper = McpJsonDefaults.getMapper();

		McpSchema.ToolAnnotations annotations = McpToolAnnotations.of(tool, published.title());
		McpSchema.Tool mcpTool = McpSchema.Tool
			.builder(published.name(), jsonMapper, tool.schema().inputSchemaJson())
			// The same display name in both places, because the top-level field is the newer one and
			// an older client reads only the one in the annotations. See McpToolAnnotations.of.
			.title(published.title())
			.description(published.description())
			.annotations(annotations)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(mcpTool)
			.callHandler((exchange, request) -> handleToolCall(request, tool))
			.build();
	}

	/**
	 * The filter that decides which of the given published tools {@code tools/list} shows to the
	 * caller of a listing request.
	 *
	 * <p>
	 * A published tool is listed if and only if the caller passes the gate that also decides its
	 * call - see {@link #isVisible(String, Tool)}. A tool the filter does not know, because it was
	 * not published through {@link McpExposure}, is left to whoever registered it and stays listed.
	 * </p>
	 *
	 * <p>
	 * The filter runs on the servlet request thread of the listing request - the MCP server is built
	 * with {@code immediateExecution(true)} - so the account {@code McpAuthenticationFilter} bound for
	 * that request is the one it decides for.
	 * </p>
	 *
	 * @param published
	 *        The tools as they are published, see {@link McpExposure#resolve(List)}.
	 */
	public static McpSyncListFilter<McpSchema.Tool> visibilityFilter(List<McpExposure.Published> published) {
		Map<String, Tool> toolByPublishedName = new HashMap<>();
		for (McpExposure.Published entry : published) {
			toolByPublishedName.put(entry.name(), entry.tool());
		}
		return (transportContext, mcpTool) -> {
			Tool tool = toolByPublishedName.get(mcpTool.name());
			return tool == null || isVisible(mcpTool.name(), tool);
		};
	}

	/**
	 * Whether {@code tools/list} shows the given published tool to the caller of the current request.
	 *
	 * <p>
	 * Decided by the {@link #callGate(ToolContext) gate of a call}, in its quiet form: a listing
	 * enumerates every published tool and must not write a log line for a tool nobody called. A
	 * request without an account, and a check that fails, hide the tool: an error escaping this
	 * method would fail the whole listing.
	 * </p>
	 *
	 * @param publishedName
	 *        The name the tool is published under, for the log.
	 * @param tool
	 *        The tool behind it.
	 */
	static boolean isVisible(String publishedName, Tool tool) {
		Person caller = McpCallerContext.caller();
		if (caller == null) {
			// Defence in depth, as for a call: McpAuthenticationFilter refuses an unauthenticated
			// request long before this.
			Logger.error("Hid MCP tool '" + publishedName + "' from a tools/list request without an"
				+ " authenticated account.", McpToolAdapter.class);
			return false;
		}
		try {
			return callGate(ToolContext.forBackgroundRun(caller)).test(tool);
		} catch (RuntimeException ex) {
			Logger.error("Hid MCP tool '" + publishedName + "' from the tools/list of account '"
				+ caller.getName() + "': checking its access failed: " + ex.getMessage(), ex,
				McpToolAdapter.class);
			return false;
		}
	}

	/**
	 * The gate a published tool passes for the run of the given context, before it is called and to
	 * be listed.
	 */
	private static ToolPermissionFilter callGate(ToolContext context) {
		return new ToolPermissionFilter(context.security());
	}

	/**
	 * Handles an MCP tool call by delegating to the tool, as the account the request authenticated as.
	 *
	 * <p>
	 * The handler runs on the servlet request thread - the MCP server is built with
	 * {@code immediateExecution(true)} - so the account and the {@code TLContext}
	 * {@code McpAuthenticationFilter} established are the ones in force here.
	 * </p>
	 */
	private static McpSchema.CallToolResult handleToolCall(
			CallToolRequest request,
			Tool tool) {

		Person caller;
		try {
			caller = McpCallerContext.requireCaller();
		} catch (IllegalStateException ex) {
			// Defence in depth: the filter refuses an unauthenticated request long before this. A
			// call that reaches here without an account has escaped the filter, which is a
			// deployment fault and not something to run anyway.
			Logger.error("Refused MCP tool call '" + request.name() + "': " + ex.getMessage(), ex,
				McpToolAdapter.class);
			return error(ex.getMessage());
		}

		ToolContext context = ToolContext.forBackgroundRun(caller);

		String denial = callGate(context).denialReason(tool);
		if (denial != null) {
			Logger.info("Denied MCP tool call '" + request.name() + "' for account '"
				+ caller.getName() + "': " + denial, McpToolAdapter.class);
			return error(denial);
		}

		return execute(request, tool, context);
	}

	/**
	 * Runs the tool and persists what it changed, or rolls the change back.
	 *
	 * <p>
	 * The single-call counterpart of one {@code AgentLoop} turn: a transaction only for a tool that
	 * writes state this application's transaction covers, committed after the tool ran, and an
	 * all-or-nothing rollback on any failure. The tool checks its writes under the caller's rights
	 * itself, as it does in a chat turn. A {@code READ} tool needs none of it, and an
	 * {@code EXTERNAL} one has nothing this transaction could take back.
	 * </p>
	 */
	private static McpSchema.CallToolResult execute(CallToolRequest request, Tool tool, ToolContext context) {
		Map<String, Object> arguments = request.arguments();
		if (arguments == null) {
			arguments = Map.of();
		}

		TransactionScope scope = context.transaction();
		// The mode of this call, not of the tool: the dry run of a write tool changes nothing.
		boolean writes = ToolCatalog.callMode(tool, arguments).writesApplicationState();
		if (writes) {
			scope.begin();
		}
		try {
			ToolResult result = tool.execute(arguments, context);

			if (writes) {
				scope.commit();
			}

			return published(result);
		} catch (Exception ex) {
			Logger.error("Error executing MCP tool call '" + request.name() + "': " + ex.getMessage(),
				ex, McpToolAdapter.class);
			return error("Error executing tool '" + request.name() + "': " + ex.getMessage());
		} finally {
			// Rolls back a transaction that is still open - a tool that threw, or a commit that
			// failed - and releases the locks the tool acquired. A no-op after a successful commit,
			// which clears the transaction itself.
			scope.close();
		}
	}

	/**
	 * What an external MCP client is told a tool answered.
	 *
	 * <p>
	 * A tool that reports a failure instead of throwing is published with {@code isError: true}, the
	 * one bit the protocol carries beside the text. Without this a returned failure left this server
	 * as a <em>successful</em> call whose text happened to say otherwise, and a client had nothing but
	 * the English to go on (Gitea #387) - while the three framework refusals below it were flagged
	 * correctly all along.
	 * </p>
	 */
	static McpSchema.CallToolResult published(ToolResult result) {
		if (result.failed()) {
			return error(result.text());
		}
		return McpSchema.CallToolResult.builder()
			.content(List.of(McpSchema.TextContent.builder(result.text()).build()))
			.build();
	}

	/**
	 * An error result the MCP client sees as the outcome of its call.
	 */
	private static McpSchema.CallToolResult error(String message) {
		return McpSchema.CallToolResult.builder()
			.content(List.of(McpSchema.TextContent.builder(message).build()))
			.isError(true)
			.build();
	}
}
