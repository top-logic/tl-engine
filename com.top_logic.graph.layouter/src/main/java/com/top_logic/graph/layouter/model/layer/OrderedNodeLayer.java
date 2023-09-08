/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Collection;
import java.util.LinkedList;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link OrderedLayer} of {@link LayoutNode}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class OrderedNodeLayer extends NodeLayer {

	/**
	 * Creates a {@link OrderedNodeLayer} for the given unordered {@link UnorderedNodeLayer}.
	 */
	public OrderedNodeLayer(UnorderedNodeLayer layer) {
		super(new LinkedList<>(layer.getAll()));
	}

	/**
	 * Creates a {@link OrderedNodeLayer} for the given unordered {@link UnorderedNodeLayer}.
	 */
	public OrderedNodeLayer(Collection<LayoutNode> layer) {
		super(new LinkedList<>(layer));
	}

}
