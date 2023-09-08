/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Special {@link ExprCapture} that matches only if the matched {@link Node} does not contain
 * certain exclude patterns.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExcludeCapture extends ExprCapture {

	private final Node[] _excludes;

	/**
	 * Creates a {@link ExcludeCapture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param excludes
	 *        The exclude patterns that prevent a match, if they occur anywhere in the matched
	 *        {@link Node}.
	 */
	public ExcludeCapture(String name, Node... excludes) {
		super(name);
		_excludes = excludes;
	}

	@Override
	protected boolean matchesNode(Match match, Expr node) {
		for (Node exclude : _excludes) {
			if (node.find(new FinalMatch(match), exclude) != null) {
				return false;
			}
		}

		return super.matchesNode(match, node);
	}
}
