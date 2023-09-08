/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * {@link Node} that binds to a part of the pattern during a {@link Node#match(Match, Node)}.
 * 
 * <p>
 * A {@link Capture} is a free variable in a tree pattern that is bound by its first match to a
 * target node. If a {@link Capture} occurs more than once in a tree pattern, it ensures that all
 * subtrees that are matched by the same {@link Capture} are structurally identical.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Capture extends EmptyNode {

	private final String _name;

	/**
	 * Creates a {@link Capture}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 */
	public Capture(String name) {
		_name = name;
	}

	/**
	 * Name of the {@link Capture}.
	 */
	public String getName() {
		return _name;
	}

	@Override
	public void appendTo(StringBuilder buffer) {
		buffer.append('<');
		buffer.append(getName());
		buffer.append('>');
	}

}
