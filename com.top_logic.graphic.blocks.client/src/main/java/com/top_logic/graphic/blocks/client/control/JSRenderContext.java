/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.client.control;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * {@link RenderContext} based on client-side canvas logic.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSRenderContext implements RenderContext {

	private Context2d _context2d;

	/**
	 * Creates a {@link JSRenderContext}.
	 */
	public JSRenderContext() {
		CanvasElement canvas = Document.get().createCanvasElement();
		_context2d = canvas.getContext2d();
		_context2d.setFont("14px Arial");
	}

	@Override
	public TextMetrics measure(String text) {
		com.google.gwt.canvas.dom.client.TextMetrics metrics = _context2d.measureText(text);
		return new JSTextMetrics(metrics);
	}

}
