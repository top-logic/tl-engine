/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.basic.shared.html.TagUtilShared;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Tooltip;

/**
 * Custom operations for {@link Tooltip} diagram nodes.
 */
public interface TooltipOperations extends DecorationOperations {

	@Override
	Tooltip self();

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
		out.beginGroup(self());
		out.writeCssClass(self().getCssClass());

		// Tooltip template for the poor. This is rendered on the client where TL tools are not
		// available.
		StringBuilder buffer = new StringBuilder();
		buffer.append("<div class=\"tooltipContent\">");
		TagUtilShared.writeAttributeText(buffer, self().getText());
		buffer.append("</div>");

		out.writeAttribute(/* HTMLConstants.TL_TOOLTIP_ATTR */ "data-tooltip", buffer.toString());
		{
			DecorationOperations.super.draw(out);
		}
		out.endGroup();
	}

}
