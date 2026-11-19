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
	 * Entry point for the diagram layout with unlimited space.
	 * 
	 * <p>
	 * This method computes the size and positions of all diagram elements. Every element takes
	 * its full natural size, as needed for an export of the complete diagram.
	 * </p>
	 *
	 * @see #layout(RenderContext, double, double)
	 */
	default void layout(RenderContext context) {
		layout(context, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Entry point for the diagram layout in a viewport of the given size.
	 * 
	 * <p>
	 * This method computes the size and positions of all diagram elements. The given size is
	 * offered to the root element, containers pass the space remaining for each child down.
	 * Elements that can show their content in an internal viewport (e.g. a Gantt chart) limit
	 * their size to the offered space, all others take their natural size.
	 * </p>
	 *
	 * @param availableWidth
	 *        The width of the viewport at scale 1, or {@link Double#POSITIVE_INFINITY} for
	 *        unlimited width.
	 * @param availableHeight
	 *        The height of the viewport at scale 1, or {@link Double#POSITIVE_INFINITY} for
	 *        unlimited height.
	 */
	default void layout(RenderContext context, double availableWidth, double availableHeight) {
		Box root = self().getRoot();
		root.computeIntrinsicSize(context, 0, 0, availableWidth, availableHeight);
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
	 * <p>
	 * In the browser, the diagram is drawn the same way: there the context's font defaults are the
	 * font of the element the diagram is displayed in, stated on the root, so that text is drawn in
	 * the font it was measured with.
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
			// The font the layout measured with, as presentation attributes on the root: text
			// inherits it, an explicit font on a text or from a stylesheet rule still overrides it,
			// and no selector support is required of the renderer.
			out.setTextStyle(context.getDefaultFontDeclaration(), context.getDefaultFontSizePx(), null);
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
			self().setIncrementalSelection(false);
			for (Widget selected : self().getSelection()) {
				SelectionUtil.setSelected(selected, false);
			}
			self().setSelection(Collections.emptyList());
		}
		event.stopPropagation();
	}

	/**
	 * Adds the given elements to the selection, as a selection rectangle drawn over them does.
	 *
	 * <p>
	 * The change is {@link Diagram#isIncrementalSelection() incremental}: the elements selected so
	 * far stay selected, and so do the selected objects the diagram does not display. Elements that
	 * cannot be selected are skipped. A diagram without {@link Diagram#isMultiSelect() multiple
	 * selection} is left unchanged.
	 * </p>
	 *
	 * @param elements
	 *        The elements to select.
	 */
	default void addToSelection(Iterable<? extends Widget> elements) {
		Diagram diagram = self();
		if (!diagram.isMultiSelect()) {
			return;
		}
		boolean incrementalSet = false;
		for (Widget element : elements) {
			if (!SelectionUtil.isSelectable(element) || SelectionUtil.isSelected(element)) {
				continue;
			}
			if (!incrementalSet) {
				// Must be set before the selection changes, see Diagram#isIncrementalSelection().
				diagram.setIncrementalSelection(true);
				incrementalSet = true;
			}
			diagram.getSelection().add(element);
			SelectionUtil.setSelected(element, true);
		}
	}
}
