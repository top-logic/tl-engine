/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.model;

import com.top_logic.event.infoservice.InfoService;

/**
 * {@link GraphDropTarget} that prevents all drop operations over a graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoGraphDrop implements GraphDropTarget {

	/**
	 * Singleton {@link NoGraphDrop} instance.
	 */
	public static final NoGraphDrop INSTANCE = new NoGraphDrop();

	private NoGraphDrop() {
		// Singleton constructor.
	}

	@Override
	public boolean dropEnabled(GraphData model) {
		return false;
	}

	@Override
	public void handleDrop(GraphDropEvent event) {
		InfoService.showWarning(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
	}

}
