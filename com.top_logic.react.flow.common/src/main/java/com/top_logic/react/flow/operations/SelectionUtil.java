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
 * {@code clickHandler}). This utility provides type-safe access to the {@code selected} flag on
 * either type.
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
