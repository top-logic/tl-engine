/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Edge between at least one {@link SegmentContainer} and an other container or a
 * {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class VirtualSegmentEdge {
	private final Object _source;

	private final Object _target;

	private final boolean _isContainerEdge;

	private final int _weight;

	@Override
	public String toString() {
		return "VirtualSegmentEdge [_source=" + _source + ", _target=" + _target + ", _isContainerEdge="
			+ _isContainerEdge + ", _weight=" + _weight + "]";
	}

	/**
	 * Creates a {@link VirtualSegmentEdge} between the given {@link SegmentContainer} and
	 * {@link LayoutNode}.
	 */
	public VirtualSegmentEdge(SegmentContainer source, LayoutNode target) {
		_source = source;
		_target = target;

		_isContainerEdge = false;

		_weight = 1;
	}

	/**
	 * Creates a {@link VirtualSegmentEdge} between the two given {@link SegmentContainer}.
	 */
	public VirtualSegmentEdge(SegmentContainer source, SegmentContainer target) {
		_source = source;
		_target = target;

		_isContainerEdge = true;

		_weight = target.size();
	}

	/**
	 * @see #VirtualSegmentEdge(SegmentContainer, LayoutNode)
	 */
	public VirtualSegmentEdge(LayoutNode source, SegmentContainer target) {
		_source = source;
		_target = target;

		_isContainerEdge = false;

		_weight = 1;
	}

	/**
	 * Source object of this edge.
	 */
	public Object source() {
		return _source;
	}

	/**
	 * Target object of this edge.
	 */
	public Object target() {
		return _target;
	}

	/**
	 * True if this edge connects two {@link SegmentContainer}, otherwise false.
	 */
	public boolean isContainerEdge() {
		return _isContainerEdge;
	}

	/**
	 * Size of the target {@link SegmentContainer} if its an edge between two
	 *         {@link SegmentContainer}, otherwise 1.
	 */
	public int getWeight() {
		return _weight;
	}
}
