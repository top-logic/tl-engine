/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.operations.WidgetOperations;

/**
 * Custom operations defined on a {@link TreeConnector}.
 */
public interface TreeConnectorOperations extends WidgetOperations {

	/**
	 * The visual radius, when the connector is shown.
	 */
	double RADIUS = 4.5;

	/**
	 * The visual diameter, when the connector is shown.
	 */
	double DIAMETER = 2 * RADIUS;

	@Override
	TreeConnector self();

	/**
	 * Whether this the connector on the parent-side of the connection.
	 */
	default boolean isParent() {
		return self().getConnection().getParent() == this;
	}

	/**
	 * The X coordinate, where this connector connects to its anchor.
	 */
	default double getX() {
		TreeLayout layout = self().getConnection().getOwner();
		if (isParent()) {
			return layout.fromX(self().getAnchor());
		} else {
			return layout.toX(self().getAnchor());
		}
	}

	/**
	 * The Y coordinate, where this connector connects to its anchor.
	 */
	default double getY() {
		return self().getAnchor().getY() + self().getConnectPosition() * self().getAnchor().getHeight();
	}

	@Override
	default void draw(SvgWriter out) {
		double x = getX();
		double y = getY();

		out.beginRect(x - RADIUS, y - RADIUS, DIAMETER, DIAMETER);
		out.setStroke("black");
		out.setFill("none");
		out.setStrokeWidth(1);
		out.endRect();
	}

}
