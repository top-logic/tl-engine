/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Thrown when the run's principal may not do what the run is about to do.
 *
 * <p>
 * Two kinds of refusal: a tool operating on a model object the principal may not touch, and a run
 * that may not be the agent it names, run on the model it names or reach the MCP server it names.
 * </p>
 *
 * <p>
 * The message is written to be fed back to the model as a tool result: it names the operation and
 * the type, never the value that was withheld. A denial that quotes the forbidden data defeats its
 * own purpose.
 * </p>
 *
 * @see ModelSecurity#requireRead(com.top_logic.model.TLObject)
 * @see RunAuthorization
 */
public class ToolSecurityException extends RuntimeException {

	private final BoundCommandGroup _operation;

	private final String _typeName;

	private final ResKey _reason;

	/**
	 * Creates a {@link ToolSecurityException}.
	 *
	 * @param operation
	 *        The operation that was denied.
	 * @param typeName
	 *        The qualified name of the type the denied object belongs to, or <code>null</code> if
	 *        unknown.
	 */
	public ToolSecurityException(BoundCommandGroup operation, String typeName) {
		super(message(operation, typeName));
		_operation = operation;
		_typeName = typeName;
		_reason = null;
	}

	/**
	 * Creates a {@link ToolSecurityException} for a run the principal may not start this way.
	 *
	 * @param reason
	 *        The refusal in the acting user's language, for a client that has to show it to them.
	 * @param message
	 *        The same refusal in English, for the log and for the model.
	 */
	public ToolSecurityException(ResKey reason, String message) {
		super(message);
		_operation = null;
		_typeName = null;
		_reason = reason;
	}

	/**
	 * The refusal in the acting user's language, or <code>null</code> for a model-access refusal,
	 * whose message is written for the model rather than for a person.
	 */
	public ResKey reason() {
		return _reason;
	}

	private static String message(BoundCommandGroup operation, String typeName) {
		String target = typeName == null ? "this object" : "objects of type '" + typeName + "'";
		return "Permission denied: you may not perform '" + operation.getID() + "' on " + target + ".";
	}

	/**
	 * The operation that was denied.
	 */
	public BoundCommandGroup operation() {
		return _operation;
	}

	/**
	 * The qualified name of the type the denied object belongs to, or <code>null</code> if unknown.
	 */
	public String typeName() {
		return _typeName;
	}
}
