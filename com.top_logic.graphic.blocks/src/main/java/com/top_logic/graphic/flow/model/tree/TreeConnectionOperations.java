/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.tree;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;

/**
 * 
 */
public interface TreeConnectionOperations extends Drawable {

	TreeConnection self();

	@Override
	default void draw(SvgWriter out) {
		TreeLayout layout = self().getOwner();
		TreeConnector parent = self().getParent();
		Box parentAnchor = parent.getAnchor();

		double fromX = layout.fromX(parentAnchor);
		double fromY = parentAnchor.getY() + parent.getConnectPosition() * parentAnchor.getHeight();

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
			Box childAnchor = child.getAnchor();

			double childX = childAnchor.getX();
			double childY = childAnchor.getY() + child.getConnectPosition() * childAnchor.getHeight();

			out.moveToAbs(childX, childY);
			out.lineToAbs(barX, childY);
			out.lineToAbs(barX, fromY);

			minY = Math.min(minY, childY);
			maxY = Math.max(maxY, childY);
		}

		out.endData();
		out.endPath();
	}
}
