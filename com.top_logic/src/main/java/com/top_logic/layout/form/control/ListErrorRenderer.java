/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;

/**
 * {@link ErrorRenderer} implementation that displays an error message as
 * list-structured text.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListErrorRenderer extends AbstractErrorRenderer {
	
	public static final ListErrorRenderer INSTANCE = new ListErrorRenderer();
	
	/**
	 * Singleton constructor.
	 */
	protected ListErrorRenderer() {
		super();
	}

	@Override
	public void writeError(DisplayContext context, TagWriter out, ErrorDisplay error) throws IOException {
		out.beginBeginTag(UL);
		writeControlTagAttributes(context, out, error);
		out.endBeginTag();
		{
			for (Iterator it = error.getErrorFields(); it.hasNext(); ) {
				FormField field = (FormField) it.next();
				
				out.beginTag(LI);
				out.writeText((String) TO_ERROR_MESSAGE.map(field));
				out.endTag(LI);
			}
		}
		out.endTag(UL);
	}

	@Override
	public void handleHasErrorPropertyChange(ErrorDisplay display, boolean oldValue, boolean newValue) {
		display.requestRepaint();
	}
	
	@Override
	public void handleErrorPropertyChange(ErrorDisplay display) {
		display.requestRepaint();
	}
	
}
