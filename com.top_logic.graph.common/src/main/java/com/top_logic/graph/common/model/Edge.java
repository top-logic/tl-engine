/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model;

import com.top_logic.graph.common.model.styles.EdgeStyle;

/**
 * An edge between two {@link Node}s in a {@link GraphModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Edge extends LabelOwner {

	/**
	 * Name of {@link #getEdgeStyle()} property.
	 */
	String EDGE_STYLE = "edgeStyle";

	/**
	 * The source node this {@link Edge} starts from.
	 */
	Node getSource();

	/** @see #getSource() */
	void setSource(Node source);

	/**
	 * The destination node, this {@link Edge} points to.
	 */
	Node getDestination();

	/** @see #getDestination() */
	void setDestination(Node destination);

	/**
	 * The style for rendering this {@link Edge}.
	 */
	EdgeStyle getEdgeStyle();

	/**
	 * @see #getEdgeStyle()
	 */
	void setEdgeStyle(EdgeStyle value);

}
