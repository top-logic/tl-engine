/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * Decorator for a {@link CompareInfo}.
 * 
 * <p>
 * A {@link DetailDecorator} is a renderer for a {@link CompareInfo}. It renders informations about
 * the change, but not the changed content. A typical {@link DetailDecorator} renders an image
 * depending on the {@link ChangeInfo kind} of a change and a tooltip displaying the "other" value.
 * </p>
 * 
 * @see DefaultCompareInfoDecorator
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DetailDecorator {

	/**
	 * In which context the {@link CompareInfo} is rendered.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public enum Context {

		/**
		 * Detail decorator is used in default context, i.e. no special other contexts.
		 */
		DEFAULT,

		/**
		 * Detail decorator is used in tables for a decorator column.
		 */
		TABLE,
		;
	}

	/**
	 * Starts the decoration.
	 * 
	 * <p>
	 * This method is called before the rendering of the actual value.
	 * </p>
	 * 
	 * @param info
	 *        The {@link CompareInfo} to render.
	 * @param renderContext
	 *        In which context this {@link DetailDecorator} is used.
	 * 
	 * @see #end(DisplayContext, TagWriter, CompareInfo, Context)
	 */
	void start(DisplayContext context, TagWriter out, CompareInfo info, Context renderContext) throws IOException;

	/**
	 * Ends the decoration started in
	 * {@link #start(DisplayContext, TagWriter, CompareInfo, Context)}.
	 * 
	 * <p>
	 * This method is called before the rendering of the actual value.
	 * </p>
	 * 
	 * @param info
	 *        The {@link CompareInfo} to render.
	 * @param renderContext
	 *        In which context this {@link DetailDecorator} is used.
	 * 
	 * @see #start(DisplayContext, TagWriter, CompareInfo, Context)
	 */
	void end(DisplayContext context, TagWriter out, CompareInfo info, Context renderContext) throws IOException;
}
