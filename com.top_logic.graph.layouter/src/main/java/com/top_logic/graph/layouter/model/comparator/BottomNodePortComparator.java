/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.comparator;

import java.util.Comparator;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;

/**
 * Compares two bottom {@link NodePort}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class BottomNodePortComparator implements Comparator<NodePort> {

	private LayoutDirection _direction;

	/**
	 * Creates a {@link BottomNodePortComparator} for the given direction.
	 */
	public BottomNodePortComparator(LayoutDirection direction) {
		_direction = direction;
	}

	@Override
	public int compare(NodePort port1, NodePort port2) {
		double port1X = port1.getNode().getX();
		double port2X = port2.getNode().getX();

		if (port1X < port2X) {
			return -1;
		} else if (port1X == port2X) {
			if (getBottomNodePortIndex(port1) < getBottomNodePortIndex(port2)) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	private int getBottomNodePortIndex(NodePort port) {
		return LayoutGraphUtil.getBottomNodePortIndex(_direction, port);
	}

}
