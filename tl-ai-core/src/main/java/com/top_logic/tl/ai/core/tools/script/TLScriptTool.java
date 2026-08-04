/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor.ExecutionResult;
import com.top_logic.tl.ai.core.tools.script.messages.ErrorResult;
import com.top_logic.tl.ai.core.tools.script.messages.ValidationResult;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for validating and executing TLScript code.
 *
 * <p>
 * This tool provides validation and execution of TLScript. Both methods use the shared
 * {@link TLScriptExecutor} for core validation and execution logic.
 * </p>
 *
 * @see #validateTLScript(String)
 * @see #executeTLScript(String, Boolean, List, ToolExecutionContext)
 */
public class TLScriptTool {

	/**
	 * What a refused commit says it was trying to do.
	 *
	 * @see #executeTLScript(String, Boolean, List, ToolExecutionContext)
	 */
	private static final String COMMIT_OPERATION = "executing TL-Script with withCommit=true";

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
	@SuppressWarnings("unused")
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
	@ToolMethod(description = "Validate TLScript code syntax, model references, and type compatibility. Returns validation status and error details if invalid.", mode = ToolMode.READ, group = ToolGroups.SCRIPT)
	public String validateTLScript(
			@P("TLScript code to validate") String script) {
		return doValidateTLScript(script);
	}

	private String doValidateTLScript(String script) {
		try {
			// Validate parameter
			if (script == null || script.trim().isEmpty()) {
				return toJson(ErrorResult.create().setError("Script is required"));
			}

			// Perform validation and return JSON directly
			ValidationResult result = TLScriptExecutor.validate(script);
			return result.toString();

		} catch (Exception ex) {
			String error = "Validation failed: " + ex.getMessage();
			Logger.error(error, ex, TLScriptTool.class);
			return toJson(ErrorResult.create().setError(error));
		}
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
	 *        modifications. Requires the {@link ToolGroups#DATA_WRITE} capability in addition to the
	 *        {@link ToolGroups#SCRIPT} the tool itself is classified as.
	 * @param args
	 *        Optional arguments to pass to the script ($arg1, $arg2, ...).
	 * @param context
	 *        Injected execution context, supplying the capability check for the commit path.
	 * @return JSON object containing execution result or error details.
	 */
	@ToolMethod(description = "Execute TLScript code and return results. Validates script before execution. Read-only by default; set withCommit=true to persist modifications.", mode = ToolMode.DATA_WRITE, group = ToolGroups.SCRIPT)
	public String executeTLScript(
			@P("TLScript code to execute") String script,
			@P("If true, execute within transaction and commit changes (default: false)") Boolean withCommit,
			@P("Optional arguments to pass to the script") List<Object> args,
			ToolExecutionContext context) {
		return doExecuteTLScript(script, withCommit, args, context);
	}

	private String doExecuteTLScript(String script, Boolean withCommit, List<Object> args,
			ToolExecutionContext context) {
		try {
			// Validate parameter
			if (script == null || script.trim().isEmpty()) {
				return toJson(ErrorResult.create().setError("Script is required"));
			}

			// Default to false if not specified
			boolean commit = withCommit != null && withCommit;

			if (commit) {
				// The invocation gate decides per tool and cannot see this argument, so the capability
				// the argument costs is checked here: a run granted 'script' alone may read with
				// TL-Script and must not persist with it.
				String denial = commitDenialReason(context);
				if (denial != null) {
					return toJson(ErrorResult.create().setError(denial));
				}
			}

			// Execute the script and return JSON directly
			ExecutionResult execResult = TLScriptExecutor.execute(script, commit, args);
			return execResult.toJson();

		} catch (Exception ex) {
			String error = "Execution failed: " + ex.getMessage();
			Logger.error(error, ex, TLScriptTool.class);
			return toJson(ErrorResult.create().setError(error));
		}
	}

	/**
	 * Why this run may not commit what the script changes, or <code>null</code> if it may.
	 *
	 * <p>
	 * Asks the run's own authorization rather than re-deciding anything: the same policy, the same
	 * order - background allowlist, administrator bypass, role grants - that a {@code data.write} tool
	 * would be decided by. Outside an agent loop (scripting entry points, tests) there is no run to
	 * ask, and {@link SecurityContext#UNRESTRICTED} answers as an unconfigured application does.
	 * </p>
	 */
	private static String commitDenialReason(ToolExecutionContext context) {
		SecurityContext security = context instanceof ToolContext toolContext
			? toolContext.security()
			: SecurityContext.UNRESTRICTED;
		return security.capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT_OPERATION);
	}

	/**
	 * Serializes a msgbuf data object to JSON string.
	 */
	private static String toJson(de.haumacher.msgbuf.data.DataObject dataObject) {
		return dataObject.toString();
	}
}
