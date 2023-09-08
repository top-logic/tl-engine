/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

/**
 * Simple node implementation for test cases.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class Node {
	private int _nr;
	private Node _parent;
	private Node[] _children;

	/**
	 * Create a new {@link Node}.
	 */
	public Node(int nr, Node[] children) {
		super();
		_nr = nr;
		_children = children;
		if (children != null) {
			for (Node node : children) {
				node._parent = this;
			}
		}
	}

	/** name / number of node */
	public int getNr() {
		return _nr;
	}

	/** child nodes */
	public Node[] getChildren() {
		return _children;
	}

	/** parent node */
	public Node getParent() {
		return _parent;
	}
}