/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.graph.layouter.Sugiyama;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link AlternatingLayer} used by this implementation of {@link Sugiyama}. Alternated between
 * {@link LayoutNode}s and {@link SegmentContainer}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultAlternatingLayer extends AlternatingLayer<LayoutNode, SegmentContainer> {

	private SegmentContainer _firstSegmentContainer = new SegmentContainer();

	/**
	 * Creates a {@link DefaultAlternatingLayer}.
	 */
	public DefaultAlternatingLayer() {
		super();
	}

	/**
	 * Creates a {@link DefaultAlternatingLayer} for the given {@code items}.
	 */
	public DefaultAlternatingLayer(List<Pair<LayoutNode, SegmentContainer>> items) {
		super(items);
	}

	/**
	 * First {@link SegmentContainer} of this layer.
	 */
	public SegmentContainer getFirstSegmentContainer() {
		return _firstSegmentContainer;
	}

	/**
	 * @see #getFirstSegmentContainer()
	 */
	public void setFirstSegmentContainer(SegmentContainer segmentContainer) {
		_firstSegmentContainer = segmentContainer;
	}

	/**
	 * All {@link SegmentContainer}s for this layer.
	 */
	public List<SegmentContainer> getContainers() {
		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = getAll().iterator();
		List<SegmentContainer> containers = new LinkedList<>();

		containers.add(getFirstSegmentContainer());

		while (iterator.hasNext()) {
			containers.add(iterator.next().getSecond());
		}

		return containers;
	}

	/**
	 * All {@link LayoutNode}s for this layer.
	 */
	public List<LayoutNode> getNodes() {
		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = getAll().iterator();
		List<LayoutNode> nodes = new LinkedList<>();

		while (iterator.hasNext()) {
			nodes.add(iterator.next().getFirst());
		}

		return nodes;
	}

	/**
	 * Copy an alternating layer. The {@link SegmentContainer} is replaced with a new segment
	 * container with the same content (same set of {@link LayoutEdge}).
	 */
	public DefaultAlternatingLayer copy() {
		Iterator<Pair<LayoutNode, SegmentContainer>> iterator = getAll().iterator();

		DefaultAlternatingLayer newLayer = new DefaultAlternatingLayer();

		newLayer.setFirstSegmentContainer(new SegmentContainer(getFirstSegmentContainer()));

		while (iterator.hasNext()) {
			Pair<LayoutNode, SegmentContainer> item = iterator.next();

			newLayer.add(new Pair<>(item.getFirst(), new SegmentContainer(item.getSecond())));
		}

		return newLayer;
	}
}
