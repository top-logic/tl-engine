/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import java.util.Collection;

import com.top_logic.graph.common.model.layout.GraphLayout;

/**
 * Graph model shared between client and server.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphModel extends TagOwner {

	/**
	 * Property name of {@link #isEditing()}.
	 */
	String EDITING = "editing";

	/**
	 * Name of {@link #getSelectedGraphParts()} property.
	 */
	public final static String SELECTED_GRAPH_PARTS = "selectedGraphParts";

	/**
	 * Property name of {@link #getLayout()}.
	 */
	String LAYOUT = "layout";

	/**
	 * Whether this graph is in edit mode.
	 */
	boolean isEditing();

	/**
	 * @see #isEditing()
	 */
	void setEditMode(boolean editing);

	/**
	 * Creates a {@link Node} in this {@link GraphModel}.
	 * 
	 * @param parent
	 *        The parent group node for which a new child node should be created, or
	 *        <code>null</code> to create a top-level node.
	 * @param tag
	 *        The initial {@link #getTag()} to assign to the new node (before informing listeners
	 *        about the node creation).
	 * 
	 * @return The newly created {@link Node}.
	 */
	Node createNode(Node parent, Object tag);

	/**
	 * Looks up the {@link Node} in this {@link GraphModel} with the given {@link Node#getTag()
	 * tag}.
	 */
	Node getNode(Object tag);

	/**
	 * Looks up the {@link GraphPart} in this {@link GraphModel} with the given {@link GraphPart#getTag()
	 * tag}.
	 */
	GraphPart getGraphPart(Object tag);

	/**
	 * Removes the given {@link GraphPart} from this {@link GraphModel}.
	 */
	void removeGraphPart(GraphPart graphPart);

	/**
	 * Removes the given {@link Node} from this {@link GraphModel}.
	 * 
	 * <p>
	 * If the given node is still linked to other nodes, these {@link Edge}s are deleted also.
	 * </p>
	 * 
	 * @param node
	 *        The node to remove.
	 */
	void remove(Node node);

	/**
	 * All nodes in this {@link GraphModel}.
	 */
	Collection<? extends Node> getNodes();

	/**
	 * Creates an {@link Edge} between the given {@link Node nodes}.
	 * 
	 * @param source
	 *        The source node of the new {@link Edge}.
	 * @param dest
	 *        The destination node of the new {@link Edge}.
	 * @param tag
	 *        The initial {@link #getTag()} to assign to the newly created {@link Edge} before
	 *        informing {@link GraphListener}s about the creation.
	 * @return the newly created {@link Edge}.
	 */
	Edge createEdge(Node source, Node dest, Object tag);

	/**
	 * Looks up the {@link Edge} in this {@link GraphModel} with the given {@link Edge#getTag()
	 * tag}.
	 */
	Edge getEdge(Object tag);

	/**
	 * Removes the given {@link Edge} from this {@link GraphModel}.
	 * 
	 * @param edge
	 *        The edge to remove.
	 */
	void remove(Edge edge);

	/**
	 * Drops all nodes and edges and graph listeners in this graph resulting in an empty model.
	 */
	void reset();

	/**
	 * The graph layout configuration to use for displaying the graph.
	 */
	GraphLayout getLayout();

	/**
	 * @see #getLayout()
	 */
	void setLayout(GraphLayout layout);

	/**
	 * Adds an observer to this {@link GraphModel}.
	 * 
	 * @param listener
	 *        The observer that should be informed about changes.
	 */
	void addGraphListener(GraphListener listener);

	/**
	 * Removes the given {@link GraphListener}.
	 * 
	 * @see #addGraphListener(GraphListener)
	 * 
	 * @param listener
	 *        The listener to remove from this {@link GraphModel}.
	 */
	void removeGraphListener(GraphListener listener);

	/**
	 * {@link GraphPart}s of the corresponding selected diagramjs graph components.
	 */
	Collection<? extends GraphPart> getSelectedGraphParts();

	/**
	 * @see #getSelectedGraphParts()
	 */
	void setSelectedGraphParts(Collection<? extends GraphPart> selectedGraphParts);

}
