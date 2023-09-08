/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Current bindings {@link Capture}s in a {@link Node#match(Match, Node)} operation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Match {

	/**
	 * Binds the given {@link Capture} to the given {@link Node}.
	 * 
	 * @param capture
	 *        The {@link Capture} to bind.
	 * @param node
	 *        The {@link Node} to bind to.
	 */
	void bind(Capture capture, Node node);

	/**
	 * The binding established for the given {@link Capture} in a former
	 * {@link #bind(Capture, Node)} operation.
	 */
	Node getBinding(Capture capture);

	/**
	 * An unique identifier for the given {@link NewIdReplacement}.
	 */
	String getValue(NewIdReplacement expr);

	/**
	 * Clears all bindings established so far.
	 */
	void clear();

}
