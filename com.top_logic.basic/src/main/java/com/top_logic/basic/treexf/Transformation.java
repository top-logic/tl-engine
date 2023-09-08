/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

import com.top_logic.basic.Logger;

/**
 * Pattern-based {@link Node} transformation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Transformation {

	private final Node _pattern;

	private final Node _replacement;

	/**
	 * Creates a {@link Transformation}.
	 * 
	 * @param pattern
	 *        See {@link #getPattern()}.
	 * @param replacement
	 *        See {@link #getReplacement()}.
	 */
	public Transformation(Node pattern, Node replacement) {
		_pattern = pattern;
		_replacement = replacement;
	}

	/**
	 * The pattern that is matched against an input {@link Node}.
	 */
	public Node getPattern() {
		return _pattern;
	}

	/**
	 * The {@link Node} whose expansion replaces the match of the {@link #getPattern()}.
	 */
	public Node getReplacement() {
		return _replacement;
	}

	/**
	 * Matches {@link #getPattern()} against the given node and produce a {@link #getReplacement()}
	 * expansion, if the match succeeds.
	 * 
	 * @param match
	 *        Current bindings of the match.
	 * @param node
	 *        The input node to match against.
	 * @return The replacement, if the match succeeds, or a reference to the input node, otherwise.
	 */
	public Node replace(Match match, Node node) {
		match.clear();
		if (_pattern.match(match, node)) {
			Node result = _replacement.expand(match);

			if (Logger.isDebugEnabled(Transformation.class)) {
				String message = _pattern + " => " + _replacement + "\n" + node + " => " + result;
				Logger.debug(message, Transformation.class);
			}

			return result;
		} else {
			return node;
		}
	}

}
