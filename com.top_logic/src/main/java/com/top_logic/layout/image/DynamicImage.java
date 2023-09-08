/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.top_logic.layout.DisplayContext;

/**
 * TODO BHU.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DynamicImage {
	
	void prepare(DisplayContext context, int width, int height);
	
	String getID();
	String getLabel();
	
	int getWidth();
	int getHeight();
	
	void encode(DisplayContext context, OutputStream output, String format) throws IOException;
	Map getImageMap();
	
}
