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
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Compares connected top {@link NodePort}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ConnectedTopNodePortComparator implements Comparator<NodePort> {

	private LayoutDirection _direction;

	private BottomNodePortComparator _bottomNodePortComparator;

	/**
	 * Creates a {@link Comparator} for connected top {@link NodePort}s for the given
	 * {@link LayoutDirection}.
	 */
	public ConnectedTopNodePortComparator(LayoutDirection direction) {
		_direction = direction;

		_bottomNodePortComparator = new BottomNodePortComparator(_direction);
	}

	@Override
	public int compare(NodePort o1, NodePort o2) {
		Optional<NodePort> minTopNodePort1 = getMinimalConnectedTopNodePort(o1);
		Optional<NodePort> minTopNodePort2 = getMinimalConnectedTopNodePort(o2);

		if (!minTopNodePort1.isPresent() || !minTopNodePort2.isPresent()) {
			throw new RuntimeException();
		}

		return _bottomNodePortComparator.compare(minTopNodePort1.get(), minTopNodePort2.get());
	}

	private Optional<NodePort> getMinimalConnectedTopNodePort(NodePort port) {
		return getConnectedPresentTopNodePortStream(port).min(_bottomNodePortComparator);
	}

	private Stream<NodePort> getConnectedPresentTopNodePortStream(NodePort port) {
		return getConnectedTopNodePortStream(port).filter(Optional::isPresent).map(Optional::get);
	}

	private Stream<Optional<NodePort>> getConnectedTopNodePortStream(NodePort port) {
		return LayoutGraphUtil.getAttachedEdgesStream(port).map(getEdgeTopNodePortMap());
	}

	private Function<? super LayoutEdge, ? extends Optional<NodePort>> getEdgeTopNodePortMap() {
		return edge -> LayoutGraphUtil.getTopNodePort(_direction, edge);
	}
}
