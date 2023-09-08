/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Transforms {@link Node} expressions given a set of {@link Transformation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeTransformer {

	private final Transformation[] _transformations;

	private boolean _continue;

	/**
	 * Creates a {@link TreeTransformer}.
	 * 
	 * @param transformations
	 *        The {@link Transformation}s to apply.
	 */
	public TreeTransformer(Transformation... transformations) {
		_transformations = transformations;
	}

	/**
	 * Transforms the given {@link Node} by applying {@link Transformation}s.
	 * 
	 * @param match
	 *        Temporary space to hold a single match.
	 * @param orig
	 *        The {@link Node} to transform.
	 * @return The transformed {@link Node}.
	 */
	public Node transform(Match match, Node orig) {
		Node result = orig;
		while (true) {
			_continue = false;
			result = transformRecursive(match, result);
			if (!_continue) {
				break;
			}
		}
		return result;
	}

	private Node transformRecursive(Match matcher, Node orig) {
		if (orig == null) {
			return orig;
		}
		Node result = orig;

		descend(matcher, result);
		for (Transformation pattern : _transformations) {
			result = pattern.replace(matcher, result);
		}

		if (result != orig) {
			_continue = true;
		}

		return result;
	}

	private void descend(Match matcher, Node node) {
		for (int n = 0, cnt = node.getSize(); n < cnt; n++) {
			node.setChild(n, transformRecursive(matcher, node.getChild(n)));
		}
	}

}
