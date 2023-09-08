/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.ordering;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.crossing.LayerCrossingMinimizer;
import com.top_logic.graph.layouter.algorithm.crossing.LayerDownCrossingMinimizer;
import com.top_logic.graph.layouter.algorithm.crossing.LayerUpCrossingMinimizer;
import com.top_logic.graph.layouter.algorithm.layering.LayerConstants;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.layer.DefaultAlternatingLayer;
import com.top_logic.graph.layouter.model.layer.SegmentContainer;
import com.top_logic.graph.layouter.model.layer.UnorderedNodeLayer;

/**
 * Finds an ordering for the given layered graph, i.e. order each layer of nodes w.r.t. minimize the
 * edge crossings between two layers.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class EiglspergerLayerOrderingFinder extends LayerOrderingFinder {

	private Set<LayoutEdge> _crossingType1Edges = new LinkedHashSet<>();

	private Set<LayoutEdge> _currentCrossingType1Edges = new LinkedHashSet<>();

	private Map<Integer, DefaultAlternatingLayer> _currentLayers = new LinkedHashMap<>();

	private Map<Integer, DefaultAlternatingLayer> _layers = new LinkedHashMap<>();

	private LayerCrossingMinimizer _upMinimizer;

	private LayerCrossingMinimizer _downMinimizer;

	private LayoutDirection _direction;

	/**
	 * Creates a {@link LayerOrderingFinder} for the given graph and direction.
	 */
	public EiglspergerLayerOrderingFinder(LayoutGraph graph, LayoutDirection direction) {
		super(graph);

		_direction = direction;
		_upMinimizer = new LayerUpCrossingMinimizer(_direction);
		_downMinimizer = new LayerDownCrossingMinimizer(_direction);
	}

	@Override
	public Map<Integer, DefaultAlternatingLayer> getLayerOrdering(Map<Integer, UnorderedNodeLayer> layering) {
		if (layering.size() > 1) {
			setCurrentLayer(LayerConstants.FIRST_LAYER, getFirstAlternatingLayer(layering));

			int bestCrossingsNumber = Integer.MAX_VALUE;
			int currentCrossingNumber = Integer.MAX_VALUE - 1;

			while (currentCrossingNumber < bestCrossingsNumber) {
				bestCrossingsNumber = currentCrossingNumber;
				setBestFoundOrdering();

				for (int i = 1; i <= 10; i++) {
					_currentCrossingType1Edges = new LinkedHashSet<>();

					if (i % 2 != 0) {
						currentCrossingNumber = sweepDown(layering, _downMinimizer);
					} else {
						currentCrossingNumber = sweepUp(layering, _upMinimizer);
					}
				}
			}

			return _layers;
		} else if (layering.size() == 1) {
			DefaultAlternatingLayer layer = getFirstAlternatingLayer(layering);

			return new MapBuilder<Integer, DefaultAlternatingLayer>(true).put(LayerConstants.FIRST_LAYER, layer)
				.toMap();
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * Sweeps down. Fix the first layer as reference and order the lower layer. Next, fix the fresh
	 * ordered layer and repeat the finding of an ordering for the next lower layer till the last
	 * layer is reached and ordered.
	 */
	public int sweepDown(Map<Integer, UnorderedNodeLayer> layering, LayerCrossingMinimizer downMinimizer) {
		DefaultAlternatingLayer workingLayer = getCurrentLayer(LayerConstants.FIRST_LAYER);
		int crossingNumber = 0;

		for (int i = LayerConstants.FIRST_LAYER; i <= layering.size() - 1; i++) {
			List<LayoutNode> nodes = getCurrentLayerNodes(layering, i + 1);

			setCurrentLayer(i, getLayerCopy(workingLayer));

			DefaultAlternatingLayer newLayer = downMinimizer.getMinCrossingLayer(workingLayer, nodes);
			crossingNumber += downMinimizer.getCrossingNumber();

			workingLayer = newLayer;

			addCrossingEdges(downMinimizer);
		}

		setCurrentLayer(layering.size(), workingLayer);

		return crossingNumber;
	}

	/**
	 * Sweeps up. Fix the last layer as reference and order the upper layer. Next, fix the fresh
	 * ordered layer and repeat the finding of an ordering for the next upper layer till the first
	 * layer is reached and ordered.
	 */
	public int sweepUp(Map<Integer, UnorderedNodeLayer> layering, LayerCrossingMinimizer upMinimizer) {
		DefaultAlternatingLayer workingLayer = getCurrentLayer(layering.size());
		int crossingNumber = 0;

		for (int i = layering.size(); i >= LayerConstants.FIRST_LAYER + 1; i--) {
			List<LayoutNode> nodes = getCurrentLayerNodes(layering, i - 1);

			setCurrentLayer(i, getLayerCopy(workingLayer));

			DefaultAlternatingLayer newLayer = upMinimizer.getMinCrossingLayer(workingLayer, nodes);
			crossingNumber += upMinimizer.getCrossingNumber();

			workingLayer = newLayer;

			addCrossingEdges(upMinimizer);
		}

		setCurrentLayer(LayerConstants.FIRST_LAYER, workingLayer);

		return crossingNumber;
	}

	private void setBestFoundOrdering() {
		_crossingType1Edges = _currentCrossingType1Edges;
		_layers = getLayeringCopy(_currentLayers);
	}

	private List<LayoutNode> getCurrentLayerNodes(Map<Integer, UnorderedNodeLayer> layering, int i) {
		return getCurrentLayer(i) != null ? getCurrentLayer(i).getNodes() : new LinkedList<>(layering.get(i).getAll());
	}

	private void addCrossingEdges(LayerCrossingMinimizer crossingMinimizer) {
		addConflictingType1Edges((crossingMinimizer.getCrossingTyp1Edges()));
	}

	private void addConflictingType1Edges(Set<LayoutEdge> edges) {
		_currentCrossingType1Edges.addAll(edges);
	}

	private Map<Integer, DefaultAlternatingLayer> getLayeringCopy(Map<Integer, DefaultAlternatingLayer> layering) {
		return layering.entrySet().stream().collect(LinkedHashMap::new, getLayeringCopyAccumulator(), Map::putAll);
	}

	private BiConsumer<LinkedHashMap<Integer, DefaultAlternatingLayer>, ? super Entry<Integer, DefaultAlternatingLayer>> getLayeringCopyAccumulator() {
		return (map, entry) -> map.put(entry.getKey(), entry.getValue().copy());
	}

	private DefaultAlternatingLayer getLayerCopy(DefaultAlternatingLayer layer) {
		return layer.copy();
	}

	private void setCurrentLayer(int i, DefaultAlternatingLayer layer) {
		_currentLayers.put(i, layer);
	}

	private DefaultAlternatingLayer getCurrentLayer(int i) {
		return _currentLayers.get(i);
	}

	private DefaultAlternatingLayer getFirstAlternatingLayer(Map<Integer, UnorderedNodeLayer> layering) {
		return getSimpleAlternatingLayer(getLayerNodes(layering, LayerConstants.FIRST_LAYER));
	}

	private Collection<LayoutNode> getLayerNodes(Map<Integer, UnorderedNodeLayer> layering, int layer) {
		return layering.get(layer).getAll();
	}

	private DefaultAlternatingLayer getSimpleAlternatingLayer(Collection<LayoutNode> nodes) {
		return new DefaultAlternatingLayer(nodes.stream().map(getSimpleAlternatingLayerItem()).collect(Collectors.toList()));
	}

	private Function<? super LayoutNode, ? extends Pair<LayoutNode, SegmentContainer>> getSimpleAlternatingLayerItem() {
		return node -> new Pair<>(node, new SegmentContainer());
	}

	@Override
	public Set<LayoutEdge> getCrossingType1Edges() {
		return _crossingType1Edges;
	}

}
