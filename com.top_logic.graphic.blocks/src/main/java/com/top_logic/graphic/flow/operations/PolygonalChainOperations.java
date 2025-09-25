/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import java.util.stream.Collectors;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Point;
import com.top_logic.graphic.flow.data.PolygonalChain;
import com.top_logic.graphic.flow.operations.util.DiagramUtil;

/**
 * A polygonal chain.
 */
public interface PolygonalChainOperations extends BoxOperations {

	@Override
	PolygonalChain self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		double maxX = 0, maxY = 0;
		for (Point p : self().getPoints()) {
			maxX = Math.max(maxX, p.getX());
			maxY = Math.max(maxY, p.getY());
		}

		self().setWidth(maxX + self().getThickness());
		self().setHeight(maxY + self().getThickness());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		double x = self().getX();
		double y = self().getY();
		String points = self().getPoints()
			.stream()
			.map(point -> new StringBuilder()
				.append(point.getX() + x)
				.append(',')
				.append(point.getY() + y))
			.collect(Collectors.joining(" "));

		boolean isPolygon = self().isClosed();
		if (isPolygon) {
			out.beginPolygon();
		} else {
			out.beginPolyline();
		}
		out.writePoints(points);
		out.writeCssClass(self().getCssClass());
		if (!self().getDashes().isEmpty()) {
			out.setStrokeDasharray(DiagramUtil.doubleArray(self().getDashes()));
		}
		out.setStrokeWidth(self().getThickness());
		out.setStroke(self().getStrokeStyle());
		out.setFill(self().getFillStyle());
		if (isPolygon) {
			out.endPolygon();
		} else {
			out.endPolyline();
		}
	}

}
