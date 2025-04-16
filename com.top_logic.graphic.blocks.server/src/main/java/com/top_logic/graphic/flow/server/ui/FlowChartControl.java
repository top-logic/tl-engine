/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.blocks.server.svg.SvgTagWriter;
import com.top_logic.graphic.flow.operations.FlowDiagram;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractVisibleControl;

/**
 * 
 */
public class FlowChartControl extends AbstractVisibleControl {

	private FlowDiagram _diagram;

	@Override
	public Object getModel() {
		return _diagram;
	}

	/**
	 * @see #getModel()
	 */
	public void setModel(FlowDiagram diagram) {
		_diagram = diagram;
		requestRepaint();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		if (_diagram != null) {
			_diagram.draw(new SvgTagWriter(out));
		}
		out.endTag(DIV);
	}

}
