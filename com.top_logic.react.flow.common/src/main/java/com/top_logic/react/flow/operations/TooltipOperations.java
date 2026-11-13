/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.data.Tooltip;

/**
 * Custom operations for {@link Tooltip} diagram nodes.
 */
public interface TooltipOperations extends DecorationOperations {

	/**
	 * Attribute declaring the tooltip of a drawn element, interpreted by the tooltip host of the
	 * React UI.
	 */
	String TOOLTIP_ATTR = "data-tooltip";

	/**
	 * Prefix of a {@link #TOOLTIP_ATTR} value whose rest is the plain text to show.
	 */
	String TEXT_TOOLTIP_PREFIX = "text:";

	@Override
	Tooltip self();

	/**
	 * Declares the given plain text as tooltip of the element currently written.
	 *
	 * <p>
	 * Must be called after opening an element and before writing its content.
	 * </p>
	 */
	static void writeTextTooltip(SvgWriter out, String text) {
		out.writeAttribute(TOOLTIP_ATTR, TEXT_TOOLTIP_PREFIX + text);
	}

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
		out.beginGroup();
		out.attachModel(self());
		out.writeCssClass(self().getCssClass());

		writeTextTooltip(out, self().getText());
		{
			DecorationOperations.super.draw(out);
		}
		out.endGroup();
	}

}
