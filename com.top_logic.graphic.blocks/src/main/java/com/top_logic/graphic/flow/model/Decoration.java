/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;

/**
 * {@link DrawElement} that wraps a single other {@link DrawElement}.
 */
public interface Decoration extends DrawElement {

	@Override
	com.top_logic.graphic.flow.data.Decoration self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		Box content = self().getContent();
		content.computeIntrinsicSize(context, offsetX, offsetY);
		self().setWidth(content.getWidth());
		self().setHeight(content.getHeight());
	}

	@Override
	default void draw(SvgWriter out) {
		self().getContent().draw(out);
	}

}
