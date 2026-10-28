/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.SelectableBox;
import com.top_logic.react.flow.data.Widget;

/**
 * Shared helpers for selection management across selectable widget types.
 *
 * <p>
 * Both {@link SelectableBox} and {@link GraphEdge} support selection (have {@code selected} +
 * {@code clickHandler}), an edge only if it is {@link GraphEdge#isSelectable() selectable}. This
 * utility provides type-safe access to the {@code selected} flag on either type.
 * </p>
 */
public class SelectionUtil {

	/**
	 * Sets the {@code selected} flag on the given widget, if it supports selection.
	 */
	public static void setSelected(Widget widget, boolean value) {
		if (widget instanceof SelectableBox) {
			((SelectableBox) widget).setSelected(value);
		} else if (widget instanceof GraphEdge) {
			((GraphEdge) widget).setSelected(value);
		}
	}

	/**
	 * Whether the user can select the given widget.
	 *
	 * <p>
	 * A {@link SelectableBox} can always be selected, a {@link GraphEdge} only if it is marked
	 * {@link GraphEdge#isSelectable() selectable}, no other widget at all. A widget that cannot be
	 * selected takes no part in the selection of the diagram: it is never marked as selected, neither
	 * by the user nor for a selected user object it carries.
	 * </p>
	 */
	public static boolean isSelectable(Widget widget) {
		if (widget instanceof GraphEdge) {
			return ((GraphEdge) widget).isSelectable();
		}
		return widget instanceof SelectableBox;
	}

	/**
	 * Returns whether the given widget is currently selected.
	 */
	public static boolean isSelected(Widget widget) {
		if (widget instanceof SelectableBox) {
			return ((SelectableBox) widget).isSelected();
		} else if (widget instanceof GraphEdge) {
			return ((GraphEdge) widget).isSelected();
		}
		return false;
	}

}
