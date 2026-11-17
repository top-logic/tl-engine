/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgConstants;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.data.ClipBox;

/**
 * A polygonal chain.
 */
public interface ClipBoxOperations extends DecorationOperations {

	/**
	 * Purpose of the {@link SvgConstants#CLIP_PATH} element of a {@link ClipBox}, see
	 * {@link SvgWriter#uniqueId(com.top_logic.react.flow.data.Widget, String)}.
	 */
	String CLIP_PATH_PURPOSE = "clip";

	@Override
	ClipBox self();

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		self().getContent().distributeSize(context, offsetX, offsetY, width, height);

		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void drawContent(SvgWriter out) {
		out.beginGroup();
		out.attachModel(self());

		String clipPathId = out.uniqueId(self(), CLIP_PATH_PURPOSE);

		out.beginGroup();
		out.writeClipPathRef(clipPathId);
		out.writeCssClass(self().getCssClass());

		DecorationOperations.super.drawContent(out);

		out.endGroup();

		out.beginClipPath();
		out.writeId(clipPathId);

		out.rect(self().getX(), self().getY(), self().getWidth(), self().getHeight());

		out.endClipPath();

		out.endGroup();
	}

}
