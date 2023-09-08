/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.partition;

import java.util.Collection;
import java.util.List;

import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Layer partition for {@link Line1DContainer}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ContainerLayerPartition extends LayerPartition<Line1DContainer> {

	/**
	 * Creates a {@link ContainerLayerPartition}.
	 */
	public ContainerLayerPartition(List<Collection<Line1DContainer>> partition) {
		super(partition);
	}

}
