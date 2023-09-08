/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.model;

import com.top_logic.layout.dnd.DndData;
import com.top_logic.layout.dnd.DropEvent;

/**
 * Event data of a drop operation over a graph.
 * 
 * @see GraphDropTarget#handleDrop(GraphDropEvent)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GraphDropEvent extends DropEvent {

	private final GraphData _target;

	private final double _x;

	private final double _y;

	/**
	 * Creates a {@link GraphDropEvent}.
	 *
	 * @param data
	 *        The collected data from the client-side drop operation.
	 * @param x
	 *        See {@link #getX()}.
	 * @param y
	 *        See {@link #getY()}.
	 */
	public GraphDropEvent(GraphData graph, DndData data, double x, double y) {
		super(data);
		_target = graph;
		_x = x;
		_y = y;
	}

	/**
	 * The graph over which something was dropped.
	 */
	@Override
	public GraphData getTarget() {
		return _target;
	}

	/**
	 * The X coordinate of the drop in client world coordinates.
	 */
	public double getX() {
		return _x;
	}

	/**
	 * The Y coordinate of the drop in client world coordinates.
	 */
	public double getY() {
		return _y;
	}

}
