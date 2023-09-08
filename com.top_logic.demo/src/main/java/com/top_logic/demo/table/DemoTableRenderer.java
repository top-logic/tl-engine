/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;

/**
 * The class {@link DemoTableRenderer} writes different content for different header rows. 
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoTableRenderer extends DefaultTableRenderer {
	
	public DemoTableRenderer(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}
	
	@Override
	public void writeColumnHeader(DisplayContext context, TagWriter out, RenderState state, int rowNumber, int column)
			throws IOException {
		if ((rowNumber == 0) || isAdditionalHeader(rowNumber)) {
			super.writeColumnHeader(context, out, state, rowNumber, column);
		} else {
			out.writeText("Header Row:  " + rowNumber + " Col: " + column);
		}
	}
	
}

