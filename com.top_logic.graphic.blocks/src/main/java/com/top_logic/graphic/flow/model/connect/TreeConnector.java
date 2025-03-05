/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.connect;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.graphic.blocks.math.Vec;
import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.param.Orientation;

/**
 * 
 */
public class TreeConnector implements Drawable {

	private Socket _parent;

	private Socket _bar;

	private List<Socket> _children = new ArrayList<>();

	private Orientation _orientation = Orientation.HORIZONTAL;

	@Override
	public void draw(SvgWriter out) {
		out.beginGroup();

		out.beginPath();
		out.beginData();
		out.moveToAbs(Vec.vec(_bar.getX(), _parent.getY()));
		out.lineToAbs(_parent.getPos());
		out.endData();
		out.endPath();

		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;

		for (Socket child : _children) {
			double y = child.getY();

			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y);

			out.beginPath();
			out.beginData();
			out.moveToAbs(Vec.vec(_bar.getX(), child.getY()));
			out.lineToAbs(child.getPos());
			out.endData();
			out.endPath();
		}

		out.beginPath();
		out.beginData();
		out.moveToAbs(Vec.vec(_bar.getX(), minY));
		out.lineToAbs(Vec.vec(_bar.getX(), maxY));
		out.endData();
		out.endPath();

		out.endGroup();
	}

}
