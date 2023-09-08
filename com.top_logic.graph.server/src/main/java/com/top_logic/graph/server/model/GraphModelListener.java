/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.model;

import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.graph.common.model.impl.SharedGraph;

/**
 * {@link PropertyListener} observing the {@link GraphData#getGraph()} property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphModelListener extends PropertyListener {

	/**
	 * Informs of a change to the {@link GraphData#getGraph()} property.
	 * 
	 * @param sender
	 *        The changed {@link GraphData}.
	 * @param oldValue
	 *        The old graph.
	 * @param newValue
	 *        The new graph.
	 */
	void handleGraphChange(GraphData sender, SharedGraph oldValue, SharedGraph newValue);

}
