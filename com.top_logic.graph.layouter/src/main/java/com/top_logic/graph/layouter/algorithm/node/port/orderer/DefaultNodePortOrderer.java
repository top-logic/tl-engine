/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.port.orderer;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.model.comparator.ConnectedBottomNodePortComparator;
import com.top_logic.graph.layouter.model.comparator.ConnectedTopNodePortComparator;

/**
 * Default NodePortOrder which order ports by their horizontal coordinate.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultNodePortOrderer extends NodePortOrderer {

	/**
	 * Creates a default {@link NodePortOrderer} for the given {@link LayoutDirection}.
	 */
	public DefaultNodePortOrderer(LayoutDirection direction) {
		super(direction, getTopNodePortComparator(direction), getBottomNodePortComparator(direction));
	}

	private static ConnectedBottomNodePortComparator getBottomNodePortComparator(LayoutDirection direction) {
		return new ConnectedBottomNodePortComparator(direction);
	}

	private static ConnectedTopNodePortComparator getTopNodePortComparator(LayoutDirection direction) {
		return new ConnectedTopNodePortComparator(direction);
	}

}
