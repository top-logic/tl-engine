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
import com.top_logic.layout.provider.LabelProviderService;

/**
 * {@link Renderer} implementation that forwards to a {@link Renderer} provided by the
 * {@link LabelProviderService} for the given value.
 * 
 * @see LabelProviderService#getRenderer(Object)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DispatchingRenderer implements Renderer<Object> {
	
	/**
	 * Singleton instance of this class.
	 */
	public static final DispatchingRenderer INSTANCE = new DispatchingRenderer();

	/**
	 * Singleton constructor that allows sub-classes.
	 */
	protected DispatchingRenderer() {
		super();
	}
	
	@Override
	public void write(DisplayContext context, TagWriter out, Object value)
			throws IOException {
		LabelProviderService.getInstance().getRenderer(value).write(context, out, value);
	}

	@Override
	public <X> Renderer<? super X> generic(Class<X> expectedType) {
		return this;
	}

}
