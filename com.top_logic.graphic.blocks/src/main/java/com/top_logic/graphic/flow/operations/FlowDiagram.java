/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Empty;

/**
 * Top-level element of a flow chart.
 */
public class FlowDiagram implements Drawable {

	private Box _root = Empty.create();

	/**
	 * The root diagram element.
	 */
	public BoxOperations getRoot() {
		return _root;
	}

	public FlowDiagram setRoot(Box root) {
		_root = root;
		return this;
	}

	public void layout(RenderContext context) {
		_root.computeIntrinsicSize(context, 0, 0);
		_root.distributeSize(context, 0, 0, _root.getWidth(), _root.getHeight());
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginSvg();
		out.dimensions(Double.toString(_root.getWidth()), Double.toString(_root.getHeight()), 0, 0, _root.getWidth(),
			_root.getHeight());
		_root.draw(out);
		out.endSvg();
	}

}
