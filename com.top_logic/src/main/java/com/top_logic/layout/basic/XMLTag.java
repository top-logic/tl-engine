/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Like a {@link HTMLFragment} but with a hole that can be filled by the caller.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface XMLTag {

	/**
	 * Starts the tag.
	 * 
	 * @see TagWriter#beginBeginTag(String)
	 */
	void beginBeginTag(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Closes the begin tag (finish writing attributes).
	 * 
	 * @see TagWriter#endBeginTag()
	 */
	void endBeginTag(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Writes the end tag.
	 * 
	 * @see TagWriter#endTag(String)
	 */
	void endTag(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Convenience method for calling {@link #endBeginTag(DisplayContext, TagWriter)} and
	 * {@link #endTag(DisplayContext, TagWriter)} in order.
	 */
	void endEmptyTag(DisplayContext context, TagWriter out) throws IOException;

}
