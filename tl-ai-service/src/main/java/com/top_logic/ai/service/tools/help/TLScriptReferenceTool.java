/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.help;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.tools.help.messages.FunctionDocResult;
import com.top_logic.ai.service.tools.help.messages.FunctionListResult;
import com.top_logic.ai.service.tools.util.DataClassSerializer;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for listing TLScript functions and retrieving documentation.
 * <p>
 * This tool provides access to the TLScript function registry, allowing AI agents to discover
 * available functions and retrieve detailed documentation for them. It uses {@link SearchBuilder}
 * to access the function metadata.
 * </p>
 */
public class TLScriptReferenceTool {

	/**
	 * Configuration for {@link TLScriptReferenceTool}.
	 */
	public interface Config extends PolymorphicConfiguration<TLScriptReferenceTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link TLScriptReferenceTool} from configuration.
	 */
	@CalledByReflection
	public TLScriptReferenceTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Lists all available TLScript functions and methods.
	 * <p>
	 * Returns a list of function names that can be used in TLScript expressions. These functions
	 * are the building blocks for writing TLScript code.
	 * </p>
	 *
	 * @return JSON object containing the list of function names.
	 */
	@Tool("List all available TLScript functions and methods. Returns function names that can be used in TLScript expressions.")
	public String listTLScriptFunctions() {
		Map<String, Object> args = new HashMap<>();

		return ThreadContextManager.inSystemInteraction(
			TLScriptReferenceTool.class,
			() -> {
				try {
					SearchBuilder<?> searchBuilder = SearchBuilder.getInstance();
					List<String> functionNames = searchBuilder.getMethodNames();

					// Build response using msgbuf data class
					FunctionListResult result = FunctionListResult.create();
					result.setFunctions(functionNames);

					return DataClassSerializer.toJson(result, TLScriptReferenceTool.class, "listTLScriptFunctions()", args);

				} catch (Exception ex) {
					String error = "Failed to list TLScript functions: " + ex.getMessage();
					Logger.error(error, ex, TLScriptReferenceTool.class);
					return DataClassSerializer.errorToJson(error, TLScriptReferenceTool.class, "listTLScriptFunctions()", args);
				}
			});
	}

	/**
	 * Gets detailed documentation for a specific TLScript function.
	 * <p>
	 * Returns the function signature, description, parameters, return value, and usage examples
	 * when available. This helps understand how to use the function correctly in TLScript code.
	 * </p>
	 *
	 * @param functionName
	 *        Name of the TLScript function to get documentation for.
	 * @return JSON object containing the function documentation if available, or an error message
	 *         if not found.
	 */
	@Tool("Get detailed documentation for a specific TLScript function. Returns signature, description, and usage information.")
	public String getTLScriptFunctionDocumentation(
			@P("Name of the TLScript function") String functionName) {

		Map<String, Object> args = new HashMap<>();
		args.put("functionName", functionName);

		return ThreadContextManager.inSystemInteraction(
			TLScriptReferenceTool.class,
			() -> {
				try {
					// Validate parameters
					if (functionName == null || functionName.trim().isEmpty()) {
						String error = "Function name is required";
						Logger.error("getTLScriptFunctionDocumentation() error: " + error,
							TLScriptReferenceTool.class);
						return DataClassSerializer.errorToJson(error, TLScriptReferenceTool.class, "getTLScriptFunctionDocumentation()", args);
					}

					SearchBuilder<?> searchBuilder = SearchBuilder.getInstance();

					Optional<String> documentation =
						searchBuilder.getDocumentation(DummyDisplayContext.newInstance(), functionName);

					// Retrieve parameter information from the function builder
					List<String> parameters = new ArrayList<>();
					try {
						MethodBuilder<?> builder = searchBuilder.getBuilder(functionName);
						if (builder != null) {
							ArgumentDescriptor descriptor = builder.descriptor();

							// TODO: Make in TL accessible.

							// Use reflection to access protected methods
							Method getMaxArgCntMethod = descriptor.getClass().getDeclaredMethod("getMaxArgCnt");
							getMaxArgCntMethod.setAccessible(true);
							int maxArgs = (Integer) getMaxArgCntMethod.invoke(descriptor);

							if (maxArgs < 0) {
								// Varargs function - indicate with special marker
								parameters.add("...");
							} else {
								// Fixed number of arguments - retrieve each parameter name
								Method getArgumentNameMethod = descriptor.getClass().getDeclaredMethod("getArgumentName", int.class);
								getArgumentNameMethod.setAccessible(true);
								for (int i = 0; i < maxArgs; i++) {
									String paramName = (String) getArgumentNameMethod.invoke(descriptor, i);
									parameters.add(paramName);
								}
							}
						}
					} catch (Exception ex) {
						// Log parameter retrieval failure but continue with documentation
						Logger.warn("Failed to retrieve parameters for function '" + functionName + "': " + ex.getMessage(),
							TLScriptReferenceTool.class);
					}

					// Build response using msgbuf data class
					FunctionDocResult result = FunctionDocResult.create()
						.setFunctionName(functionName)
						.setHasDocumentation(documentation.isPresent())
						.setParameters(parameters);

					if (documentation.isPresent()) {
						// Convert HTML documentation to Markdown
						String htmlDoc = documentation.get();
						String markdownDoc = Converters.htmlToMarkdown(htmlDoc);
						result.setDocumentation(markdownDoc);
					} else {
						result.setErrorMessage("No documentation available for function: " + functionName);
					}

					return DataClassSerializer.toJson(result, TLScriptReferenceTool.class, "getTLScriptFunctionDocumentation()", args);

				} catch (Exception ex) {
					String error = "Failed to get function documentation: " + ex.getMessage();
					Logger.error(error, ex, TLScriptReferenceTool.class);
					return DataClassSerializer.errorToJson(error, TLScriptReferenceTool.class, "getTLScriptFunctionDocumentation()", args);
				}
			});
	}
}
