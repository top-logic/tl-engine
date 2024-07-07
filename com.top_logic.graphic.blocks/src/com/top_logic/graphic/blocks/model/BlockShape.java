/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model;

import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * A {@link BlockModel} that takes part of the layout process.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockShape extends Drawable, BlockModel {

	/**
	 * The occupied height of this {@link BlockShape}.
	 */
	double getHeight();

	/**
	 * Computes {@link #getHeight()} potentially other layout-dependent properties.
	 */
	void updateDimensions(RenderContext context, double offsetX, double offsetY);

}
