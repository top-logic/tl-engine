/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Adapter that uses a {@link Renderer} to implement an {@link AbstractButtonRenderer} for
 * displaying {@link AbstractButtonControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractButtonLinkRenderer<C extends AbstractButtonLinkRenderer.Config<?>>
		extends AbstractButtonRenderer<C> {

	/**
	 * Create a {@link AbstractButtonLinkRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractButtonLinkRenderer(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void writeButton(DisplayContext context, TagWriter out, AbstractButtonControl<?> button)
			throws IOException {
		commandRenderer().write(context, out, button);
	}

	/**
	 * The {@link Renderer} to render the {@link ButtonControl}.
	 */
	protected abstract Renderer<? super AbstractButtonControl<?>> commandRenderer();

}
