/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.top_logic.layout.processor.Expansion.Buffer;

/**
 * Value of a layout parameter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ParameterValue {

	private Expansion _expansion;

	private final String _paramName;

	private ParameterValue _fallback;

	/**
	 * Creates a {@link ParameterValue}.
	 * 
	 * @param paramName
	 *        See {@link #getParamName()}
	 */
	public ParameterValue(String paramName) {
		_paramName = paramName;
	}

	/**
	 * The name this value is associated with.
	 */
	public String getParamName() {
		return _paramName;
	}

	/**
	 * Whether this value can be expanded.
	 * 
	 * @see #expand(Buffer)
	 */
	public boolean isDefined() {
		return true;
	}

	/**
	 * The {@link Node}s to insert replacing a variable reference.
	 * 
	 * @param out
	 *        The buffer to write the result to.
	 * 
	 * @return The created nodes, or {@link Expansion#NO_NODE_EXPANSION} in case the value to expand
	 *         contains an optional parameter without explicit given value.
	 */
	public final List<? extends Node> expand(Buffer out) throws ElementInAttributeContextError {
		List<? extends Node> expandedNodes = internalExpand(out);
		if (expandedNodes == Expansion.NO_NODE_EXPANSION) {
			ParameterValue fallback = fallback();
			if (fallback != null) {
				expandedNodes = fallback.expand(out);
			}
		}
		return expandedNodes;
	}

	/**
	 * Actual implementation of {@link #expand(Buffer)}.
	 * 
	 * @param out
	 *        The buffer to write the result to.
	 * 
	 * @return The created nodes.
	 */
	protected abstract List<? extends Node> internalExpand(Buffer out) throws ElementInAttributeContextError;

	protected static Document owner(Node parent) {
		if (parent.getNodeType() == Node.DOCUMENT_NODE) {
			return (Document) parent;
		}
		return parent.getOwnerDocument();
	}

	public void initExpansion(Expansion expansion) {
		_expansion = expansion;
	}

	public Expansion expansion() {
		return _expansion;
	}

	public ParameterValue fallback() {
		return _fallback;
	}

	public void setFallback(ParameterValue fallback) {
		_fallback = fallback;
	}

}