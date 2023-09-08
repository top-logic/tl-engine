/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import java.awt.Image;
import java.util.List;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;

/**
 * Default-implementation of {@link LabelIconProvider} that works with a configured list of
 * type-names.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class DefaultLabelIconProvider implements LabelIconProvider,
		ConfiguredInstance<DefaultLabelIconProvider.Config> {

	/**
	 * Config-interface for {@link DefaultLabelIconProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<DefaultLabelIconProvider> {

		/**
		 * the type-names to create icons for
		 */
		@ListBinding(attribute = "name")
		public List<String> getTypes();

	}

	private Config _config;

	private Image[] _icons;

	/**
	 * Config-constructor for {@link DefaultLabelIconProvider}
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public DefaultLabelIconProvider(InstantiationContext context, Config config) {
		_config = config;
		List<String> types = config.getTypes();
		_icons = new Image[types.size()];
		Theme theme = ThemeFactory.getTheme();
		int i = 0;
		for (String type : types) {
			Image icon = theme.getImage(type);
			_icons[i++] = icon;
		}
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Image[] getIcons() {
		return _icons;
	}

}