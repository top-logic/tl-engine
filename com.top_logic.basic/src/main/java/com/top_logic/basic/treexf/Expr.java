/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.treexf;

/**
 * {@link Node} that represents a structural part of the expression language under transformation.
 * 
 * @see LiteralValue
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Expr extends AbstractNode {

	private Object _type;

	private final Node[] _children;

	/**
	 * Creates a {@link Expr}.
	 * 
	 * @param type
	 *        See {@link #getType()}.
	 * @param children
	 *        The parts of this {@link Expr}.
	 */
	protected Expr(Object type, Node[] children) {
		_type = type;
		_children = children;
	}

	@Override
	public int getSize() {
		return _children.length;
	}

	@Override
	public Node getChild(int index) {
		return _children[index];
	}

	@Override
	public void setChild(int index, Node child) {
		_children[index] = child;
	}

	/**
	 * The concrete expression node type.
	 */
	public Object getType() {
		return _type;
	}

	@Override
	public Kind kind() {
		return Kind.EXPR;
	}

	@Override
	protected boolean internalMatch(Match match, Node node) {
		Expr other = (Expr) node;
		if (_type != other.getType()) {
			return false;
		}

		if (getSize() != other.getSize()) {
			return false;
		}

		for (int n = 0, cnt = getSize(); n < cnt; n++) {
			if (!getChild(n).match(match, other.getChild(n))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public Node expand(Match match) {
		return new Expr(getType(), expandChildren(match));
	}

	private Node[] expandChildren(Match matcher) {
		int cnt = getSize();
		Node[] result = new Node[cnt];
		for (int n = 0; n < cnt; n++) {
			result[n] = getChild(n).expand(matcher);
		}
		return result;
	}

	@Override
	public void appendTo(StringBuilder buffer) {
		String typeName = _type.toString();
		buffer.append(Character.toLowerCase(typeName.charAt(0)));
		buffer.append(typeName.substring(1));
		buffer.append('(');
		int cnt = getSize();
		for (int n = 0; n < cnt; n++) {
			if (n > 0) {
				buffer.append(", ");
			}
			getChild(n).appendTo(buffer);
		}
		buffer.append(')');
	}

}
