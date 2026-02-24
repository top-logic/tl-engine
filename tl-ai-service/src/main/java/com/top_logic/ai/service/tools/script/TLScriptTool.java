/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.script;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.help.messages.ErrorResult;
import com.top_logic.ai.service.tools.script.TLScriptExecutor.ExecutionResult;
import com.top_logic.ai.service.tools.script.messages.ValidationResult;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Langchain4j tool for validating and executing TLScript code.
 *
 * <p>
 * This tool provides two methods for working with TLScript:
 * </p>
 * <ul>
 * <li>{@link #validateTLScript(String)} - Validates syntax, model references, and types</li>
 * <li>{@link #executeTLScript(String, Boolean, List)} - Validates and executes TLScript code</li>
 * </ul>
 *
 * <p>
 * Both methods use the shared {@link TLScriptExecutor} for core validation and execution logic.
 * </p>
 */
public class TLScriptTool {

	/**
	 * Configuration for {@link TLScriptTool}.
	 */
	public interface Config extends PolymorphicConfiguration<TLScriptTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link TLScriptTool}.
	 */
	public TLScriptTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link TLScriptTool} from configuration.
	 */
	@CalledByReflection
	public TLScriptTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Validates TLScript code without executing it.
	 *
	 * <p>
	 * Performs complete TLScript validation:
	 * </p>
	 * <ul>
	 * <li>Syntax validation (parser errors)</li>
	 * <li>Model reference validation (undefined classes/properties/types)</li>
	 * <li>Type checking (type compatibility)</li>
	 * </ul>
	 *
	 * @param script
	 *        The TLScript code to validate.
	 * @return JSON object indicating validation success or containing error details.
	 */
	@Tool("Validate TLScript code syntax, model references, and type compatibility. Returns validation status and error details if invalid.")
	public String validateTLScript(
			@P("TLScript code to validate") String script) {
		Map<String, Object> args = new HashMap<>();
		args.put("script", script);

		return ThreadContextManager.inSystemInteraction(
			TLScriptTool.class,
			() -> {
				try {
					// Validate parameter
					if (script == null || script.trim().isEmpty()) {
						String result = toJson(ErrorResult.create().setError("Script is required"));
						return trackResult("validateTLScript", args, result);
					}

					// Perform validation and return JSON directly
					ValidationResult result = TLScriptExecutor.validate(script);
					String jsonResult = result.toString();

					return trackResult("validateTLScript", args, jsonResult);

				} catch (Exception ex) {
					String error = "Validation failed: " + ex.getMessage();
					Logger.error(error, ex, TLScriptTool.class);
					String result = toJson(ErrorResult.create().setError(error));
					return trackResult("validateTLScript", args, result);
				}
			});
	}

	/**
	 * Executes TLScript code and returns the result.
	 *
	 * <p>
	 * Performs complete TLScript validation before execution. By default, scripts execute in
	 * read-only mode where any modifications are discarded. Set {@code withCommit: true} to
	 * execute within a transaction and persist changes.
	 * </p>
	 *
	 * @param script
	 *        The TLScript code to execute.
	 * @param withCommit
	 *        If true, execute within transaction and commit changes; if false (default), discard
	 *        modifications.
	 * @param args
	 *        Optional arguments to pass to the script ($arg1, $arg2, ...).
	 * @return JSON object containing execution result or error details.
	 */
	@Tool("Execute TLScript code and return results. Validates script before execution. Read-only by default; set withCommit=true to persist modifications.")
	public String executeTLScript(
			@P("TLScript code to execute") String script,
			@P("If true, execute within transaction and commit changes (default: false)") Boolean withCommit,
			@P("Optional arguments to pass to the script") List<Object> args) {
		Map<String, Object> trackingArgs = new HashMap<>();
		trackingArgs.put("script", script);
		trackingArgs.put("withCommit", withCommit);
		trackingArgs.put("args", args);

		return ThreadContextManager.inSystemInteraction(
			TLScriptTool.class,
			() -> {
				try {
					// Validate parameter
					if (script == null || script.trim().isEmpty()) {
						String result = toJson(ErrorResult.create().setError("Script is required"));
						return trackResult("executeTLScript", trackingArgs, result);
					}

					// Default to false if not specified
					boolean commit = withCommit != null && withCommit;

					// Execute the script and return JSON directly
					ExecutionResult execResult = TLScriptExecutor.execute(script, commit, args);
					String jsonResult = execResult.toJson();

					return trackResult("executeTLScript", trackingArgs, jsonResult);

				} catch (Exception ex) {
					String error = "Execution failed: " + ex.getMessage();
					Logger.error(error, ex, TLScriptTool.class);
					String result = toJson(ErrorResult.create().setError(error));
					return trackResult("executeTLScript", trackingArgs, result);
				}
			});
	}

	private static String trackResult(String toolName, Map<String, Object> args, String result) {
		ToolCallTracking.recordIfTracking(toolName, args, result);
		Logger.info(ToolCallTracking.formatToolCall(toolName, args, result), TLScriptTool.class);
		return result;
	}

	/**
	 * Serializes a msgbuf data object to JSON string.
	 */
	private static String toJson(de.haumacher.msgbuf.data.DataObject dataObject) {
		return dataObject.toString();
	}
}
