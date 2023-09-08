/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.ConstantDisplayValue;

/**
 * A String value that depends on the current {@link com.top_logic.layout.DisplayContext}.
 * 
 * <p>
 * Note: This interface should only be implemented directly, if there is a significant efficiency
 * gain.
 * </p>
 * 
 * @see AbstractDisplayValue Implementing the common case that requires string concatenation for
 *      producing the output.
 * @see SimpleDisplayValue When only a value has been looked up.
 * @see ConstantDisplayValue When the value has already been created.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DisplayValue extends DynamicText, HTMLFragment {

	/**
	 * Compute the value represented by this object valid for the given
	 * {@link com.top_logic.layout.DisplayContext}.
	 * 
	 * <p>
	 * Note: Whenever possible, consider using {@link #append(DisplayContext, Appendable)} instead.
	 * </p>
	 * 
	 * @param context
	 *     The context in which the value is displayed.
	 *     
	 * @return
	 *     The value as string.
	 */
	String get(DisplayContext context);

	@Override
	default void write(DisplayContext context, TagWriter out) throws IOException {
		append(context, out);
	}
}
