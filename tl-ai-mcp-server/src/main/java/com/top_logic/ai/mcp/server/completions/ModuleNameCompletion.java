/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.completions;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.ai.mcp.server.authentication.McpCallerContext;
import com.top_logic.ai.mcp.server.resources.ModuleTypesResource;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.util.model.ModelService;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;

/**
 * MCP completion handler that provides module name suggestions for resource template parameters.
 *
 * <p>
 * This completion handler provides suggestions for the {@code moduleName} parameter in resource
 * templates like {@code toplogic://model/modules/{moduleName}/types}. It returns a list of all
 * available module names in the TopLogic model, optionally filtered by a prefix.
 * </p>
 *
 * <p>
 * The completion handler supports partial matching - if the user has typed part of a module name,
 * only modules starting with that prefix are returned.
 * </p>
 *
 * @author Bernhard Haumacher
 */
public class ModuleNameCompletion {

	/** Parameter name for module name in resource templates. */
	public static final String PARAMETER_NAME = "moduleName";

	/**
	 * Creates the MCP completion specification for module name parameter.
	 *
	 * <p>
	 * This creates a completion handler that responds to requests for the {@code moduleName}
	 * parameter in resource URI templates.
	 * </p>
	 *
	 * @return The completion specification that can be registered with the MCP server.
	 */
	public static McpServerFeatures.SyncCompletionSpecification createSpecification() {
		McpSchema.ResourceReference reference = new McpSchema.ResourceReference(
			ModuleTypesResource.URI_TEMPLATE
		);

		return new McpServerFeatures.SyncCompletionSpecification(
			reference,
			ModuleNameCompletion::handleCompleteRequest
		);
	}

	/**
	 * Handles completion requests for module names.
	 *
	 * <p>
	 * Establishes the calling account's TopLogic context and delegates to {@link #getModuleCompletions(String)}.
	 * </p>
	 *
	 * @param exchange
	 *        The MCP server exchange for interacting with the client.
	 * @param request
	 *        The completion request containing the partial module name.
	 * @return The completion result with matching module names.
	 */
	private static McpSchema.CompleteResult handleCompleteRequest(
			McpSyncServerExchange exchange,
			McpSchema.CompleteRequest request) {

		return McpCallerContext.inCallerContext(() -> {
			// Extract the partial value being typed (if any)
			String partialValue = request.argument() != null ? request.argument().value() : "";

			// Get matching module names
			List<String> completions = getModuleCompletions(partialValue);

			// Create completion response
			McpSchema.CompleteResult.CompleteCompletion completion =
				new McpSchema.CompleteResult.CompleteCompletion(
					completions,
					completions.size(),
					false // {@code hasMore} - we return all matches
				);

			return new McpSchema.CompleteResult(completion);
		});
	}

	/**
	 * Gets module name completions, optionally filtered by a prefix.
	 *
	 * @param prefix
	 *        The partial module name to filter by (may be empty for all modules).
	 * @return List of module names matching the prefix.
	 */
	private static List<String> getModuleCompletions(String prefix) {
		TLModel model = ModelService.getApplicationModel();

		return model.getModules().stream()
			.map(TLModule::getName)
			.filter(name -> prefix == null || prefix.isEmpty() || name.startsWith(prefix))
			.sorted()
			.collect(Collectors.toList());
	}
}
