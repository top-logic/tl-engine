/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * {@link Renderer} implementation that is backed by another {@link Renderer}
 * instance as "default implementation".
 * 
 * <p>
 * Sub-classes must override {@link #write(DisplayContext, TagWriter, Object)}
 * and call the super implementation in the (sub-class-defined) default case.
 * This class is declared <code>abstract</code> (even if it has no
 * <code>abstract</code> methods), because there is no reason for
 * instantiating this class without overriding the
 * {@link #write(DisplayContext, TagWriter, Object)} method.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class RendererProxy<T> implements Renderer<T> {

	/**
	 * @see #RendererProxy(Renderer)
	 */
	private final Renderer<? super T> fallback;

	/**
	 * Create a {@link Renderer} that forwards all calls to the
	 * {@link #write(DisplayContext, TagWriter, Object)} method of the given
	 * fallback implementation.
	 */
	protected RendererProxy(final Renderer<? super T> fallback) {
		this.fallback = fallback;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, T value)
			throws IOException {
		fallback.write(context, out, value);
	}

	@Override
	public abstract <X> Renderer<? super X> generic(Class<X> expectedType);

}
