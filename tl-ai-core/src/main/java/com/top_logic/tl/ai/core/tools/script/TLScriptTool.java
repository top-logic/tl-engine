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
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor.ExecutionResult;
import com.top_logic.tl.ai.core.tools.model.messages.ErrorResponse;
import com.top_logic.tl.ai.core.tools.script.messages.ValidationResult;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
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
	@ToolMethod(description = "Validate TLScript code syntax, model references, and type compatibility. Returns validation status and error details if invalid.", mode = ToolMode.READ, group = ToolGroups.SCRIPT, marking = AnswerMarking.NONE)
	public String validateTLScript(
			@P("TLScript code to validate") String script) {
		return doValidateTLScript(script);
	}

	private String doValidateTLScript(String script) {
		try {
			// Validate parameter
			if (script == null || script.trim().isEmpty()) {
				return toJson(ErrorResponse.create().setError("Script is required"));
			}

			// Perform validation and return JSON directly
			ValidationResult result = TLScriptExecutor.validate(script);
			return result.toString();

		} catch (Exception ex) {
			String error = "Validation failed: " + ex.getMessage();
			Logger.error(error, ex, TLScriptTool.class);
			return toJson(ErrorResponse.create().setError(error));
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
	 *        Injected execution context, supplying the capability check for the commit path and the
	 *        {@link ReadFilter} the result is disclosed through.
	 * @return JSON object containing execution result or error details; a successful result is marked
	 *         as business data, see {@link ReadFilter#marked(String, String)}. Model objects in the result
	 *         carry only what the run's principal may read; ones it may not read are absent.
	 */
	@ToolMethod(description = "Execute TLScript code and return results. Validates script before execution. Read-only by default; set withCommit=true to persist modifications. Objects in the result that you may not read are omitted, but only while they are still objects: a count or a sum, and a label produced by toString($obj) or \"\" + $obj, are plain values by the time the result is filtered and are returned in full. So wrap fetched model objects in filterSecurity(...) inside the script - filterSecurity(all(`m:T`)) - before you count them, coerce them to text, or present them, and never assume a label you got out of a script is one the user is allowed to see.", mode = ToolMode.DATA_WRITE, group = ToolGroups.SCRIPT, marking = AnswerMarking.TOOL)
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
				return toJson(ErrorResponse.create().setError("Script is required"));
			}

			// Default to false if not specified
			boolean commit = withCommit != null && withCommit;

			if (commit) {
				// The invocation gate decides per tool and cannot see this argument, so the capability
				// the argument costs is checked here: a run granted 'script' alone may read with
				// TL-Script and must not persist with it.
				String denial = commitDenialReason(context);
				if (denial != null) {
					return toJson(ErrorResponse.create().setError(denial));
				}
			}

			// Execute the script, then disclose only what this run's principal may read of the result.
			// The filter is resolved before the script runs so a context that cannot name a principal
			// fails before anything is read rather than after.
			ReadFilter readFilter = ReadFilter.of(context);
			ExecutionResult execResult = TLScriptExecutor.execute(script, commit, args);
			return execResult.toJson(readFilter);

		} catch (Exception ex) {
			String error = "Execution failed: " + ex.getMessage();
			Logger.error(error, ex, TLScriptTool.class);
			return toJson(ErrorResponse.create().setError(error));
		}
	}

	/**
	 * Why this run may not commit what the script changes, or <code>null</code> if it may.
	 *
	 * <p>
	 * Asks the run's own authorization rather than re-deciding anything: the same policy, the same
	 * order - background allowlist, administrator bypass, role grants - that a {@code data.write} tool
	 * would be decided by. There is no permissive fallback: a commit gate that cannot name the
	 * principal it is deciding for would let every write through.
	 * </p>
	 */
	private static String commitDenialReason(ToolExecutionContext context) {
		if (!(context instanceof ToolContext toolContext)) {
			throw new IllegalStateException("Committing what a script changes is checked against the"
				+ " run's principal, and this call has no run to ask. Execute the tool with the"
				+ " ToolContext of an agent run.");
		}
		return toolContext.security().capabilityDenialReason(ToolGroups.DATA_WRITE, COMMIT_OPERATION);
	}

	/**
	 * Serializes a msgbuf data object to JSON string.
	 */
	private static String toJson(de.haumacher.msgbuf.data.DataObject dataObject) {
		return dataObject.toString();
	}
}
