/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;

/**
 * Custom operations for {@link TreeConnection}s.
 */
public interface TreeConnectionOperations extends Drawable {

	/**
	 * The {@link TreeConnection} data.
	 */
	TreeConnection self();

	@Override
	default void draw(SvgWriter out) {
		TreeConnector parent = self().getParent();

		double fromX = parent.getX();
		double fromY = parent.getY();

		double barX = self().getBarPosition();

		out.beginPath();
		out.setStroke(self().getOwner().getStrokeStyle());
		out.setStrokeWidth(self().getOwner().getThickness());
		out.setFill("none");
		out.beginData();
		out.moveToAbs(fromX, fromY);
		out.lineToHorizontalAbs(barX);

		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		for (TreeConnector child : self().getChildren()) {
			double childX = child.getX();
			double childY = child.getY();

			out.moveToAbs(childX, childY);
			out.lineToAbs(barX, childY);
			out.lineToAbs(barX, fromY);

			minY = Math.min(minY, childY);
			maxY = Math.max(maxY, childY);
		}

		out.endData();
		out.endPath();

		// Optionally show connectors
//		parent.draw(out);
//		for (TreeConnector child : self().getChildren()) {
//			child.draw(out);
//		}

	}
}
