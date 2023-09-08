/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles.view;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.graph.common.model.styles.PolylineEdgeStyle;

/**
 * Stroke style for edges.
 * 
 * @see PolylineEdgeStyle#getStroke()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Stroke {

	/**
	 * The fill color of the line.
	 */
	@Name("fill")
	Fill getFill();

	/**
	 * The dash style of the line.
	 */
	@Name("dashStyle")
	DashStyle getDashStyle();

	/**
	 * The dash cap.
	 */
	@Name("lineCap")
	LineCap getLineCap();

	/**
	 * The dash and bend join.
	 */
	@Name("lineJoin")
	LineJoin getLineJoin();

	/**
	 * The miter limit.
	 */
	@Name("miterLimit")
	double getMiterLimit();

	/**
	 * Thickness of the line.
	 */
	@Name("thickness")
	@DoubleDefault(1.0)
	double getThickness();

}
