/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.FormattedLabelProvider;

/**
 * A {@link Renderer} implementation that renders a given value as single text
 * node.
 * 
 * <p>
 * The conversion of the rendered value to a plain string is performed by a
 * {@link LabelProvider}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PlainTextRenderer implements Renderer<Object> {
	
	/**
	 * Default instance of this class that uses a {@link FormattedLabelProvider}
	 * for converting its rendered values into a textual representation.
	 */
	public static final PlainTextRenderer INSTANCE = 
		new PlainTextRenderer(FormattedLabelProvider.INSTANCE);
	
	/**
	 * @see #PlainTextRenderer(LabelProvider)
	 */
	private final LabelProvider labelProvider;
	
	/**
	 * Create a new {@link PlainTextRenderer} that uses the given
	 * {@link LabelProvider} for {@link Object} to {@link String} conversion.
	 */
	public PlainTextRenderer(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value)
		throws IOException 
	{
		out.writeText(labelProvider.getLabel(value));
	}

}
