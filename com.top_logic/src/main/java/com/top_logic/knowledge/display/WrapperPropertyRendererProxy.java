/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.display;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.PlainTextRenderer;

/**
 * A {@link Renderer} for {@link WrapperProperty} objects.
 * 
 * <p>
 * The deferred {@link WrapperProperty} values are resolved and rendered with a secondary
 * {@link Renderer} implementation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperPropertyRendererProxy implements Renderer<WrapperProperty<?>> {

	/**
	 * Default instance of this class that uses the default
	 * {@link PlainTextRenderer} for the actual work.
	 */
	public static final WrapperPropertyRendererProxy INSTANCE = 
		new WrapperPropertyRendererProxy(PlainTextRenderer.INSTANCE);
	
	private Renderer<Object> _valueRenderer;

	/**
	 * Construct a custom {@link WrapperPropertyRendererProxy} that uses a given
	 * {@link Renderer} implementation.
	 */
	public WrapperPropertyRendererProxy(Renderer<Object> valueRenderer) {
		_valueRenderer = valueRenderer;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WrapperProperty<?> deferredValue)
			throws IOException {
		_valueRenderer.write(context, out, deferredValue.get());
	}

}
