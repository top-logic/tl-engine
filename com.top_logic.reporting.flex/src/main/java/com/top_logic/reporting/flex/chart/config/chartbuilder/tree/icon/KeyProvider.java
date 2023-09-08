/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import com.top_logic.reporting.flex.chart.config.model.ChartNode;

/**
 * Abstraction-layer to get a key for an image of a {@link ChartNode}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class KeyProvider {

	/**
	 * @param node
	 *        the {@link ChartNode} from the universal chart-model
	 * @return the key to request an corresponding icon at the {@link ValueIconProvider}
	 */
	public Object getKey(ChartNode node) {
		if (node == null) {
			return null;
		}
		return node.getValue().intValue();
	}

}