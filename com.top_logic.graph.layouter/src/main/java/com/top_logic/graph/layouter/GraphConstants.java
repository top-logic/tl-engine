/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter;

/**
 * Configuration options for graphs.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface GraphConstants {
	/**
	 * General scale factor for all graph component positions.
	 */
	public final static int SCALE = 20;

	/**
	 * Width of an edge marker.
	 */
	public final static int EDGE_MARKER_WIDTH = 10;

	/**
	 * Offset for node labels.
	 */
	public final static int NODE_LABEL_OFFSET = 5;
}
