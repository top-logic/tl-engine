/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.styles.view.Arrow;
import com.top_logic.graph.common.model.styles.view.Stroke;

/**
 * Default style for {@link Edge}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PolylineEdgeStyle extends EdgeStyle {

	/**
	 * Value for smoothing bends.
	 */
	@Name("smoothingLength")
	double getSmoothingLength();

	/**
	 * The arrow to render at the source end of the edge.
	 */
	@Name("sourceArrow")
	Arrow getSourceArrow();

	/**
	 * The arrow to render at the destination end of the edge.
	 */
	@Name("targetArrow")
	Arrow getTargetArrow();

	/**
	 * The stroke style to use for drawing the edge.
	 */
	@Name("stroke")
	Stroke getStroke();

}
