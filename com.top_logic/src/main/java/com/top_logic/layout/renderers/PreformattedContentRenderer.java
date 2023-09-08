/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.renderers;

import java.io.IOException;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * {@link PreformattedRenderer} whose content {@link Renderer} can be configured.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PreformattedContentRenderer<T> extends PreformattedRenderer<T>
		implements ConfiguredInstance<PreformattedContentRenderer.Config<T>> {

	private final Renderer<? super T> _contentRenderer;

	private final Config<T> _config;

	/**
	 * Typed configuration interface definition for {@link PreformattedContentRenderer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<T> extends PolymorphicConfiguration<PreformattedContentRenderer<T>> {

		/**
		 * The {@link Renderer} that renderes the actual content.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends Renderer<? super T>> getContentRenderer();
	}

	/**
	 * Create a {@link PreformattedContentRenderer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public PreformattedContentRenderer(InstantiationContext context, Config<T> config) {
		_config = config;
		_contentRenderer = context.getInstance(config.getContentRenderer());
	}

	@Override
	protected void writeContent(DisplayContext context, TagWriter out, T value) throws IOException {
		_contentRenderer.write(context, out, value);
	}

	@Override
	public Config<T> getConfig() {
		return _config;
	}

}

