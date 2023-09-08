/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.ControlRenderer;

/**
 * Interface for specialized renderers for {@link ErrorDisplay}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ErrorRenderer extends ControlRenderer<ErrorDisplay> {

	/**
	 * Sub-classes renders the given {@link ErrorDisplay} to the given writer.
	 */
	public abstract void writeError(DisplayContext context, TagWriter out, ErrorDisplay error) throws IOException;
	
	/**
	 * Sub-classes adjust the client-side view of the rendered
	 * {@link ErrorDisplay} to a change of the "has-error" property (by
	 * {@link AbstractControl#addUpdate(ClientAction) adding} corresponding
	 * incremental updates.
	 */
	public abstract void handleHasErrorPropertyChange(ErrorDisplay display, boolean oldValue, boolean newValue);

	/**
	 * @see #handleHasErrorPropertyChange(ErrorDisplay, boolean, boolean)
	 */
	public abstract void handleErrorPropertyChange(ErrorDisplay display);
	
}
