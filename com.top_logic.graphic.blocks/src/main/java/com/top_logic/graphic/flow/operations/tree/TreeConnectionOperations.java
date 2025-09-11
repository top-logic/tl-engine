/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.ConnectorSymbol;
import com.top_logic.graphic.flow.data.EdgeDecoration;
import com.top_logic.graphic.flow.data.OffsetPosition;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.operations.util.DiagramUtil;

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

	/**
	 * Layouts edge decorations.
	 */
	default void layout(RenderContext context) {
		for (EdgeDecoration decoration : self().getDecorations()) {
			decoration.layout(context);
		}
	}

	@Override
	default void draw(SvgWriter out) {
		TreeConnector parent = self().getParent();

		double fromX = parent.getX();
		double fromY = parent.getY();

		double barX = self().getBarPosition();
		double scale = actualThickness() / 2;

		TreeConnector child = self().getChild();
		double childX = child.getX() - inset(child.getSymbol()) * scale;
		double childY = child.getY();

		out.beginPath();
		setStroke(out);
		if (!self().getDashes().isEmpty()) {
			out.setStrokeDasharray(DiagramUtil.doubleArray(self().getDashes()));
		}

		out.setFill("none");
		out.beginData();
		{
			out.moveToAbs(childX, childY);
			out.lineToHorizontalAbs(barX);
			out.lineToVerticalAbs(fromY);
			out.lineToHorizontalAbs(fromX + inset(parent.getSymbol()) * scale);
		}
		out.endData();
		out.endPath();

		drawSymbol(out, child.getX(), child.getY(), 1, child.getSymbol(), scale);
		drawSymbol(out, fromX, fromY, -1, parent.getSymbol(), scale);

		for (EdgeDecoration decoration : self().getDecorations()) {
			// Note: The line position is not used/supported for tree layouts.
			OffsetPosition offsetPosition = decoration.getOffsetPosition();
			double x = childX + offsetX(offsetPosition, decoration.getContent().getWidth());
			double y = childY + offsetY(offsetPosition, decoration.getContent().getHeight());

			out.beginGroup();
			out.translate(x, y);
			{
				decoration.draw(out);
			}
			out.endGroup();
		}
	}

	/**
	 * Placement offset in X direction.
	 */
	default double offsetX(OffsetPosition offsetPosition, double width) {
		switch (offsetPosition) {
			case TOP_LEFT:
			case CENTER_LEFT:
			case BOTTOM_LEFT:
				return 0;
			case TOP_RIGHT:
			case CENTER_RIGHT:
			case BOTTOM_RIGHT:
				return -width;
			case CENTER_TOP:
			case CENTER:
			case CENTER_BOTTOM:
				return -width / 2;
		}
		throw new IllegalArgumentException("No such position: " + offsetPosition);
	}

	/**
	 * Placement offset in Y direction.
	 */
	default double offsetY(OffsetPosition offsetPosition, double height) {
		switch (offsetPosition) {
			case TOP_LEFT:
			case TOP_RIGHT:
			case CENTER_TOP:
				return 0;

			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
			case CENTER_BOTTOM:
				return -height;

			case CENTER_LEFT:
			case CENTER_RIGHT:
			case CENTER:
				return -height / 2;
		}
		throw new IllegalArgumentException("No such position: " + offsetPosition);
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
	 *        <code>-1</code> for a parent connector and <code>1</code> for a child connector.
	 * @param connectorSymbol
	 *        The symbol type.
	 */
	default void drawSymbol(SvgWriter out, double fromX, double fromY, double direction,
			ConnectorSymbol connectorSymbol, double scale) {

		switch (connectorSymbol) {
			case NONE:
				return;
			case ARROW:
			case CLOSED_ARROW:
			case FILLED_ARROW: {
				// Arrow to parent
				double arrowOffset = ARROW_LENGTH * scale * direction;
				double arrowOpening = ARROW_WIDTH * scale / 2;

				out.beginPath();
				setStroke(out);
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
				setStroke(out);
				if (connectorSymbol != ConnectorSymbol.FILLED_DIAMOND) {
					out.setFill("none");
				}
				out.beginData();

				double radius = DIAMOND_RADIUS * scale;

				out.moveToAbs(fromX - radius, fromY - radius);
				out.lineToRel(radius, radius);
				out.lineToRel(-radius, radius);
				out.lineToRel(-radius, -radius);
				out.closePath();

				out.endData();
				out.endPath();
				break;
			}
			default:
				break;
		}
	}

	/**
	 * Sets the connection stroke style.
	 */
	default void setStroke(SvgWriter out) {
		double actualThickness = actualThickness();
		out.setStrokeWidth(actualThickness);
		String strokeStyle = self().getStrokeStyle();
		if (strokeStyle != null) {
			out.setStroke(strokeStyle);
		} else {
			out.setStroke(self().getOwner().getStrokeStyle());
		}
	}

	/**
	 * The stroke width to use.
	 */
	default double actualThickness() {
		Double thickness = self().getThickness();
		return (thickness != null) ? thickness.doubleValue() : self().getOwner().getThickness();
	}
}
