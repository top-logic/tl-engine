/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.format.WikiWrite;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link Renderer} interpreting a string representation of an object as wiki text.
 * 
 * @see WikiWrite
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WikiTextRenderer extends AbstractConfiguredInstance<WikiTextRenderer.Config<?>>
		implements Renderer<Object> {

	/**
	 * Configuration options for {@link WikiTextRenderer}.
	 */
	public interface Config<I extends WikiTextRenderer> extends PolymorphicConfiguration<I> {
		/**
		 * Algorithm extracting wiki text from an object.
		 */
		@ItemDefault(MetaLabelProvider.class)
		PolymorphicConfiguration<LabelProvider> getTextExtractor();
	}

	/**
	 * Default {@link WikiTextRenderer} instance.
	 */
	@SuppressWarnings("unchecked")
	// Note: Explicit cast is required for certain Java compilers.
	public static final WikiTextRenderer DEFAULT_INSTANCE =
		(WikiTextRenderer) TypedConfigUtil.createInstance(TypedConfiguration.newConfigItem(Config.class));

	/**
	 * Creates a {@link WikiTextRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public WikiTextRenderer(InstantiationContext context, Config<?> config) {
		super(context, config);
		_textExtractor = context.getInstance(config.getTextExtractor());
	}

	private final LabelProvider _textExtractor;

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		WikiWrite.wikiWrite(out, _textExtractor.getLabel(value));
	}

}
