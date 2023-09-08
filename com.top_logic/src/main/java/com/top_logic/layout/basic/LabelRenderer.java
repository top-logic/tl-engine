/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link Renderer} that writes the label of a certain object by getting the actual label from a
 * {@link LabelProvider}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LabelRenderer implements Renderer<Object> {

	/**
	 * Default instance using {@link MetaLabelProvider}.
	 */
	public static final LabelRenderer INSTANCE = new LabelRenderer(MetaLabelProvider.INSTANCE);

	private final LabelProvider _labelProvider;

	/**
	 * Creates a new {@link LabelRenderer}.
	 * 
	 * @param labelProvider
	 *        the {@link LabelProvider} to resolve label for the object to write.
	 */
	public LabelRenderer(LabelProvider labelProvider) {
		_labelProvider = labelProvider;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		out.writeText(_labelProvider.getLabel(value));
	}

	@Override
	public <X> Renderer<? super X> generic(Class<X> expectedType) {
		return this;
	}

}
