/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.AbstractControlRenderer;
import com.top_logic.layout.form.FormField;

/**
 * Abstract base class for renderers for {@link ErrorDisplay}s.
 * 
 * <p>
 * Adapts the {@link Renderer#write(DisplayContext, TagWriter, Object)} method
 * to values of type {@link ErrorDisplay}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractErrorRenderer extends AbstractControlRenderer<ErrorDisplay> implements ErrorRenderer {

	public static final Mapping TO_ERROR_MESSAGE = new Mapping() {
			@Override
			public Object map(Object input) {
				FormField field = (FormField) input;
				return field.getLabel() + ": " + field.getError();
			}
		};

	/**
	 * Overridden to provide a type-safe rendering method for sub-classes. 
	 * 
	 * <p>
	 * Forwards to {@link #writeError(DisplayContext, TagWriter, ErrorDisplay)}
	 * </p>
	 * 
	 * @see Renderer#write(DisplayContext, TagWriter, Object)
	 */
	@Override
	public final void write(DisplayContext context, TagWriter out, ErrorDisplay value) throws IOException {
		writeError(context, out, value);
	}

}
