/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * Thrown when an LLM-supplied argument cannot be coerced to a tool method's parameter type.
 *
 * <p>Always thrown before the tool's method is invoked, so a caller catching this type
 * specifically (as opposed to the broader {@link IllegalArgumentException}, which a tool's own
 * body may also throw after already producing side effects) knows the tool never ran and the
 * failure is synthetic.</p>
 *
 * @see AnnotatedTools#coerceArg(Object, Class, java.lang.reflect.Type, String, ToolExecutionContext)
 */
public class ToolArgumentCoercionException extends IllegalArgumentException {

	/** @param message LLM-facing description of the coercion failure */
	public ToolArgumentCoercionException(String message) {
		super(message);
	}

	/** @param message LLM-facing description of the coercion failure
	 *  @param cause the underlying coercion error */
	public ToolArgumentCoercionException(String message, Throwable cause) {
		super(message, cause);
	}
}
