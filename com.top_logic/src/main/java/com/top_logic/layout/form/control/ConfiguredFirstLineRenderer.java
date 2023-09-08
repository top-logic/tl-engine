/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;

/**
 * {@link FirstLineRenderer} that can be created with configuration options.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredFirstLineRenderer extends FirstLineRenderer
		implements ConfiguredInstance<ConfiguredFirstLineRenderer.Config<?>> {

	private Config<?> _config;

	/**
	 * Options of the {@link ConfiguredFirstLineRenderer} configuration.
	 */
	public interface Options extends ConfigurationItem {
		/**
		 * @see #getLabelProvider()
		 */
		String LABEL_PROVIDER = "labelProvider";

		/**
		 * @see #getTooltipRenderer()
		 */
		String TOOLTIP_RENDERER = "tooltipRenderer";

		/**
		 * {@link LabelProvider} producing a short representation of a complex value (e.g. the first
		 * line of a text).
		 */
		@Name(LABEL_PROVIDER)
		@ItemDefault(FirstLineLabelProvider.class)
		PolymorphicConfiguration<? extends LabelProvider> getLabelProvider();

		/**
		 * @see #getLabelProvider()
		 */
		void setLabelProvider(PolymorphicConfiguration<? extends LabelProvider> value);

		/**
		 * {@link Renderer} writing the complete value.
		 */
		@Name(TOOLTIP_RENDERER)
		@ItemDefault(WikiTextRenderer.class)
		PolymorphicConfiguration<? extends Renderer<?>> getTooltipRenderer();

		/**
		 * @see #getTooltipRenderer()
		 */
		void setTooltipRenderer(PolymorphicConfiguration<? extends Renderer<?>> value);
	}

	/**
	 * Configuration options for {@link ConfiguredFirstLineRenderer}.
	 */
	public interface Config<I extends ConfiguredFirstLineRenderer> extends PolymorphicConfiguration<I>, Options {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link ConfiguredFirstLineRenderer} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConfiguredFirstLineRenderer(InstantiationContext context, Config<?> config) {
		super(labelProvider(context, config), tooltipRenderer(context, config));
		_config = config;
	}

	@Override
	public Config<?> getConfig() {
		return _config;
	}

	static FirstLineRenderer createFirstLineRenderer(InstantiationContext context,
			Options config) {
		return new FirstLineRenderer(labelProvider(context, config), tooltipRenderer(context, config));
	}

	private static Renderer<Object> tooltipRenderer(InstantiationContext context, Options config) {
		Renderer<?> renderer = context.getInstance(config.getTooltipRenderer());
		Renderer<Object> tooltipRenderer = renderer == null ? null : renderer.generic();
		return tooltipRenderer;
	}

	private static LabelProvider labelProvider(InstantiationContext context, Options config) {
		LabelProvider labelProvider = context.getInstance(config.getLabelProvider());
		return labelProvider;
	}

}
