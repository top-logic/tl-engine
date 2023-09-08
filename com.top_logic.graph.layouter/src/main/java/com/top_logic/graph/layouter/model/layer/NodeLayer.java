/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Collection;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Layer containing only {@link LayoutNode}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NodeLayer extends AbstractLayer<LayoutNode> {

	/**
	 * Creates a {@link NodeLayer}.
	 */
	public NodeLayer(Collection<LayoutNode> items) {
		super(items);
	}

}
