/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.reporting.flex.chart.config.UniqueName;

/**
 * {@link ColorProvider} with a configured number of colors that are provided in sequence.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class OrderedColorProvider implements ColorProvider, ConfiguredInstance<OrderedColorProvider.Config> {

	/**
	 * Config-interface for {@link OrderedColorProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<OrderedColorProvider> {

		/**
		 * a list of string-encoded colors
		 */
		@ListBinding(format = HexEncodedPaint.class)
		public List<? extends Paint> getColors();

		/**
		 * See {@link #getColors()}
		 */
		public void setColors(List<? extends Paint> colors);

		@InstanceFormat
		ColorProvider getFallback();

		void setFallback(ColorProvider value);

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link OrderedColorProvider}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public OrderedColorProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public ColorContext createColorContext() {
		return new OrderedColorContext(getColors(), _config.getFallback());
	}

	/**
	 * the configured colors
	 */
	public List<? extends Paint> getColors() {
		return _config.getColors();
	}

	/**
	 * Implementation of {@link com.top_logic.reporting.flex.chart.config.color.ColorContext} for
	 * {@link OrderedColorProvider}.
	 * 
	 * @author <a href=mailto:cca@top-logic.com>cca</a>
	 */
	public static class OrderedColorContext implements ModifiableColorContext, ColorProvider,
			ConfiguredInstance<Config> {

		private final List<Paint> _unusedColors;

		private int _nextIndex = 0;

		private final ColorProvider _fallbackProvider;

		private final Map<Object, Paint> _assignedColors;

		private ColorContext _fallback;

		@Override
		public ColorContext createColorContext() {
			return this;
		}

		@Override
		public Config getConfig() {
			return newConfig(getHandled(), _fallbackProvider);
		}

		/**
		 * Creates a new OrderedColorContext for the given list of colors.
		 */
		public OrderedColorContext(List<? extends Paint> colors, ColorProvider fallbackProvider) {
			_unusedColors = new ArrayList<>(colors);
			_fallbackProvider = fallbackProvider == null ? new ConfiguredColorProvider() : fallbackProvider;
			_fallback = _fallbackProvider.createColorContext();

			_assignedColors = new LinkedHashMap<>();
		}

		private Paint next(Object group) {
			if (_nextIndex >= _unusedColors.size()) {
				return _fallback.getColor(group);
			} else {
				return _unusedColors.get(_nextIndex++);
			}
		}

		@Override
		public Paint getColor(Object group) {
			if (group instanceof UniqueName) {
				group = ((UniqueName) group).getKey();
			}
			Paint assignedColor = _assignedColors.get(group);
			if (assignedColor != null) {
				return assignedColor;
			}
			Paint newColor = next(group);
			_assignedColors.put(group, newColor);
			return newColor;
		}

		@Override
		public void setColor(Object key, Color col) {
			_assignedColors.put(key, col);
		}

		@Override
		public void reset() {
			_fallback = _fallbackProvider.createColorContext();
			_nextIndex = 0;
			_assignedColors.clear();
		}

		/**
		 * the map of color by object containing all objects where a color has been
		 *         requestet.
		 */
		public List<Paint> getHandled() {
			return new ArrayList<>(_assignedColors.values());
		}

	}

	/**
	 * Factory method to create an initialized {@link Config}.
	 * 
	 * @return a new ConfigItem.
	 */
	public static Config newConfig(List<? extends Paint> colors, ColorProvider fallback) {
		Config item = TypedConfiguration.newConfigItem(Config.class);
		if (colors != null) {
			item.setColors(colors);
		}
		item.setFallback(fallback);
		return item;
	}

	/**
	 * Factory method to create an initialized {@link OrderedColorProvider}.
	 * 
	 * @return a new OrderedColorProvider.
	 */
	public static OrderedColorProvider newInstance(List<? extends Paint> colors, ColorProvider fallback) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(newConfig(colors, fallback));
	}

}
