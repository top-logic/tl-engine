/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.List;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Layering for {@link LayoutNode}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public abstract class NodeLayering<L extends NodeLayer> {

	List<L> _layering;

	/**
	 * Creates a {@link NodeLayering}.
	 */
	public NodeLayering(List<L> layering) {
		_layering = layering;
	}
	
	/**
	 * Number of layers.
	 */
	public int size() {
		return _layering.size();
	}

	/**
	 * Corresponding {@link NodeLayer}.
	 */
	public L getLayer(int layer) {
		return _layering.get(layer);
	}

}
