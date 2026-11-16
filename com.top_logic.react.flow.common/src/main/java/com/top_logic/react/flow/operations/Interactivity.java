/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.data.ClickTarget;
import com.top_logic.react.flow.data.ContextMenu;
import com.top_logic.react.flow.data.Tooltip;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Decides whether a drawn part of a diagram reacts on the pointer, and writes the corresponding
 * {@link #POINTER_EVENTS_ATTR} attribute.
 *
 * <p>
 * Parts drawn on top of other elements only for information, such as edges and the labels of
 * decorations, must not catch clicks meant for the elements below them. Such a part receives
 * pointer events only when it is interactive: when it can be selected, reacts on a click, offers a
 * context menu, or shows a tooltip.
 * </p>
 */
public class Interactivity {

	/**
	 * SVG presentation attribute deciding which parts of an element are hit by the pointer.
	 */
	public static final String POINTER_EVENTS_ATTR = "pointer-events";

	/**
	 * {@link #POINTER_EVENTS_ATTR} value: the element is never hit, the pointer reaches the
	 * elements below it.
	 */
	public static final String POINTER_EVENTS_NONE = "none";

	/**
	 * {@link #POINTER_EVENTS_ATTR} value: the element is hit on its whole fill and stroke area,
	 * whether painted or not.
	 */
	public static final String POINTER_EVENTS_ALL = "all";

	/**
	 * {@link #POINTER_EVENTS_ATTR} value: the element is hit only on its stroke, whether painted or
	 * not.
	 */
	public static final String POINTER_EVENTS_STROKE = "stroke";

	/**
	 * Whether the given widget itself reacts on the pointer.
	 *
	 * <p>
	 * This is the case for a widget the user can {@link SelectionUtil#isSelectable(Widget)
	 * select}, a {@link ClickTarget}, a {@link ContextMenu}, and a {@link Tooltip}.
	 * </p>
	 *
	 * @see #containsInteractive(Widget)
	 */
	public static boolean isInteractive(Widget widget) {
		return SelectionUtil.isSelectable(widget)
			|| widget instanceof ClickTarget
			|| widget instanceof ContextMenu
			|| widget instanceof Tooltip;
	}

	/**
	 * Whether the given widget or any widget it {@link WidgetTraversal#visitContents(Widget,
	 * java.util.function.Consumer) contains} {@link #isInteractive(Widget) reacts on the pointer}.
	 *
	 * @param widget
	 *        The widget to check, or <code>null</code>.
	 */
	public static boolean containsInteractive(Widget widget) {
		boolean[] result = { false };
		WidgetTraversal.visitContents(widget, part -> result[0] |= isInteractive(part));
		return result[0];
	}

	/**
	 * Lets the pointer pass through the element currently written to the elements below it.
	 *
	 * <p>
	 * Must be called after opening an element and before writing its content. The setting is
	 * inherited by the content.
	 * </p>
	 */
	public static void writePassThrough(SvgWriter out) {
		out.writeAttribute(POINTER_EVENTS_ATTR, POINTER_EVENTS_NONE);
	}

	/**
	 * Writes the {@link #POINTER_EVENTS_ATTR} of the group currently written that draws the given
	 * widget on top of other elements.
	 *
	 * <p>
	 * When the widget {@link #containsInteractive(Widget) contains an interactive part}, the whole
	 * area of the group receives pointer events, also within a pass-through group. Otherwise the
	 * pointer passes through it.
	 * </p>
	 *
	 * <p>
	 * Must be called after opening an element and before writing its content.
	 * </p>
	 */
	public static void writeOverlayPointerEvents(SvgWriter out, Widget widget) {
		out.writeAttribute(POINTER_EVENTS_ATTR,
			containsInteractive(widget) ? POINTER_EVENTS_ALL : POINTER_EVENTS_NONE);
	}

}
