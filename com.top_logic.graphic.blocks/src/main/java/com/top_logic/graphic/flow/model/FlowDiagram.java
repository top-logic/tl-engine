/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Top-level element of a flow chart.
 */
public class FlowDiagram implements Drawable {

	private DrawElement _root = new EmptyBlock();

	/**
	 * The root diagram element.
	 */
	public DrawElement getRoot() {
		return _root;
	}

	public FlowDiagram setRoot(DrawElement root) {
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
		_root.draw(out);
		out.endSvg();
	}

}
