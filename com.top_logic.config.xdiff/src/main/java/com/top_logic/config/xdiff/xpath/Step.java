/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.config.xdiff.xpath;

/**
 * A step in a XPath {@link LocationPath}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Step extends XPathNode implements Cloneable {
	private Axis _axis;

	private NodeTest _nodeTest;

	private int _position;

	Step(Axis axis, NodeTest nodeTest, int position) {
		super();
		setAxis(axis);
		setNodeTest(nodeTest);
		setPosition(position);
	}

	@Override
	public Step clone() {
		return new Step(_axis, _nodeTest, _position);
	}

	/**
	 * The {@link Axis} of this {@link Step}.
	 */
	public Axis getAxis() {
		return _axis;
	}

	/**
	 * Setter for {@link #getAxis()}.
	 */
	public void setAxis(Axis axis) {
		_axis = axis;
	}

	/**
	 * The {@link NodeTest} of this {@link Step}.
	 */
	public NodeTest getNodeTest() {
		return _nodeTest;
	}

	/**
	 * Setter for {@link #getNodeTest()}.
	 */
	public void setNodeTest(NodeTest nodeTest) {
		_nodeTest = nodeTest;
	}

	/**
	 * Simplified predicate of this {@link Step}.
	 * 
	 * <p>
	 * The only predicate supported is <code>[positon() = {@link #getPosition() p}]</code>.
	 * </p>
	 */
	public int getPosition() {
		return _position;
	}

	/**
	 * Setter for {@link #getPosition()}.
	 */
	public void setPosition(int position) {
		_position = position;
	}

	@Override
	public <R, A> R visit(XPathVisitor<R, A> v, A arg) {
		return v.visitStep(this, arg);
	}

}