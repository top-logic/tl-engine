/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link DrawElement} that wraps a single other {@link DrawElement}.
 */
public abstract class Decoration extends AbstractDrawElement {

	private DrawElement _content = new EmptyBlock();

	/**
	 * The decorated element.
	 */
	public DrawElement getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public Decoration setContent(DrawElement content) {
		_content = content == null ? new EmptyBlock() : content;
		return this;
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		_content.computeIntrinsicSize(context, offsetX, offsetY);
		setWidth(_content.getWidth());
		setHeight(_content.getHeight());
	}

	@Override
	public void draw(SvgWriter out) {
		_content.draw(out);
	}

}
