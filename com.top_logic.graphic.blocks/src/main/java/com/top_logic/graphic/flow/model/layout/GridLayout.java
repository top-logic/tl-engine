/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.layout;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.model.AbstractDrawElement;
import com.top_logic.graphic.flow.model.DrawElement;
import com.top_logic.graphic.flow.model.EmptyBlock;

/**
 * 
 */
public class GridLayout extends AbstractDrawElement {

	private int _cols;

	private int _rows;

	private DrawElement[] _contents;

	private double _rowHeight[];

	private double _colWidth[];

	private double _gapX;

	private double _gapY;

	/**
	 * Creates a {@link GridLayout}.
	 */
	public GridLayout(int cols, int rows) {
		_rows = rows;
		_cols = cols;
		_contents = new DrawElement[rows * cols];
		for (int n = 0; n < _contents.length; n++) {
			_contents[n] = new EmptyBlock();
		}
	}

	/**
	 * Gap between columns.
	 * 
	 * <p>
	 * If negative columns overlap.
	 * </p>
	 */
	public double getGapX() {
		return _gapX;
	}

	/**
	 * @see #getGapX()
	 */
	public GridLayout setGapX(double gapX) {
		_gapX = gapX;
		return this;
	}

	/**
	 * Gap between rows.
	 * 
	 * <p>
	 * If negative, rows overlap.
	 * </p>
	 */
	public double getGapY() {
		return _gapY;
	}

	/**
	 * @see #getGapY()
	 */
	public GridLayout setGapY(double gapY) {
		_gapY = gapY;
		return this;
	}

	public DrawElement get(int col, int row) {
		return _contents[index(col, row)];
	}

	public GridLayout set(int col, int row, DrawElement content) {
		_contents[index(col, row)] = content;
		return this;
	}

	private int index(int col, int row) {
		if (col < 0 || col >= _cols || row < 0 || row >= _rows) {
			throw new IllegalArgumentException("No such element: " + col + ", " + row);
		}
		return col + row * _cols;
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		setX(offsetX);
		setY(offsetY);

		_rowHeight = new double[_rows];
		_colWidth = new double[_cols];

		for (int r = 0; r < _rows; r++) {
			for (int c = 0; c < _cols; c++) {
				DrawElement element = get(c, r);
				element.computeIntrinsicSize(context, offsetX, offsetY);

				_colWidth[c] = Math.max(_colWidth[c], element.getWidth());
				_rowHeight[r] = Math.max(_rowHeight[r], element.getHeight());
			}
		}

		double height = 0;
		for (double h : _rowHeight) {
			height += h;
		}
		if (_rows > 0) {
			height += _gapY * (_rows - 1);
		}

		double width = 0;
		for (double w : _colWidth) {
			width += w;
		}
		if (_cols > 0) {
			width += _gapX * (_cols - 1);
		}

		setWidth(width);
		setHeight(height);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);

		double additionalWidth = (width - getWidth()) / _cols;
		double additionalHeight = (height - getHeight()) / _rows;
		
		for (int c = 0; c < _cols; c++) {
			_colWidth[c] += additionalWidth;
		}
		
		for (int r = 0; r < _rows; r++) {
			_rowHeight[r] += additionalHeight;
		}
		
		double y = offsetY;
		for (int r = 0; r < _rows; r++) {
			double x = offsetX;
			for (int c = 0; c < _cols; c++) {
				DrawElement element = get(c, r);
				element.distributeSize(context, x, y, _colWidth[c], _rowHeight[r]);
				x += _colWidth[c] + _gapX;
			}
			y += _rowHeight[r] + _gapY;
		}
		
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void draw(SvgWriter out) {
		for (int r = 0; r < _rows; r++) {
			for (int c = 0; c < _cols; c++) {
				DrawElement element = get(c, r);
				element.draw(out);
			}
		}
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created

	}

}
