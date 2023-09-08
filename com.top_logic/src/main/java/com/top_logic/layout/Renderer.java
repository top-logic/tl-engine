/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;

/**
 * A {@link Renderer} is a generic mechanism to transform a given value into a sequence of events on
 * a {@link TagWriter} to produce XML output.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @param <T>
 *        The type of values this {@link Renderer} can handle.
 */
public interface Renderer<T> {

	/**
	 * Render the given value.
	 * 
	 * @param context
	 *     The context in which the rendering occurs.
	 * @param out
	 *     The {@link TagWriter} output to which the output should be generated.
	 * @param value
	 *     The value to render.
	 */
	void write(DisplayContext context, TagWriter out, T value)
		throws IOException;

	/**
	 * A {@link Renderer} that can be safely used for all potential values.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	default Renderer<Object> generic() {
		// Note: Javac fails without the cast.
		return generic((Class) Object.class);
	}

	/**
	 * A {@link Renderer} that can be safely used for values of the given type.
	 */
	default <X> Renderer<? super X> generic(Class<X> expectedType) {
		return RendererUtil.upgrade(expectedType, this);
	}
}
