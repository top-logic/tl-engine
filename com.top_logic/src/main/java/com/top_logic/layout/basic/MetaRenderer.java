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
 * {@link Renderer} implementation that renders {@link Renderer} objects by
 * calling their {@link Renderer#write(DisplayContext, TagWriter, Object)}
 * method and forwarding the value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MetaRenderer<T extends Renderer<T>> implements Renderer<T> {
	@SuppressWarnings("rawtypes")
	public static final Renderer INSTANCE = new MetaRenderer();
	
	@Override
	public void write(DisplayContext context, TagWriter out, T value) throws IOException {
		value.write(context, out, value);
	}
}