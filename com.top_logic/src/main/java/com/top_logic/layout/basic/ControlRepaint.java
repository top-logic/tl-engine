/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.ElementReplacement;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;

/**
 * Creates {@link ClientAction} that replaces the whole client-side view of a control with a newly
 * rendered fragment.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ControlRepaint {

	/**
	 * Creates an {@link ElementReplacement} which rewrites the whole Control.
	 * 
	 * <p>
	 * It is expected that the replacement is created for an {@link Control} which is requested to
	 * repaint.
	 * </p>
	 */
	public static ElementReplacement newControlRepaint(final Control view) {
		return new ElementReplacement(view.getID(), view);
	}

	/**
	 * Creates an {@link ElementReplacement} which rewrites the whole Control.
	 * 
	 * <p>
	 * This method is designed to be called within the internal
	 * {@link AbstractControlBase#internalRevalidate(DisplayContext, com.top_logic.layout.UpdateQueue)
	 * revalidation phase} of an {@link AbstractControlBase} to redraw self completely.
	 * </p>
	 */
	public static ElementReplacement newContentRepaint(final AbstractControlBase self) {
		String id = self.getID();
		HTMLFragment replacement = new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				self.internalWrite(context, out);
			}
		};
		return new ElementReplacement(id, replacement);
	}

}
