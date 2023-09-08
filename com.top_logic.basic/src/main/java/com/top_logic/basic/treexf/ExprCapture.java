/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * {@link Capture} that matches any {@link Expr} node.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExprCapture extends Capture {

	/**
	 * Creates a {@link ExprCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	protected ExprCapture(String name) {
		super(name);
	}

	@Override
	public Kind kind() {
		return Kind.EXPR;
	}

	@Override
	protected final boolean internalMatch(Match match, Node node) {
		Node binding = match.getBinding(this);
		if (binding == null) {
			if (matchesNode(match, (Expr) node)) {
				match.bind(this, node);
				return true;
			} else {
				return false;
			}
		} else {
			return binding.match(match, node);
		}
	}

	/**
	 * Hook for subclasses to decide, whether the given node matches this {@link ExprCapture}.
	 * 
	 * @param match
	 *        The current bindings.
	 * @param node
	 *        The {@link Expr} to match against.
	 * @return Whether the match was successful.
	 * 
	 * @see #match(Match, Node)
	 */
	protected boolean matchesNode(Match match, Expr node) {
		return true;
	}

	@Override
	public Node expand(Match match) {
		return match.getBinding(this).expand(match);
	}

}
