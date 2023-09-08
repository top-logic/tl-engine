/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Port where incoming or outgoing edges of this node are attached to.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NodePort {

	private double _x;

	private double _y;

	private LayoutNode _node;

	private Set<LayoutEdge> _attachedEdges = new LinkedHashSet<>();

	/**
	 * Creates a {@link NodePort} for the given node and edges.
	 * 
	 * @param node
	 *        {@link LayoutNode} where the port is part of.
	 * @param attachedEdges
	 *        {@link LayoutEdge}s that are attached to this port.
	 */
	public NodePort(LayoutNode node, Collection<LayoutEdge> attachedEdges) {
		_attachedEdges.addAll(attachedEdges);
		_node = node;
	}

	/**
	 * Creates a {@link NodePort} for the given node and edge.
	 * 
	 * @param node
	 *        {@link LayoutNode} where the port is part of.
	 * @param edge
	 *        {@link LayoutEdge} that is attached to this port.
	 */
	public NodePort(LayoutNode node, LayoutEdge edge) {
		_attachedEdges.add(edge);
		_node = node;
	}

	/**
	 * Attached edges to this port.
	 */
	public Set<LayoutEdge> getAttachedEdges() {
		return _attachedEdges;
	}

	/**
	 * Sets a new set of attached edges to this port.
	 */
	public void setAttachedEdges(Set<LayoutEdge> attachedEdges) {
		_attachedEdges = attachedEdges;
	}

	/**
	 * Checks if the given edge is attached to this port.
	 * 
	 * @return TRUE, if the given edge is attached, otherwise false.
	 */
	public boolean isAttached(LayoutEdge edge) {
		return _attachedEdges.contains(edge);
	}

	/**
	 * {@link LayoutNode} where the port is part of.
	 */
	public LayoutNode getNode() {
		return _node;
	}

	/**
	 * Detach the given {@link LayoutEdge} from this {@link NodePort}.
	 * 
	 * @return TRUE, if the given edge could be removed, otherwise false.
	 */
	public boolean detach(LayoutEdge edge) {
		return _attachedEdges.remove(edge);
	}

	/**
	 * Attach the given {@link LayoutEdge} to this {@link NodePort}.
	 */
	public void attach(LayoutEdge edge) {
		_attachedEdges.add(edge);
	}

	/**
	 * Number of {@link LayoutEdge}s that are attached to this port.
	 */
	public int size() {
		return _attachedEdges.size();
	}

	/**
	 * Absolute horizontal coordinate of this port.
	 */
	public double getX() {
		return _x;
	}

	/**
	 * @see #getX()
	 */
	public void setX(double x) {
		_x = x;
	}

	/**
	 * Absolute vertical coordinate of this port.
	 */
	public double getY() {
		return _y;
	}

	/**
	 * @see #getY()
	 */
	public void setY(double y) {
		_y = y;
	}

}