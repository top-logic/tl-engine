/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.operations.BoxOperations;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Layout and rendering operations for {@link GanttLayout}.
 *
 * <p>
 * Places {@link com.top_logic.react.flow.data.GanttItem} boxes along the X axis according to
 * their {@code start}/{@code end} (or {@code at}) positions, and along the Y axis according
 * to the item's row. Renders axis ticks from {@code axis.currentTicks}, draws row-lane
 * backgrounds, edges (orthogonal), and decorations.
 * </p>
 */
public interface GanttLayoutOperations extends BoxOperations {

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		GanttLayout self = (GanttLayout) this;
		self.setX(offsetX);
		self.setY(offsetY);
		self.setWidth(self.getRowLabelWidth()
			+ (self.getAxis().getRangeMax() - self.getAxis().getRangeMin()) * self.getAxis().getCurrentZoom());
		self.setHeight(self.getAxisHeight() + countRows(self) * self.getRowHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		computeIntrinsicSize(context, offsetX, offsetY);
	}

	@Override
	default void draw(SvgWriter out) {
		// Implemented in Tasks 11-14.
	}

	/** Total number of rows counting the full tree. */
	static int countRows(GanttLayout layout) {
		int total = 0;
		for (GanttRow root : layout.getRootRows()) {
			total += 1 + countDescendants(root);
		}
		return total;
	}

	private static int countDescendants(GanttRow row) {
		int total = 0;
		for (GanttRow child : row.getChildren()) {
			total += 1 + countDescendants(child);
		}
		return total;
	}
}
