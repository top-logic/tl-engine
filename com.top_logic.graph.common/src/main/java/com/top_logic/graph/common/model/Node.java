/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import java.util.Collection;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.graph.common.model.styles.NodeStyle;

/**
 * A node in a {@link GraphModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Node extends LabelOwner {

	/**
	 * Name of {@link #getNodeStyle()} property.
	 */
	String NODE_STYLE = "nodeStyle";

	/**
	 * Name of the {@link #getParent()} property.
	 */
	String PARENT = "parent";

	/**
	 * Name of the {@link #getExpansion()} property.
	 */
	String EXPANSION = "expansion";

	/** Name of the {@link #getShowSuccessors()} property. */
	String SHOW_SUCCESSORS = "showSuccessors";

	/** Name of the {@link #getShowPredecessors()} property. */
	String SHOW_PREDECESSORS = "showPredecessors";

	/**
	 * Name of the {@link #getX()} property.
	 */
	String X = "x";

	/**
	 * Name of the {@link #getY()} property.
	 */
	String Y = "y";

	/**
	 * Name of the {@link #getWidth()} property.
	 */
	String WIDTH = "width";

	/**
	 * Name of the {@link #getHeight()} property.
	 */
	String HEIGHT = "height";

	/**
	 * The {@link Edge}s that start at this {@link Node}.
	 */
	Collection<? extends Edge> getOutgoingEdges();

	/**
	 * The {@link Edge}s that point to this {@link Node}.
	 */
	Collection<? extends Edge> getIncomingEdges();

	/**
	 * Removes all {@link Edge}s from this nodes.
	 * 
	 * @see #getIncomingEdges()
	 * @see #getOutgoingEdges()
	 */
	void clear();

	/**
	 * The style for rendering this node.
	 */
	NodeStyle getNodeStyle();

	/**
	 * @see #getNodeStyle()
	 */
	void setNodeStyle(NodeStyle nodeStyle);

	/**
	 * The parent node of a group node, or <code>null</code> for a top-level node.
	 */
	Node getParent();

	/**
	 * Makes this {@link Node} a {@link #getContents() content} of the given group node.
	 * 
	 * @see #getParent()
	 */
	void setParent(Node group);

	/**
	 * The {@link Node}s contained in this group node.
	 * 
	 * <p>
	 * All resulting {@link Node}s return this {@link Node} as its {@link #getParent()}.
	 * </p>
	 * 
	 * @return The read-only contents of this {@link Node}, empty for non-group nodes.
	 */
	Collection<? extends Node> getContents();

	/**
	 * Whether this group node is displaying its {@link #getContents()}.
	 */
	boolean getExpansion();

	/**
	 * @see #getExpansion()
	 */
	void setExpansion(boolean value);

	/** Whether the direct predecessor nodes should be displayed. */
	@Name(SHOW_SUCCESSORS)
	boolean getShowSuccessors();

	/** @see #getShowSuccessors() */
	void setShowSuccessors(boolean value);

	/** Whether the direct successor nodes should be displayed. */
	@Name(SHOW_PREDECESSORS)
	boolean getShowPredecessors();

	/** @see #getShowPredecessors() */
	void setShowPredecessors(boolean value);

	/**
	 * The X coordinate in client world coordinates.
	 */
	double getX();

	/**
	 * @see #getX()
	 */
	void setX(double value);

	/**
	 * The Y coordinate in client world coordinates.
	 */
	double getY();

	/**
	 * @see #getY()
	 */
	void setY(double value);

	/**
	 * The width of the client-side node display in client world coordinates.
	 */
	double getWidth();

	/**
	 * @see Node#getWidth()
	 */
	void setWidth(double value);

	/**
	 * The height of the client-side node display in client world coordinates.
	 */
	double getHeight();

	/**
	 * @see #getHeight()
	 */
	void setHeight(double value);
}
