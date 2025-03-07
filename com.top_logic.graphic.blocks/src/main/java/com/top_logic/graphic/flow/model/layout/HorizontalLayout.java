/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.layout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.model.AbstractDrawElement;
import com.top_logic.graphic.flow.model.DrawElement;
import com.top_logic.graphic.flow.param.SpaceDistribution;

/**
 * A row of elements.
 */
public class HorizontalLayout extends AbstractDrawElement {

	private double _gap;

	private List<DrawElement> _cols = new ArrayList<>();

	private SpaceDistribution _fill = SpaceDistribution.NONE;

	/**
	 * The elements to display in a row.
	 */
	public List<DrawElement> getCols() {
		return _cols;
	}

	/**
	 * Adds a new column to this row.
	 */
	public HorizontalLayout addCol(DrawElement col) {
		_cols.add(col);
		return this;
	}

	/**
	 * @see #getCols()
	 */
	public HorizontalLayout setCols(List<DrawElement> cols) {
		_cols = cols;
		return this;
	}

	/**
	 * The gap between elements.
	 * 
	 * <p>
	 * If negative, elements overlap.
	 * </p>
	 */
	public double getGap() {
		return _gap;
	}

	/**
	 * @see #getGap()
	 */
	public HorizontalLayout setGap(double gap) {
		_gap = gap;
		return this;
	}

	/**
	 * How additional space is distributed between elements.
	 */
	public SpaceDistribution getFill() {
		return _fill;
	}

	/**
	 * @see #getFill()
	 */
	public HorizontalLayout setFill(SpaceDistribution fill) {
		_fill = fill;
		return this;
	}

	@Override
	public void draw(SvgWriter out) {
		for (DrawElement e : _cols) {
			e.draw(out);
		}
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {

	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		double width = 0;
		double maxHeight = 0;

		setX(offsetX);
		setY(offsetY);

		double x = offsetX;
		double y = offsetY;
		double gap = _gap;

		for (int n = 0; n < _cols.size(); n++) {
			DrawElement col = _cols.get(n);

			if (n > 0) {
				width += gap;
			}
			x = offsetX + width;

			col.computeIntrinsicSize(context, x, y);

			width += col.getWidth();
			maxHeight = Math.max(maxHeight, col.getHeight());
		}

		setWidth(width);
		setHeight(maxHeight);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);

		double additionalSpace = width - getWidth();
		int cnt = _cols.size();
		double additionalWidth =
			_fill == SpaceDistribution.STRETCH_CONTENT && cnt > 0 ? additionalSpace / cnt : 0;
		double gap = _gap
			+ (_fill == SpaceDistribution.STRETCH_GAP && cnt > 1 ? additionalSpace / (cnt - 1) : 0);
		
		double elementX = offsetX;

		for (int n = 0; n < cnt; n++) {
			DrawElement col = _cols.get(n);

			double elementWidth = col.getWidth() + additionalWidth;

			col.distributeSize(context, elementX, offsetY, elementWidth, height);

			elementX += elementWidth;
			elementX += gap;
		}

		setWidth(width);
		setHeight(height);
	}

}