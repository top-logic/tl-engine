/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * Base class for {@link Node} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractNode implements Node {

	@Override
	public Node find(Match match, Node pattern) {
		if (pattern.match(match, this)) {
			return this;
		}
		return findDescend(match, pattern);
	}

	private Node findDescend(Match match, Node pattern) {
		for (int n = 0, cnt = getSize(); n < cnt; n++) {
			Node child = getChild(n);
			Node result = child.find(match, pattern);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	@Override
	public final boolean match(Match match, Node node) {
		if (matchesKind(node)) {
			return internalMatch(match, node);
		} else {
			return false;
		}
	}

	private boolean matchesKind(Node other) {
		return other.kind() == kind();
	}

	/**
	 * Implementation of {@link #match(Match, Node)} assuming that {@link #kind()} does match.
	 */
	protected abstract boolean internalMatch(Match match, Node node);

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		appendTo(result);
		return result.toString();
	}

}
