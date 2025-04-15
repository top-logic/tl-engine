/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * 
 */
public interface BoxOperations extends Drawable {

	com.top_logic.graphic.flow.data.Box self();

	void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY);

	void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height);

}
