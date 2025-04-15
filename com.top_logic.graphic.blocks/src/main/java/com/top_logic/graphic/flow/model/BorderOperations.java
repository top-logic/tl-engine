/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.util.List;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

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
		DecorationOperations.super.draw(out);

		double radius = self().getThickness() / 2;

		out.beginPath();
		out.setStrokeWidth(self().getThickness());
		out.setStroke(self().getStrokeStyle());
		if (!self().getDashes().isEmpty()) {
			out.setStrokeDasharray(doubleArray(self().getDashes()));
		}
		out.setFill("none");
		out.beginData();
		out.moveToAbs(self().getX() + (self().isLeft() ? radius : 0), self().getY() + radius);
		if (self().isTop()) {
			out.lineToAbs(self().getX() + self().getWidth() - (self().isRight() ? radius : 0), self().getY() + radius);
		} else {
			out.moveToAbs(self().getX() + self().getWidth() - radius, self().getY());
		}
		if (self().isRight()) {
			out.lineToAbs(self().getX() + self().getWidth() - radius,
				self().getY() + self().getHeight() - (self().isBottom() ? radius : 0));
		} else {
			out.moveToAbs(self().getX() + self().getWidth(), self().getY() + self().getHeight() - radius);
		}
		if (self().isBottom()) {
			out.lineToAbs(self().getX() + (self().isLeft() ? radius : 0), self().getY() + self().getHeight() - radius);
		} else {
			out.moveToAbs(self().getX() + radius, self().getY() + self().getHeight());
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

	private static double[] doubleArray(List<Double> dashes) {
		double[] result = new double[dashes.size()];
		for (int n = 0, cnt = dashes.size(); n < cnt; n++) {
			result[n] = dashes.get(n);
		}
		return result;
	}

}
