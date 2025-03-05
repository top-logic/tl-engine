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
 * A column of elements.
 */
public class VerticalLayout extends AbstractDrawElement {

	private double _gap;

	private SpaceDistribution _fill = SpaceDistribution.NONE;

	private List<DrawElement> _rows = new ArrayList<>();

	/**
	 * Adds a new row to this layout.
	 */
	public VerticalLayout addRow(DrawElement row) {
		_rows.add(row);
		return this;
	}

	/**
	 * Space between elements.
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
	public VerticalLayout setGap(double gap) {
		_gap = gap;
		return this;
	}

	@Override
	public void draw(SvgWriter out) {
		for (DrawElement e : _rows) {
			e.draw(out);
		}
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {

	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		double height = 0;
		double maxWidth = 0;

		setX(offsetX);
		setY(offsetY);

		double x = offsetX;
		double y = offsetY;

		for (int n = 0; n < _rows.size(); n++) {
			DrawElement row = _rows.get(n);

			if (n > 0) {
				height += _gap;
			}
			y = offsetY + height;

			row.computeIntrinsicSize(context, x, y);

			height += row.getHeight();
			maxWidth = Math.max(maxWidth, row.getWidth());
		}

		setWidth(maxWidth);
		setHeight(height);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);

		int cnt = _rows.size();
		double additionalSpace = height - getHeight();
		double additionalHeight = _fill == SpaceDistribution.STRETCH_CONTENT && cnt > 0 ? additionalSpace / cnt : 0;
		double gap = _gap + (_fill == SpaceDistribution.STRETCH_GAP && cnt > 1 ? additionalSpace / (cnt - 1) : 0);

		double elementY = offsetY;

		for (int n = 0; n < cnt; n++) {
			DrawElement row = _rows.get(n);

			double elementHeight = row.getHeight() + additionalHeight;

			row.distributeSize(context, offsetX, elementY, width, elementHeight);

			elementY += elementHeight;
			elementY += gap;
		}

		setWidth(width);
		setHeight(height);
	}

}
