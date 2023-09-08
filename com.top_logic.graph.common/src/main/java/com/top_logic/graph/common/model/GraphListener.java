/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

/**
 * Listener interface for observing changes to a {@link GraphModel}.
 * 
 * @see GraphModel#addGraphListener(GraphListener)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphListener {

	/**
	 * Informs about a change in a {@link GraphModel}.
	 * 
	 * @param event
	 *        The change information.
	 * 
	 * @see GraphEvent#getGraph()
	 */
	void handleGraphEvent(GraphEvent<?> event);

}
