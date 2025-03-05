/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * An empty space.
 */
public class EmptyBlock extends AbstractDrawElement {

	@Override
	public void draw(SvgWriter out) {
		// No contents.
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// No props.
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		setWidth(0);
		setHeight(0);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);
	}

}
