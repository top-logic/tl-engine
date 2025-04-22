/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.layout;

import java.util.List;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Empty;
import com.top_logic.graphic.flow.operations.BoxOperations;
import com.top_logic.graphic.flow.operations.util.DiagramUtil;

/**
 * 
 */
public interface GridLayoutOperations extends BoxOperations {

	@Override
	com.top_logic.graphic.flow.data.GridLayout self();

	default Box get(int col, int row) {
		int index = index(col, row);
		List<Box> contents = self().getContents();
		while (contents.size() < index + 1) {
			contents.add(Empty.create());
		}
		return contents.get(index);
	}

	default com.top_logic.graphic.flow.data.GridLayout set(int col, int row, Box content) {
		int index = index(col, row);
		List<Box> contents = self().getContents();
		while (contents.size() < index + 1) {
			contents.add(Empty.create());
		}
		contents.set(index, content);
		return self();
	}

	// private
	default int index(int col, int row) {
		if (col < 0 || col >= self().getCols() || row < 0 || row >= self().getRows()) {
			throw new IllegalArgumentException("No such element: " + col + ", " + row);
		}
		return col + row * self().getCols();
	}

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		self().setRowHeight(DiagramUtil.doubleList(self().getRows()));
		self().setColWidth(DiagramUtil.doubleList(self().getCols()));

		for (int r = 0; r < self().getRows(); r++) {
			for (int c = 0; c < self().getCols(); c++) {
				Box element = get(c, r);
				element.computeIntrinsicSize(context, offsetX, offsetY);

				self().getColWidth().set(c, Math.max(self().getColWidth().get(c), element.getWidth()));
				self().getRowHeight().set(r, Math.max(self().getRowHeight().get(r), element.getHeight()));
			}
		}

		double height = 0;
		for (double h : self().getRowHeight()) {
			height += h;
		}
		if (self().getRows() > 0) {
			height += self().getGapY() * (self().getRows() - 1);
		}

		double width = 0;
		for (double w : self().getColWidth()) {
			width += w;
		}
		if (self().getCols() > 0) {
			width += self().getGapX() * (self().getCols() - 1);
		}

		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		double additionalWidth = (width - self().getWidth()) / self().getCols();
		double additionalHeight = (height - self().getHeight()) / self().getRows();
		
		for (int c = 0; c < self().getCols(); c++) {
			self().getColWidth().set(c, self().getColWidth().get(c) + additionalWidth);
		}
		
		for (int r = 0; r < self().getRows(); r++) {
			self().getRowHeight().set(r, self().getRowHeight().get(r) + additionalHeight);
		}
		
		double y = offsetY;
		for (int r = 0; r < self().getRows(); r++) {
			double x = offsetX;
			for (int c = 0; c < self().getCols(); c++) {
				BoxOperations element = get(c, r);
				element.distributeSize(context, x, y, self().getColWidth().get(c), self().getRowHeight().get(r));
				x += self().getColWidth().get(c) + self().getGapX();
			}
			y += self().getRowHeight().get(r) + self().getGapY();
		}
		
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		for (int r = 0; r < self().getRows(); r++) {
			for (int c = 0; c < self().getCols(); c++) {
				Box element = get(c, r);
				out.write(element);
			}
		}
	}

}
