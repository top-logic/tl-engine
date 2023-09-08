/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.layer;

import java.util.Collection;
import java.util.LinkedHashSet;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * {@link UnorderedLayer} of {@link LayoutNode}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UnorderedNodeLayer extends NodeLayer {

	/**
	 * Creates a {@link UnorderedNodeLayer} for the given {@link Collection} of
	 * {@link LayoutNode}s.
	 */
	public UnorderedNodeLayer(Collection<LayoutNode> items) {
		super(new LinkedHashSet<>(items));
	}
}
