/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * The class {@link ContentDecorator} can be used to render additional decoration for some
 * {@link Object}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ContentDecorator {

	/**
	 * This method writes the HTML code before the actual content of the control.
	 */
	void startDecoration(DisplayContext context, TagWriter out, Object value) throws IOException;

	/**
	 * This method writes the HTMLGroup after the actual content of the control
	 */
	void endDecoration(DisplayContext context, TagWriter out, Object value) throws IOException;

}
