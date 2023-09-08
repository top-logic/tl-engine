/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Paint;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;

/**
 * {@link ColorProvider} for a single configured color.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class SingleColorProvider implements ColorProvider, ColorContext, ConfiguredInstance<SingleColorProvider.Config> {

	private final Config _config;

	public interface Config extends PolymorphicConfiguration<SingleColorProvider> {

		@Format(HexEncodedPaint.class)
		public Paint getColor();

	}

	public SingleColorProvider(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Paint getColor(Object obj) {
		return _config.getColor();
	}

	@Override
	public ColorContext createColorContext() {
		return this;
	}

}
