/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.layout;

import com.top_logic.graph.common.model.Edge;

/**
 * A position on an {@link Edge}, e.g. for displaying a label.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum EdgePosition {

	/**
	 * In the middle between the source and target node.
	 */
	CENTER,

	/**
	 * Near the source node.
	 * 
	 * @see Edge#getSource()
	 */
	SOURCE,

	/**
	 * Near the destination node.
	 * 
	 * @see Edge#getDestination()
	 */
	TARGET,

	;

}
