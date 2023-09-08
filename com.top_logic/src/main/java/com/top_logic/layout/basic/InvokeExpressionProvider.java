/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.control.ButtonControl;

/**
 * {@link InvokeExpressionProvider}s are intended for use in {@link CommandModel}s and
 * {@link ButtonControl}s. This provider specifies which JavaScript function will be executed by
 * pressing the button for that {@link CommandModel}. For use of this interface see
 * {@link ButtonControl#writeOnClickContent(DisplayContext, Appendable)}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface InvokeExpressionProvider {

	/**
	 * This method returns a JavaScript function for the given {@link ButtonControl} to be executed
	 * when pressing the button.
	 * 
	 * @param context
	 *            the context which is used to render the given {@link ButtonControl}
	 * @param buttonControl
	 *            the {@link ButtonControl} to get the invoke expression for.
	 */
	public void writeInvokeExpression(DisplayContext context, Appendable out, AbstractButtonControl<?> buttonControl)
			throws IOException;

}
