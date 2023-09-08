/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.styles;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.graph.common.model.styles.view.Fill;
import com.top_logic.graph.common.model.styles.view.Stroke;

/**
 * {@link NodeStyle} that allows to select from per-defined {@link #getShape() shapes}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ShapeNodeStyle extends NodeStyle {

	/**
	 * Name of {@link #getShape()} property.
	 */
	String SHAPE = "shape";

	/**
	 * Name of {@link #getFill()} property.
	 */
	String FILL = "fill";

	/**
	 * Name of {@link #getStroke()} property.
	 */
	String STROKE = "stroke";

	/**
	 * Selection of supported nodes shapes.
	 */
	@Name(SHAPE)
	ShapeNodeShape getShape();

	/**
	 * Fill style used for the {@link #getShape()}.
	 */
	@Name(FILL)
	Fill getFill();

	/**
	 * Style for the outline of the shape.
	 */
	@Name(STROKE)
	Stroke getStroke();

}
