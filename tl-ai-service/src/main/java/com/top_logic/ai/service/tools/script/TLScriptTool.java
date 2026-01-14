/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.script;

import java.util.List;

import com.top_logic.ai.service.tools.help.messages.ErrorResult;
import com.top_logic.ai.service.tools.script.TLScriptExecutor.ExecutionResult;
import com.top_logic.ai.service.tools.script.TLScriptExecutor.ValidationError;
import com.top_logic.ai.service.tools.script.TLScriptExecutor.ValidationResult;
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

		return ThreadContextManager.inSystemInteraction(
			TLScriptTool.class,
			() -> {
				try {
					// Validate parameter
					if (script == null || script.trim().isEmpty()) {
						return toJson(ErrorResult.create().setError("Script is required"));
					}

					// Perform validation
					ValidationResult result = TLScriptExecutor.validate(script);

					// Build response using msgbuf
					com.top_logic.ai.service.tools.script.messages.ValidationResult msgResult =
						com.top_logic.ai.service.tools.script.messages.ValidationResult.create()
							.setValid(result.isValid());

					if (result.isValid()) {
						msgResult.setMessage("TLScript is valid");
					} else {
						ValidationError error = result.getError();
						com.top_logic.ai.service.tools.script.messages.ValidationError msgError =
							com.top_logic.ai.service.tools.script.messages.ValidationError.create()
								.setType(error.getType().name())
								.setMessage(error.getMessage())
								.setLine(error.getLine())
								.setColumn(error.getColumn());
						msgResult.setError(msgError);
					}

					String jsonResult = toJson(msgResult);
					Logger.info("validateTLScript() result: " + jsonResult, TLScriptTool.class);
					return jsonResult;

				} catch (Exception ex) {
					String error = "Validation failed: " + ex.getMessage();
					Logger.error(error, ex, TLScriptTool.class);
					return toJson(ErrorResult.create().setError(error));
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

		return ThreadContextManager.inSystemInteraction(
			TLScriptTool.class,
			() -> {
				try {
					// Validate parameter
					if (script == null || script.trim().isEmpty()) {
						return toJson(ErrorResult.create().setError("Script is required"));
					}

					// Default to false if not specified
					boolean commit = withCommit != null && withCommit;

					// Execute the script and return JSON directly
					ExecutionResult execResult = TLScriptExecutor.execute(script, commit, args);
					String jsonResult = execResult.toJson();

					Logger.info("executeTLScript() result: " + jsonResult, TLScriptTool.class);
					return jsonResult;

				} catch (Exception ex) {
					String error = "Execution failed: " + ex.getMessage();
					Logger.error(error, ex, TLScriptTool.class);
					return toJson(ErrorResult.create().setError(error));
				}
			});
	}

	/**
	 * Serializes a msgbuf data object to JSON string.
	 */
	private static String toJson(de.haumacher.msgbuf.data.DataObject dataObject) {
		return dataObject.toString();
	}
}
