/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model;

import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * A {@link BlockModel} that takes part in the layout process.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockShape extends Drawable, BlockModel {

	/**
	 * The occupied height of this {@link BlockShape}.
	 */
	double getHeight();

	/**
	 * Computes {@link #getHeight()} and potentially other layout-dependent properties.
	 */
	void updateDimensions(RenderContext context, double offsetX, double offsetY);

}
