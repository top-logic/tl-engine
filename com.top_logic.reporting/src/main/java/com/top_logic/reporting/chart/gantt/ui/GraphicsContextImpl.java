/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.ui;

import java.awt.Graphics2D;

/**
 * Default {@link GraphicsContext} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GraphicsContextImpl implements GraphicsContext, GraphicsContext.ContentContext, GraphicsContext.FooterContext, GraphicsContext.HeaderContext {

	private InstructionGraphics2D _header = new InstructionGraphics2D();

	private InstructionGraphics2D _content = new InstructionGraphics2D();

	private InstructionGraphics2D _footer = new InstructionGraphics2D();

	/**
	 * The header output.
	 */
	@Override
	public Graphics2D header() {
		return _header;
	}

	/**
	 * The content output.
	 */
	@Override
	public Graphics2D content() {
		return _content;
	}

	/**
	 * The footer output.
	 */
	@Override
	public Graphics2D footer() {
		return _footer;
	}

	/**
	 * Outputs the header content to the given device.
	 */
	public void replayHeader(Graphics2D out) {
		_header.replay(out);
	}

	/**
	 * Outputs the header content to the given device.
	 */
	public void replayContent(Graphics2D out) {
		_content.replay(out);
	}

	/**
	 * Outputs the header content to the given device.
	 */
	public void replayFooter(Graphics2D out) {
		_footer.replay(out);
	}

}
