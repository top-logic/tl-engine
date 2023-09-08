/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.model;

/**
 * Behavior that implements reactions on a drop operation over a graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface GraphDropTarget {

	/**
	 * Whether drop is possible over the given target.
	 * 
	 * @param model
	 *        The potential target {@link GraphData}.
	 */
	boolean dropEnabled(GraphData model);

	/**
	 * Announces a drop operation on a graph.
	 * 
	 * @param event
	 *        Information further describing the drop details.
	 * 
	 * @see GraphDropEvent#getTarget()
	 */
	void handleDrop(GraphDropEvent event);

}
