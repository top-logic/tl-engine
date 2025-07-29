/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.event.Registration;
import com.top_logic.graphic.blocks.svg.event.SVGDropEvent;
import com.top_logic.graphic.blocks.svg.event.SVGDropHandler;
import com.top_logic.graphic.flow.data.DropRegion;

/**
 * Operations for {@link DropRegion} diagram elements.
 */
public interface DropRegionOperations extends DecorationOperations, SVGDropHandler {

	@Override
	DropRegion self();

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		// No visible content.
		self().getContent().distributeSize(context, offsetX, offsetY, width, height);
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		Registration clickHandler = self().getHandlerRegistration();
		if (clickHandler != null) {
			clickHandler.cancel();
		}

		out.beginGroup(self());
		out.writeCssClass(self().getCssClass());
		self().setHandlerRegistration(out.attachOnDrop(this, self()));
		{
			DecorationOperations.super.draw(out);
		}
		out.endGroup();
	}

	@Override
	default void onDrop(SVGDropEvent event) {
		self().getDiagram().getContext().processDrop(self(), event);

		event.stopPropagation();
		event.preventDefault();
	}

}
