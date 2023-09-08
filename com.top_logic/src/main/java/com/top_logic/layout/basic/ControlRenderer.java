/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link Renderer} that can be used by {@link Control} to delegate the actual rendering to
 * {@link ControlRenderer#write(DisplayContext, TagWriter, Object)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ControlRenderer<T extends Control> extends Renderer<T>, HTMLConstants {

	/**
	 * Callback method for {@link AbstractControlBase} to call in
	 * {@link AbstractControlBase#writeControlClassesContent(Appendable)}.
	 * 
	 * @param out
	 *        The stream to append control CSS classes to.
	 * @param control
	 *        The control to get write additional CSS classes for.
	 * 
	 * @throws IOException
	 *         Thrown by the given {@link Appendable}
	 * 
	 * @see HTMLUtil#appendCSSClass(Appendable, String)
	 */
	default void appendControlCSSClasses(Appendable out, T control) throws IOException {
		// No additional CSS classes in general
	}

}

