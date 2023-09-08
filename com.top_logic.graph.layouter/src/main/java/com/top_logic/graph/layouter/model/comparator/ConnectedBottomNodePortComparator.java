/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.comparator;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * {@link Comparator} for bottom {@link NodePort}s of a {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ConnectedBottomNodePortComparator implements Comparator<NodePort> {

	private LayoutDirection _direction;

	private TopNodePortComparator _topNodePortComparator;

	/**
	 * Creates a {@link NodePort} comparator for the given {@link LayoutDirection}.
	 */
	public ConnectedBottomNodePortComparator(LayoutDirection direction) {
		_direction = direction;

		_topNodePortComparator = new TopNodePortComparator(_direction);
	}

	@Override
	public int compare(NodePort port1, NodePort port2) {
		Optional<NodePort> minBottomNodePort1 = getMinimalConnectedBottomNodePort(port1);
		Optional<NodePort> minBottomNodePort2 = getMinimalConnectedBottomNodePort(port2);

		if (!minBottomNodePort1.isPresent() || !minBottomNodePort2.isPresent()) {
			throw new RuntimeException();
		}

		return _topNodePortComparator.compare(minBottomNodePort1.get(), minBottomNodePort2.get());
	}

	private Optional<NodePort> getMinimalConnectedBottomNodePort(NodePort port) {
		return getConnectedPresentBottomNodePortStream(port).min(_topNodePortComparator);
	}

	private Stream<NodePort> getConnectedPresentBottomNodePortStream(NodePort port) {
		return getConnectedBottomNodePortStream(port).filter(Optional::isPresent).map(Optional::get);
	}

	private Stream<Optional<NodePort>> getConnectedBottomNodePortStream(NodePort port) {
		return LayoutGraphUtil.getAttachedEdgesStream(port).map(getEdgeBottomNodePortMap());
	}

	private Function<? super LayoutEdge, ? extends Optional<NodePort>> getEdgeBottomNodePortMap() {
		return edge -> LayoutGraphUtil.getBottomNodePort(_direction, edge);
	}
}
