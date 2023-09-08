/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.io.IOException;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * {@link PDFRenderer} that delegates the write process to a configured {@link Renderer}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PDFRendererAdapter extends AbstractConfiguredInstance<PDFRendererAdapter.Config> implements PDFRenderer {

	/**
	 * Typed configuration interface definition for {@link PDFRendererAdapter}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<PDFRendererAdapter> {

		/**
		 * The {@link Renderer} to delegate writing to.
		 */
		@Mandatory
		PolymorphicConfiguration<Renderer<Object>> getRenderer();

		/**
		 * Setter for {@link #getRenderer()}.
		 */
		void setRenderer(PolymorphicConfiguration<Renderer<Object>> value);
	}

	private final Renderer<Object> _renderer;

	/**
	 * Create a {@link PDFRendererAdapter}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PDFRendererAdapter(InstantiationContext context, Config config) {
		super(context, config);
		_renderer = context.getInstance(config.getRenderer());
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		_renderer.write(context, out, value);
	}

}

