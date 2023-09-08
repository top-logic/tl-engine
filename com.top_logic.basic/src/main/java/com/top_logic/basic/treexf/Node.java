/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;


/**
 * Generic representation of a part of an expression useful for transformations.
 * 
 * @see TransformFactory Creating nodes.
 * @see Transformation Performing transformations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Node {

	/**
	 * Kind of {@link Node}.
	 * 
	 * @see #EXPR
	 * @see #VALUE
	 */
	public enum Kind {

		/**
		 * A {@link Node} of {@link Node#kind()} expression represents a member of the wrapped
		 * expression language.
		 */
		EXPR,

		/**
		 * A {@link Node} of {@link Node#kind()} value represents a primitive value of a property of
		 * a node in the wrapped expression language.
		 */
		VALUE;

	}

	/**
	 * The {@link Kind} of this Node.
	 */
	Kind kind();

	/**
	 * The number of {@link #getChild(int) children} of this {@link Node}.
	 */
	int getSize();

	/**
	 * The {@link Node} child with the given index.
	 */
	Node getChild(int index);

	/**
	 * Updates the {@link Node} child at the given index.
	 */
	void setChild(int index, Node child);

	/**
	 * Finds the first match of the given pattern within the subtree rooted at this {@link Node}.
	 * 
	 * @param match
	 *        Current bindings of {@link Capture}s.
	 * @param pattern
	 *        The pattern to match.
	 * @return The top-level node within the subtree rooted at this node that matches the given
	 *         pattern, or <code>null</code> if no match is found.
	 */
	Node find(Match match, Node pattern);

	/**
	 * Whether this pattern matches exactly the given {@link Node}.
	 * 
	 * @param match
	 *        Bindings of {@link Capture}s being performed during the match.
	 * @param node
	 *        The node to match this pattern against.
	 * @return Whether the match was successful.
	 */
	boolean match(Match match, Node node);

	/**
	 * Expands a replacement {@link Node} using the bindings of a former
	 * {@link #match(Match, Node)}.
	 * 
	 * @param match
	 *        The bindings to expand.
	 * @return The newly created expansion {@link Node}.
	 */
	Node expand(Match match);

	/**
	 * Implementation of {@link Object#toString()}.
	 * 
	 * @param buffer
	 *        The buffer to write to.
	 */
	void appendTo(StringBuilder buffer);

}
