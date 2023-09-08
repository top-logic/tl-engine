/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Represents a fragment of HTML that is created on demand. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface HTMLFragment {

	/**
	 * Marshals this fragment of HTML to the given writer.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}
	 * @param out
	 *        The {@link TagWriter} to write to.
	 */
	public void write(DisplayContext context, TagWriter out) throws IOException;
}
