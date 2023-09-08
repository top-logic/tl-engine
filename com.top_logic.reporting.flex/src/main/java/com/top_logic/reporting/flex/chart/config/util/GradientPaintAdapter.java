/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.reporting.flex.chart.config.color.ColorContext;
import com.top_logic.reporting.flex.chart.config.color.ColorProvider;

/**
 * {@link ColorProvider} that translates an input-color from another ColorProvider to a
 * {@link GradientPaint}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class GradientPaintAdapter implements ColorProvider, ConfiguredInstance<GradientPaintAdapter.Config> {

	/**
	 * Config-interface for {@link GradientPaintAdapter}.
	 */
	public interface Config extends PolymorphicConfiguration<GradientPaintAdapter> {

		/**
		 * the initial color-provider for the base-color
		 */
		@InstanceFormat
		public ColorProvider getColorProvider();

		/**
		 * the value every component from the input-color should be shifted to get a second
		 *         color for the {@link GradientPaint}
		 */
		@IntDefault(70)
		public int getRGBShift();

	}

	private final Config _config;

	/**
	 * Config-Constructor for {@link GradientPaintAdapter}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public GradientPaintAdapter(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public ColorContext createColorContext() {
		final ColorContext innerContext = getConfig().getColorProvider().createColorContext();
		final int rgbShift = getConfig().getRGBShift();
		return new ColorContext() {

			@Override
			public Paint getColor(Object obj) {
				Paint color = innerContext.getColor(obj);
				return toGradientColor(rgbShift, color);
			}
		};
	}

	/**
	 * @param rgbShift
	 *        the value the components from the input-color should be shifted
	 * @param color
	 *        the first color for the {@link GradientPaint}
	 * @return the resulting {@link GradientPaint} based on the input color
	 */
	public static Paint toGradientColor(int rgbShift, Paint color) {
		if (color instanceof Color) {
			Color c1 = (Color) color;
			int red = Math.max(c1.getRed() - rgbShift, 0);
			int green = Math.max(c1.getGreen() - rgbShift, 0);
			int blue = Math.max(c1.getBlue() - rgbShift, 0);
			Color c2 = new Color(red, green, blue);

			// Coordinates are irrelevant for JFreeChart, therefore we use 0.
			// GradientPaint will be translated to fit bar anyway: See
			// BarRenderer#getGradientPaintTransformer()
			GradientPaint gradient = new GradientPaint(0f, 0f, c1, 0f, 0f, c2);

			return gradient;
		}
		return color;
	}

}
