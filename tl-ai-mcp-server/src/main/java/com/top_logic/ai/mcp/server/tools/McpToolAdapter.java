/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.ToolPermissionFilter;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.security.PreCommitGuard;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;

/**
 * Adapter that converts {@link Tool}s from the {@link ToolRegistryService} to MCP tool specifications.
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
 * <b>{@code tools/list} is not filtered per caller.</b> The SDK answers it from the globally
 * registered set without reference to the exchange, so every authenticated caller sees every tool
 * name, description and input schema, and only execution is authorized. Closing that needs an
 * exchange-aware {@code tools/list} hook the SDK does not offer (MCP Java SDK 1.1.4).
 * </p>
 */
public class McpToolAdapter {

	/**
	 * Prefix used for adapted tool names in MCP.
	 *
	 * <p>
	 * Tools use qualified names like "factory.tool", but MCP tool names should use hyphens. This
	 * prefix helps identify adapted tools.
	 * </p>
	 */
	private static final String TOOL_NAME_PREFIX = "tl-";

	/**
	 * Adapts a single {@link Tool} to an MCP tool specification.
	 *
	 * <p>
	 * The definition carries {@link McpToolAnnotations annotations} derived from the tool's
	 * {@link Tool#mode() mode} and {@link Tool#group() group}, so that a client that does not know
	 * this application can still tell a read from a write before it calls one.
	 * </p>
	 *
	 * @param tool
	 *        The tool to adapt.
	 * @param qualifiedName
	 *        The qualified name of the tool from the registry (e.g., "model-browser.listModules").
	 * @return An MCP tool specification that wraps the tool.
	 */
	public static McpServerFeatures.SyncToolSpecification adapt(Tool tool, String qualifiedName) {
		String mcpToolName = TOOL_NAME_PREFIX + qualifiedName.replace('.', '-');

		McpJsonMapper jsonMapper = McpJsonDefaults.getMapper();

		McpSchema.ToolAnnotations annotations = McpToolAnnotations.of(tool);
		McpSchema.Tool mcpTool = McpSchema.Tool.builder()
			.name(mcpToolName)
			// The same display name in both places, because the top-level field is the newer one and
			// an older client reads only the one in the annotations. See McpToolAnnotations.of.
			.title(annotations.title())
			.description(tool.description())
			.inputSchema(jsonMapper, tool.schema().inputSchemaJson())
			.annotations(annotations)
			.build();

		return McpServerFeatures.SyncToolSpecification.builder()
			.tool(mcpTool)
			.callHandler((exchange, request) -> handleToolCall(request, tool))
			.build();
	}

	/**
	 * Creates MCP tool specifications for all tools registered in the {@link ToolRegistryService}.
	 *
	 * <p>
	 * This method should only be called when the {@link ToolRegistryService} is active.
	 * </p>
	 *
	 * @return List of MCP tool specifications for all registered tools.
	 */
	public static List<McpServerFeatures.SyncToolSpecification> adaptAllTools() {
		List<McpServerFeatures.SyncToolSpecification> result = new ArrayList<>();

		if (!ToolRegistryService.Module.INSTANCE.isActive()) {
			Logger.info("ToolRegistryService is not active, skipping tool adaptation",
				McpToolAdapter.class);
			return result;
		}

		ToolRegistryService registry = ToolRegistryService.getInstance();
		for (String qualifiedName : registry.getToolNames()) {
			try {
				Tool tool = registry.getTool(qualifiedName);
				if (tool != null) {
					result.add(adapt(tool, qualifiedName));
					Logger.info("Adapted tool '" + qualifiedName + "' for MCP",
						McpToolAdapter.class);
				}
			} catch (Exception ex) {
				Logger.error("Failed to adapt tool '" + qualifiedName + "': " + ex.getMessage(),
					ex, McpToolAdapter.class);
			}
		}

		return result;
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

		String denial = new ToolPermissionFilter(context.security()).denialReason(tool);
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
	 * writes state this application's transaction covers, the pre-commit model-access guard before
	 * the commit, and an all-or-nothing rollback on any failure. A {@code READ} tool needs none of
	 * it, and an {@code EXTERNAL} one has nothing this transaction could take back.
	 * </p>
	 */
	private static McpSchema.CallToolResult execute(CallToolRequest request, Tool tool, ToolContext context) {
		Map<String, Object> arguments = request.arguments();
		if (arguments == null) {
			arguments = Map.of();
		}

		TransactionScope scope = context.transaction();
		boolean writes = tool.mode().writesApplicationState();
		if (writes) {
			scope.begin();
		}
		try {
			ToolResult result = tool.execute(arguments, context);

			if (writes) {
				String violation = precommitViolation(context, scope);
				if (violation != null) {
					scope.rollback();
					Logger.info("Rolled back MCP tool call '" + request.name() + "': " + violation,
						McpToolAdapter.class);
					return error("The changes of this call were rolled back and NOT persisted,"
						+ " because your account is not permitted to make all of them: " + violation);
				}
				scope.commit();
			}

			return McpSchema.CallToolResult.builder()
				.content(List.of(new McpSchema.TextContent(result.text())))
				.build();
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
	 * Why the accumulated changes may not be persisted, or <code>null</code> if they may.
	 */
	private static String precommitViolation(ToolContext context, TransactionScope scope) {
		PreCommitGuard guard = PreCommitGuard.forRun();
		if (!guard.enforces()) {
			return null;
		}
		SecurityContext security = context.security();
		List<PreCommitGuard.Violation> violations = guard.check(security, scope.changes());
		return violations.isEmpty() ? null : PreCommitGuard.describe(violations);
	}

	/**
	 * An error result the MCP client sees as the outcome of its call.
	 */
	private static McpSchema.CallToolResult error(String message) {
		return McpSchema.CallToolResult.builder()
			.content(List.of(new McpSchema.TextContent(message)))
			.isError(true)
			.build();
	}
}
