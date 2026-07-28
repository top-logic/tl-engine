/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import java.util.Collections;

import com.top_logic.react.flow.model.Drawable;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.svg.event.MouseButton;
import com.top_logic.react.flow.svg.event.Registration;
import com.top_logic.react.flow.svg.event.SVGClickEvent;
import com.top_logic.react.flow.svg.event.SVGClickHandler;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.Widget;

/**
 * Custom operations for {@link Diagram} elements.
 */
public interface DiagramOperations extends Drawable, SVGClickHandler {

	/**
	 * The {@link Diagram} data.
	 */
	Diagram self();

	/**
	 * Entry point for the diagram layout.
	 * 
	 * <p>
	 * This method computes the size and positions of all diagram elements.
	 * </p>
	 */
	default void layout(RenderContext context) {
		Box root = self().getRoot();
		root.computeIntrinsicSize(context, 0, 0,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		root.distributeSize(context, 0, 0, root.getWidth(), root.getHeight());
	}

	@Override
	default void draw(SvgWriter out) {
		draw(out, null, null);
	}

	/**
	 * Draws this diagram as a standalone SVG document that carries the font defaults of the
	 * {@link RenderContext} it was {@link #layout(RenderContext) laid out} with.
	 *
	 * <p>
	 * A standalone document has no external stylesheet, so text without an explicit font would be
	 * rendered in the viewer's default font instead of the one the layout reserved space for. Use
	 * this method (not {@link #draw(SvgWriter)}) whenever the result is written to a file or
	 * shipped as an export.
	 * </p>
	 *
	 * @param context
	 *        The context this diagram was laid out with, or {@code null} to omit the font defaults
	 *        (the surrounding document supplies them).
	 * @param extraStyles
	 *        Additional CSS rules to embed, or {@code null} for none.
	 */
	default void draw(SvgWriter out, RenderContext context, CharSequence extraStyles) {
		Registration clickHandler = self().getClickHandler();
		if (clickHandler != null) {
			clickHandler.cancel();
		}

		out.beginSvg();
		out.writeCssClass(self().getCssClass());
		Box root = self().getRoot();
		out.dimensions(
			"100%",
			"100%",
			self().getViewBoxX(),
			self().getViewBoxY(),
			self().getViewBoxWidth(),
			self().getViewBoxHeight());
		if (context != null) {
			out.writeDefaultTextStyle(context);
		}
		if (extraStyles != null) {
			out.style(extraStyles);
		}
		self().setClickHandler(out.attachOnClick(this, self()));
		out.write(root);
		out.endSvg();
	}

	@Override
	default void onClick(SVGClickEvent event) {
		if (!event.getButton(MouseButton.LEFT)) {
			return;
		}

		if (event.isShiftKey() || event.isCtrlKey()) {
			// Ignore.
		} else {
			for (Widget selected : self().getSelection()) {
				SelectionUtil.setSelected(selected, false);
			}
			self().setSelection(Collections.emptyList());
		}
		event.stopPropagation();
	}
}
