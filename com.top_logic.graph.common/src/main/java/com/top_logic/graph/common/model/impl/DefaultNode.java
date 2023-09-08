/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.styles.CollapsibleNodeStyle;
import com.top_logic.graph.common.model.styles.NodeStyle;

/**
 * {@link DefaultSharedObject} {@link Node} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultNode extends DefaultLabelOwner implements Node {

	/** @see #isCollapsible() */
	private static final String COLLAPSIBLE = "collapsible";

	/**
	 * Creates a {@link DefaultNode}.
	 * <p>
	 * This method has to be public, as it is called via reflection from the
	 * {@link ReflectionFactory}.
	 * </p>
	 *
	 * @param scope
	 *        See {@link DefaultSharedObject#DefaultSharedObject(ObjectScope)}.
	 */
	@CalledByReflection
	public DefaultNode(ObjectScope scope) {
		super(scope);
	}

	@Override
	public NodeStyle getNodeStyle() {
		return get(NODE_STYLE);
	}

	@Override
	public void setNodeStyle(NodeStyle value) {
		set(NODE_STYLE, value);
	}

	@Override
	public Collection<? extends DefaultEdge> getOutgoingEdges() {
		return getReferrers(DefaultEdge.class, DefaultEdge.SRC);
	}

	@Override
	public Collection<? extends DefaultEdge> getIncomingEdges() {
		return getReferrers(DefaultEdge.class, DefaultEdge.DST);
	}

	@Override
	public DefaultNode getParent() {
		return get(PARENT);
	}

	@Override
	public void setParent(Node group) {
		set(PARENT, group);
	}

	@Override
	public Collection<? extends DefaultNode> getContents() {
		return getReferrers(DefaultNode.class, PARENT);
	}

	@Override
	public boolean getExpansion() {
		return getDataBoolean(EXPANSION, true);
	}

	@Override
	public void setExpansion(boolean value) {
		setData(EXPANSION, Boolean.valueOf(value));
	}

	@Override
	public boolean getShowSuccessors() {
		return getDataBoolean(SHOW_SUCCESSORS);
	}

	@Override
	public void setShowSuccessors(boolean value) {
		setData(SHOW_SUCCESSORS, value);
	}

	@Override
	public boolean getShowPredecessors() {
		return getDataBoolean(SHOW_PREDECESSORS);
	}

	@Override
	public void setShowPredecessors(boolean value) {
		setData(SHOW_PREDECESSORS, value);
	}

	@Override
	public void clear() {
		clearEdges(getIncomingEdges());
		clearEdges(getOutgoingEdges());
	}

	private void clearEdges(Collection<? extends DefaultEdge> edgesView) {
		for (DefaultEdge edge : new ArrayList<>(edgesView)) {
			edge.delete();
		}
	}

	@Override
	protected void onDelete() {
		super.onDelete();
		clear();
		getGraph().removeInternal(this);
	}

	@Override
	public double getX() {
		return getDataDouble(X);
	}

	@Override
	public void setX(double value) {
		setData(X, value);
	}

	@Override
	public double getY() {
		return getDataDouble(Y);
	}

	@Override
	public void setY(double value) {
		setData(Y, value);
	}

	@Override
	public double getWidth() {
		return getDataDouble(WIDTH);
	}

	@Override
	public void setWidth(double value) {
		setData(WIDTH, value);
	}

	@Override
	public double getHeight() {
		return getDataDouble(HEIGHT);
	}

	@Override
	public void setHeight(double value) {
		setData(HEIGHT, value);
	}

	@Override
	protected void handleAttributeUpdate(String property) {
		super.handleAttributeUpdate(property);

		if (property.equals(NODE_STYLE)) {
			updateCollapsible();
		}
	}

	/** See: {@link #isCollapsible()} */
	protected void updateCollapsible() {
		Object nodeStyle = get(NODE_STYLE);
		set(COLLAPSIBLE, nodeStyle instanceof CollapsibleNodeStyle);
	}

	/**
	 * Workaround to detect whether a node can be collapsed or not on the client. The problem is,
	 * that {@link #getNodeStyle()} never returns the <code>YFCollapsibleNodeStyle</code> on the
	 * client. That makes it impossible to test with
	 * <code>yfNode.getNodeStyle() instance YFCollapsibleNodeStyle</code> whether a node is
	 * collapsible or not.
	 */
	public boolean isCollapsible() {
		return get(COLLAPSIBLE);
	}

}
