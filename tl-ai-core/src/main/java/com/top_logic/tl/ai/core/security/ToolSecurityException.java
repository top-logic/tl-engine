/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security;

import com.top_logic.tool.boundsec.BoundCommandGroup;

/**
 * Thrown by a tool when the run's principal may not perform an operation on a model object.
 *
 * <p>
 * The message is written to be fed back to the model as a tool result: it names the operation and
 * the type, never the value that was withheld. A denial that quotes the forbidden data defeats its
 * own purpose.
 * </p>
 *
 * @see ModelSecurity#requireRead(com.top_logic.model.TLObject)
 */
public class ToolSecurityException extends RuntimeException {

	private final BoundCommandGroup _operation;

	private final String _typeName;

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
