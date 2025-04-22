/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.operations.util.DiagramUtil;

/**
 * A border decoration drawn around an element.
 */
public interface BorderOperations extends DecorationOperations {

	@Override
	com.top_logic.graphic.flow.data.Border self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		DecorationOperations.super.computeIntrinsicSize(context, offsetX, offsetY);

		double width = self().getContent().self().getWidth();
		double height = self().getContent().self().getHeight();
		if (self().isTop()) {
			height += self().getThickness();
		}
		if (self().isLeft()) {
			width += self().getThickness();
		}
		if (self().isBottom()) {
			height += self().getThickness();
		}
		if (self().isRight()) {
			width += self().getThickness();
		}

		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		double contentX = offsetX;
		double contentY = offsetY;
		double contentWidth = width;
		double contentHeight = height;

		if (self().isTop()) {
			contentY += self().getThickness();
			contentHeight -= self().getThickness();
		}
		if (self().isLeft()) {
			contentX += self().getThickness();
			contentWidth -= self().getThickness();
		}
		if (self().isBottom()) {
			contentHeight -= self().getThickness();
		}
		if (self().isRight()) {
			contentWidth -= self().getThickness();
		}

		self().getContent().distributeSize(context, contentX, contentY, contentWidth, contentHeight);

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		drawContent(out);

		double radius = self().getThickness() / 2;

		out.beginPath(self());
		out.setStrokeWidth(self().getThickness());
		out.setStroke(self().getStrokeStyle());
		if (!self().getDashes().isEmpty()) {
			out.setStrokeDasharray(DiagramUtil.doubleArray(self().getDashes()));
		}
		out.setFill("none");
		out.beginData();
		out.moveToAbs(self().getX() + (self().isLeft() ? radius : 0), self().getY() + radius);
		if (self().isTop()) {
			out.lineToAbs(self().getRightX() - (self().isRight() ? radius : 0), self().getY() + radius);
		} else {
			out.moveToAbs(self().getRightX() - radius, self().getY());
		}
		if (self().isRight()) {
			out.lineToAbs(self().getRightX() - radius,
				self().getBottomY() - (self().isBottom() ? radius : 0));
		} else {
			out.moveToAbs(self().getRightX(), self().getBottomY() - radius);
		}
		if (self().isBottom()) {
			out.lineToAbs(self().getX() + (self().isLeft() ? radius : 0), self().getBottomY() - radius);
		} else {
			out.moveToAbs(self().getX() + radius, self().getBottomY());
		}
		if (self().isLeft()) {
			if (self().isTop() && self().isRight() && self().isBottom()) {
				out.closePath();
			} else {
				out.lineToAbs(self().getX() + radius, self().getY());
			}
		}
		out.endData();
		out.endPath();
	}

}
