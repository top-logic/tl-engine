/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgConstants;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.ClipBox;

/**
 * A polygonal chain.
 */
public interface ClipBoxOperations extends DecorationOperations {

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
		out.beginGroup(self());
		out.writeAttribute("clip-path", "url(#" + getClipPathID() + ")");
		out.writeCssClass(self().getCssClass());

		DecorationOperations.super.drawContent(out);
		
		out.endGroup();
		
		out.beginClipPath();
		out.writeAttribute("id", getClipPathID());

		out.rect(self().getX(), self().getY(), self().getWidth(), self().getHeight());

		out.endClipPath();
	}

	/**
	 * Client ID of the {@link SvgConstants#CLIP_PATH} element.
	 */
	default String getClipPathID() {
		return self().getClientId() + "-clipPath";
	}

}
