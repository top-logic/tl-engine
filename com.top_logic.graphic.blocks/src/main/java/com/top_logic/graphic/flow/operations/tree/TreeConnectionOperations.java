/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.List;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.ConnectorSymbol;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;

/**
 * Custom operations for {@link TreeConnection}s.
 */
public interface TreeConnectionOperations extends Drawable {

	/**
	 * Width of a horizontally drawn arrow symbol.
	 */
	public static final double ARROW_LENGTH = 12;

	/**
	 * Width of an arrow opening.
	 */
	public static final double ARROW_WIDTH = 8;

	/**
	 * Radius of a diamond symbol.
	 */
	public static final double DIAMOND_RADIUS = 6;

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

		List<TreeConnector> children = self().getChildren();
		if (!children.isEmpty()) {
			out.beginPath();
			out.setStroke(self().getOwner().getStrokeStyle());
			out.setStrokeWidth(self().getOwner().getThickness());
			out.setFill("none");
			out.beginData();

			for (TreeConnector child : children) {
				double childX = child.getX() - inset(child.getSymbol());
				double childY = child.getY();

				out.moveToAbs(childX, childY);
				out.lineToHorizontalAbs(barX);
				out.lineToVerticalAbs(fromY);
				out.lineToHorizontalAbs(fromX + inset(parent.getSymbol()));
			}

			out.endData();
			out.endPath();
			
			for (TreeConnector child : children) {
				double childX = child.getX();
				double childY = child.getY();

				drawSymbol(out, childX, childY, 1, child.getSymbol());
			}

			drawSymbol(out, fromX, fromY, -1, parent.getSymbol());
		}
	}

	/** 
	 * The space to reserve for the symbol in X direction.
	 */
	default double inset(ConnectorSymbol symbol) {
		switch (symbol) {
			case NONE:
			case ARROW:
				return 0;
			case CLOSED_ARROW:
			case FILLED_ARROW:
				return ARROW_LENGTH;
			case DIAMOND:
			case FILLED_DIAMOND:
				return 2 * DIAMOND_RADIUS;
		}

		throw new IllegalArgumentException("No such symbol type: " + symbol);
	}

	/**
	 * Draws a connector symbol at the given location.
	 *
	 * @param out
	 *        The writer write to.
	 * @param fromX
	 *        The X position of the connector.
	 * @param fromY
	 *        The Y position of the connector.
	 * @param direction
	 *        <code>-1<code> for a parent connector and <code>1</code> for a child connector.
	 * @param connectorSymbol
	 *        The symbol type.
	 */
	default void drawSymbol(SvgWriter out, double fromX, double fromY, double direction,
			ConnectorSymbol connectorSymbol) {
		switch (connectorSymbol) {
			case NONE:
				return;
			case ARROW:
			case CLOSED_ARROW:
			case FILLED_ARROW: {
				// Arrow to parent
				double arrowOffset = ARROW_LENGTH * direction;
				double arrowOpening = ARROW_WIDTH / 2;

				out.beginPath();
				out.setStroke(self().getOwner().getStrokeStyle());
				out.setStrokeWidth(self().getOwner().getThickness());
				if (connectorSymbol != ConnectorSymbol.FILLED_ARROW) {
					out.setFill("none");
				}
				out.beginData();

				out.moveToAbs(fromX - arrowOffset, fromY + arrowOpening);
				out.lineToRel(arrowOffset, -arrowOpening);
				out.lineToRel(-arrowOffset, -arrowOpening);
				if (connectorSymbol != ConnectorSymbol.ARROW) {
					out.closePath();
				}

				out.endData();
				out.endPath();
				break;
			}
			case DIAMOND:
			case FILLED_DIAMOND: {
				out.beginPath();
				out.setStroke(self().getOwner().getStrokeStyle());
				out.setStrokeWidth(self().getOwner().getThickness());
				if (connectorSymbol != ConnectorSymbol.FILLED_DIAMOND) {
					out.setFill("none");
				}
				out.beginData();

				out.moveToAbs(fromX - DIAMOND_RADIUS, fromY - DIAMOND_RADIUS);
				out.lineToRel(DIAMOND_RADIUS, DIAMOND_RADIUS);
				out.lineToRel(-DIAMOND_RADIUS, DIAMOND_RADIUS);
				out.lineToRel(-DIAMOND_RADIUS, -DIAMOND_RADIUS);
				out.closePath();

				out.endData();
				out.endPath();
				break;
			}
			default:
				break;
		}
	}
}
